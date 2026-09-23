#!/usr/bin/env python3
"""Pass 2 of the 26.1 port: compiler-driven method/field renames.

Reads javac -XDrawDiagnostics output (argv[1]), and for every "cannot find symbol" on a
method or field whose owner type (or a supertype) has a unique rename in the derived
1.21.1 -> 1.21.11 table (plus the 26.1 extras below), rewrites that identifier at the
exact line:column javac reported. Nothing else is touched. Run from the repo root,
with argv[2] = file listing the compiled source paths and argv[3] = classpath file.
"""
import collections
import json
import os
import re
import subprocess
import sys

JAVAP = '/opt/homebrew/Cellar/openjdk@25/25.0.4.1/libexec/openjdk.jdk/Contents/Home/bin/javap'
diag_path, srcs_path, cp_path = sys.argv[1:4]
cp = open(cp_path).read().strip()

table = json.load(open('.scratch/port-26.1/rename-1.21.1-to-1.21.11.json'))
methods = collections.defaultdict(set)  # (new owner, old name) -> {new name}
for own, name, _args, own2, name2, _args2 in table['method']:
    if name != name2:
        methods[(own2.replace('$', '.'), name)].add(name2)
fields = collections.defaultdict(set)
for own, name, own2, name2 in table['field']:
    if name != name2:
        fields[(own2.replace('$', '.'), name)].add(name2)

# 26.1 renames not covered by intermediary (26.1 primer, GuiGraphics -> GuiGraphicsExtractor)
GUI_OVERRIDES = {
    'render': 'extractRenderState', 'renderWidget': 'extractWidgetRenderState',
    'renderBackground': 'extractBackground', 'renderBlurredBackground': 'extractBlurredBackground',
    'renderLabels': 'extractLabels',
}
GUI ='net.minecraft.client.gui.GuiGraphicsExtractor'
for old, new in {
    'drawString': 'text', 'drawCenteredString': 'centeredText', 'renderItem': 'item',
    'renderFakeItem': 'fakeItem', 'renderItemDecorations': 'itemDecorations',
    'renderTooltip': 'setTooltipForNextFrame', 'renderComponentTooltip': 'setComponentTooltipForNextFrame',
    'hLine': 'horizontalLine', 'vLine': 'verticalLine', 'drawWordWrap': 'textWithWordWrap',
}.items():
    methods[(GUI, old)].add(new)
for owner in ('net.minecraft.client.gui.screens.Screen', 'net.minecraft.client.gui.components.AbstractWidget',
              'net.minecraft.client.gui.components.Renderable'):
    for old, new in GUI_OVERRIDES.items():
        methods[(owner, old)].add(new)

_supers = {}


def binary_names(c):
    """Candidate binary names for a dotted source name (nested classes use $)."""
    dots = [m.start() for m in re.finditer(r'\.', c)]
    out = [c]
    for k in range(1, 4):
        if len(dots) >= k:
            s = list(c)
            for i in dots[-k:]:
                s[i] = '$'
            out.append(''.join(s))
    return out


def mod_supers(t):
    """Supertypes of one of the mod's own classes, read from its source (top-level or nested)."""
    parts = t.split('.')
    for i in range(len(parts), 3, -1):
        path = 'src/main/java/' + '/'.join(parts[:i]) + '.java'
        if os.path.exists(path):
            break
    else:
        return []
    src = open(path).read()
    simple = parts[-1]
    m = re.search(r'\b(?:class|interface|record)\s+' + simple + r'\b(?:\s*<(?:[^<>]|<(?:[^<>]|<[^<>]*>)*>)*>)?'
                  r'(?:\s*\([^)]*\))?([^{]*)\{', src)
    if not m:
        return []
    decl = m.group(1)
    while re.search(r'<[^<>]*>', decl):
        decl = re.sub(r'<[^<>]*>', '', decl)
    names = re.findall(r'[\w.]+', re.sub(r'\b(extends|implements|permits)\b', ',', decl.split('permits')[0]))
    imports = dict((i.split('.')[-1], i) for i in re.findall(r'^import ([\w.]+);', src, re.M))
    pkg = re.search(r'^package ([\w.]+);', src, re.M).group(1)
    out = []
    for n in names:
        if '.' in n:
            out.append(n)
        elif n in imports:
            out.append(imports[n])
        elif os.path.exists('src/main/java/' + pkg.replace('.', '/') + '/' + n + '.java'):
            out.append(pkg + '.' + n)
    return out


def load_supers(types):
    """Fill _supers for all types in one javap run per call."""
    types = [t for t in types if t not in _supers]
    if not types:
        return
    cands = {b: t for t in types for b in binary_names(t)}
    r = subprocess.run([JAVAP, '-cp', cp] + list(cands), capture_output=True, text=True)
    for t in types:
        _supers[t] = []
        if t.startswith('com.portingdeadmods.researchd.'):
            _supers[t] = mod_supers(t)
    for head in r.stdout.splitlines():
        if not re.search(r'\b(class|interface)\s', head) or head.startswith(' '):
            continue
        flat = head
        while re.search(r'<[^<>]*>', flat):
            flat = re.sub(r'<[^<>]*>', '', flat)
        m = re.search(r'\b(?:class|interface)\s+([\w.$]+)', flat)
        if not m:
            continue
        name = m.group(1).replace('$', '.')
        if name not in _supers:
            continue
        for kw in ('extends', 'implements'):
            g = re.search(r'\b' + kw + r'\s+([\w.$, ]+?)(?:\s+implements|\s*\{|$)', flat)
            if g:
                _supers[name] += [s.strip().replace('$', '.') for s in g.group(1).split(',') if s.strip()]


def supertypes(t):
    """t and all its supertypes, breadth-first."""
    out, level, seen = [], [t], set()
    while level:
        level = [c for c in level if c not in seen]
        seen.update(level)
        out += level
        load_supers(level)
        level = [s for c in level for s in _supers[c]]
    return out


def lookup(tbl, owner, name):
    for t in supertypes(owner):
        news = tbl.get((t, name))
        if news:
            return next(iter(news)) if len(news) == 1 else None
    return None


by_base = collections.defaultdict(list)
for p in open(srcs_path).read().split():
    by_base[os.path.basename(p)].append(p)

edits = []  # (path, line, col, old, new)
pat = re.compile(
    r'^(\S+\.java):(\d+):(\d+): compiler\.err\.cant\.resolve\.location(?:\.args)?: kindname\.(method|variable), (\w+), .*?'
    r'\(compiler\.misc\.location(?:\.1)?: kindname\.\w+, ([^,]+?)(?:, ([^,]+?))?\)$')
hits = []
for line in open(diag_path):
    m = pat.match(line.strip())
    if not m:
        continue
    base, ln, col, kind, name, a, b = m.groups()
    owner = b if b and b != 'null' else a
    owner = re.sub(r'<.*>', '', owner).strip()
    hits.append((base, ln, col, kind, name, owner))

# Load the whole supertype closure of every owner, one javap run per level
level = list({h[5] for h in hits})
while level:
    load_supers(level)
    level = list({s for c in level for s in _supers[c] if s not in _supers})

for base, ln, col, kind, name, owner in hits:
    new = lookup(methods if kind == 'method' else fields, owner, name)
    if new:
        for p in by_base[base]:
            edits.append((p, int(ln), int(col), name, new))

done = 0
for p in {e[0] for e in edits}:
    lines = open(p).read().split('\n')
    for _, ln, col, old, new in sorted((e for e in edits if e[0] == p), key=lambda e: (e[1], -e[2])):
        if ln > len(lines):
            continue
        s = lines[ln - 1]
        m = re.compile(r'\b' + old + r'\b').search(s, max(col - 1, 0))
        if m and m.start() - (col - 1) <= 2:
            lines[ln - 1] = s[:m.start()] + new + s[m.end():]
            done += 1
    open(p, 'w').write('\n'.join(lines))
print(f'pass2: {done} identifiers renamed')

# Pass 3: overriding GUI declarations whose superclass method became extract*.
decl = 0
for line in open(diag_path):
    m = re.match(r'^(\S+\.java):(\d+):\d+: compiler\.err\.method\.does\.not\.override\.superclass', line)
    if not m:
        continue
    for p in by_base[m.group(1)]:
        lines = open(p).read().split('\n')
        ln = int(m.group(2)) - 1
        for k in range(ln, min(ln + 5, len(lines))):
            mm = re.search(r'\b(\w+)\s*\(\s*(?:final\s+)?GuiGraphicsExtractor\b', lines[k])
            if mm and mm.group(1) in GUI_OVERRIDES:
                lines[k] = lines[k][:mm.start(1)] + GUI_OVERRIDES[mm.group(1)] + lines[k][mm.end(1):]
                decl += 1
                open(p, 'w').write('\n'.join(lines))
                break
print(f'pass3: {decl} GUI override declarations renamed')

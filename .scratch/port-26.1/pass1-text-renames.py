#!/usr/bin/env python3
"""Pass 1 of the 26.1 port: whole-word text renames (classes, packages, a few static members).

Run from the repo root. Idempotent. See research-upgrade-path.md for the sources of each rule.
"""
import glob
import re

RULES = [
    # 1.21.11 class renames/moves (derived table)
    (r'\bnet\.minecraft\.resources\.ResourceLocation\b', 'net.minecraft.resources.Identifier'),
    (r'\bResourceLocation\b', 'Identifier'),
    (r'\bnet\.minecraft\.advancements\.critereon\b', 'net.minecraft.advancements.criterion'),
    (r'^import net\.minecraft\.Util;', 'import net.minecraft.util.Util;'),
    (r'^import net\.minecraft\.client\.renderer\.RenderType;', 'import net.minecraft.client.renderer.rendertype.RenderType;'),
    (r'^import net\.minecraft\.util\.FastColor;', 'import net.minecraft.util.ARGB;'),
    (r'\bFastColor\.ARGB32\.', 'ARGB.'),
    # 26.1 (primer)
    (r'\bGuiGraphics\b', 'GuiGraphicsExtractor'),
    # NeoForge 21.9 FML / networking
    (r'\bFMLEnvironment\.dist\b', 'FMLEnvironment.getDist()'),
    (r'(?<![\w.])PacketDistributor\.sendToServer\b', 'ClientPacketDistributor.sendToServer'),
    # 1.21.9: Level#isClientSide became a method
    (r'\.isClientSide\b(?!\s*\()', '.isClientSide()'),
    # Porting-Dead-Libs 1.1.9+: api.utils -> api.misc
    (r'\bportingdeadlibs\.api\.utils\.(IOAction|PDLDeferredRegisterItems|RGBAColor)\b', r'portingdeadlibs.api.misc.\1'),
]

NEEDED_IMPORTS = {
    'ClientPacketDistributor.': 'net.neoforged.neoforge.client.network.ClientPacketDistributor',
}


def add_import(src, fqn):
    if re.search(r'^import ' + re.escape(fqn) + ';', src, re.M):
        return src
    m = list(re.finditer(r'^import .*;$', src, re.M))
    at = m[-1].end() if m else re.search(r'^package .*;$', src, re.M).end()
    return src[:at] + '\nimport ' + fqn + ';' + src[at:]


changed = 0
for path in glob.glob('src/**/*.java', recursive=True):
    with open(path) as f:
        src = f.read()
    new = src
    for pat, rep in RULES:
        new = re.sub(pat, rep, new, flags=re.M)
    for marker, fqn in NEEDED_IMPORTS.items():
        if marker in new:
            new = add_import(new, fqn)
    if new != src:
        with open(path, 'w') as f:
            f.write(new)
        changed += 1
print(f'pass1: {changed} files changed')

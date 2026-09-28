package com.portingdeadmods.researchd.impl.research;

import com.portingdeadmods.researchd.api.research.packs.ResearchPack;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

/**
 * The Research Packs the creative tab and the recipe viewers list: one stack per pack, never the bare pack. The client
 * keeps one in {@link #CLIENT} and updates it from every {@code UpdateResearchPacksPayload}, so it survives dimension
 * changes, which give the client level a fresh, empty pack manager.
 */
public final class ResearchPackListing {
    public static final ResearchPackListing CLIENT = new ResearchPackListing();

    private static final Comparator<Map.Entry<ResourceKey<ResearchPack>, ResearchPack>> ORDER =
            Comparator.<Map.Entry<ResourceKey<ResearchPack>, ResearchPack>>comparingInt(
                            e -> e.getValue().sortingValue())
                    .thenComparing(e -> e.getKey().identifier());

    private List<ResourceKey<ResearchPack>> packs = List.of();

    /** Lists exactly {@code packs}, sorted by their sorting value, and returns what that added and removed. */
    public Change update(Map<ResourceKey<ResearchPack>, ResearchPack> packs) {
        List<ResourceKey<ResearchPack>> before = this.packs;
        this.packs =
                packs.entrySet().stream().sorted(ORDER).map(Map.Entry::getKey).toList();
        return new Change(
                this.packs.stream().filter(pack -> !before.contains(pack)).toList(),
                before.stream().filter(pack -> !this.packs.contains(pack)).toList());
    }

    public void clear() {
        this.packs = List.of();
    }

    public List<ItemStack> stacks() {
        return stacks(this.packs);
    }

    public static List<ItemStack> stacks(Collection<ResourceKey<ResearchPack>> packs) {
        return packs.stream().map(ResearchPackImpl::asStack).toList();
    }

    public record Change(List<ResourceKey<ResearchPack>> added, List<ResourceKey<ResearchPack>> removed) {
        public boolean isEmpty() {
            return this.added.isEmpty() && this.removed.isEmpty();
        }
    }
}

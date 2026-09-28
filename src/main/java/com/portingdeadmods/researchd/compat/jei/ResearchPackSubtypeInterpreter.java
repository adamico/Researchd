package com.portingdeadmods.researchd.compat.jei;

import com.portingdeadmods.researchd.data.ResearchdDataComponents;
import com.portingdeadmods.researchd.data.components.ResearchPackComponent;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Tells Research Packs apart by their pack. The bare pack, with no pack or an empty one, has no subtype. */
public class ResearchPackSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
    @Override
    public @Nullable Object getSubtypeData(@NotNull ItemStack stack, @NotNull UidContext context) {
        ResearchPackComponent component = stack.get(ResearchdDataComponents.RESEARCH_PACK.get());
        if (component == null) {
            return null;
        }
        return component.researchPackKey().orElse(null);
    }
}

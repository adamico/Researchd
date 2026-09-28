package com.portingdeadmods.researchd.compat.jei;

import com.portingdeadmods.researchd.Researchd;
import com.portingdeadmods.researchd.compat.JEICompat;
import com.portingdeadmods.researchd.impl.research.ResearchPackListing;
import com.portingdeadmods.researchd.registries.ResearchdItems;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public final class ResearchdJeiPlugin implements IModPlugin {
    public static final Identifier UID = Researchd.rl("researchd_jei_plugin");

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        JEICompat.RUNTIME = jeiRuntime;
        // JEI lists every item's default stack, which for Research Packs is the bare pack
        jeiRuntime
                .getIngredientManager()
                .removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, List.of(ResearchdItems.RESEARCH_PACK.toStack()));
    }

    @Override
    public void onRuntimeUnavailable() {
        JEICompat.RUNTIME = null;
    }

    @Override
    public @NotNull Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(@NotNull ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(
                ResearchdItems.RESEARCH_PACK.get(), new ResearchPackSubtypeInterpreter());
    }

    // The creative tab lists these too, but only if it was built after the packs were synced
    @Override
    public void registerExtraIngredients(@NotNull IExtraIngredientRegistration registration) {
        registration.addExtraItemStacks(ResearchPackListing.CLIENT.stacks());
    }
}

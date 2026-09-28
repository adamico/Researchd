package com.portingdeadmods.researchd.compat;

import com.portingdeadmods.researchd.impl.research.ResearchPackListing;
import java.util.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class JEICompat {
    public static IJeiRuntime RUNTIME;

    // TODO(26.1 port, 14): open recipes by id again; Recipe#getResultItem is gone on 26.1
    public static void openRecipesFor(List<ItemStack> results) {
        if (RUNTIME != null) {
            IFocusFactory focusFactory = RUNTIME.getJeiHelpers().getFocusFactory();
            IIngredientManager ingredientManager = RUNTIME.getIngredientManager();
            Map<Item, IFocus<?>> focuses = new HashMap<>();
            for (ItemStack result : results) {
                Optional<ITypedIngredient<ItemStack>> ingredient =
                        ingredientManager.createTypedIngredient(result, false);
                //noinspection OptionalIsPresent
                if (ingredient.isPresent()) {
                    focuses.put(
                            result.getItem(), focusFactory.createFocus(RecipeIngredientRole.OUTPUT, ingredient.get()));
                }
            }
            IRecipesGui recipesGui = RUNTIME.getRecipesGui();
            recipesGui.show(List.copyOf(focuses.values()));
        }
    }

    /**
     * Adds and removes Research Pack entries. Before JEI's runtime starts it lists the packs at registration. With JEI's
     * optional async start, a change landing between registration and the runtime is missed until JEI next restarts.
     */
    public static void updateResearchPacks(ResearchPackListing.Change change) {
        if (RUNTIME == null) return;

        IIngredientManager ingredientManager = RUNTIME.getIngredientManager();
        if (!change.removed().isEmpty()) {
            ingredientManager.removeIngredientsAtRuntime(
                    VanillaTypes.ITEM_STACK, ResearchPackListing.stacks(change.removed()));
        }
        if (!change.added().isEmpty()) {
            ingredientManager.addIngredientsAtRuntime(
                    VanillaTypes.ITEM_STACK, ResearchPackListing.stacks(change.added()));
        }
    }

    public static Collection<ItemStack> getItems() {
        return RUNTIME.getIngredientManager().getAllItemStacks();
    }
}

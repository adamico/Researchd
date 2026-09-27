package com.portingdeadmods.researchd.mixins;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.portingdeadmods.researchd.api.RecipeFilterContext;
import java.util.Collection;
import java.util.stream.Stream;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Drops recipes that are Blocked for the current Team Context from recipe lookups and recipe lists.
 * <p>
 * Every lookup by input goes through {@link RecipeMap#getRecipesFor}, which lists the matching recipes in priority
 * order, so a Blocked match falls through to the next one. Its matches are checked against the input they would
 * craft from; {@link RecipeMap#byType} lists have no input to go by.
 */
@Mixin(RecipeMap.class)
public abstract class RecipeMapMixin {
    @Shadow
    private Multimap<RecipeType<?>, RecipeHolder<?>> byType;

    @ModifyReturnValue(method = "byType", at = @At("RETURN"))
    private <T extends Recipe<?>> Collection<RecipeHolder<T>> researchd$filterList(
            Collection<RecipeHolder<T>> recipes) {
        RecipeFilterContext.Frame frame = RecipeFilterContext.current();
        if (frame == null) return recipes;
        return recipes.stream()
                .filter(holder -> !RecipeFilterContext.isBlocked(holder, frame))
                .toList();
    }

    /** Searches the unfiltered list: each match is checked against its input below, which is cheaper and stricter. */
    @SuppressWarnings("unchecked")
    @WrapOperation(
            method = "getRecipesFor",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/item/crafting/RecipeMap;byType(Lnet/minecraft/world/item/crafting/RecipeType;)Ljava/util/Collection;"))
    private <T extends Recipe<?>> Collection<RecipeHolder<T>> researchd$searchUnfiltered(
            RecipeMap map, RecipeType<T> type, Operation<Collection<RecipeHolder<T>>> original) {
        return (Collection<RecipeHolder<T>>) (Collection<?>) this.byType.get(type);
    }

    @ModifyReturnValue(method = "getRecipesFor", at = @At("RETURN"))
    private <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> researchd$filterMatches(
            Stream<RecipeHolder<T>> matches, RecipeType<T> type, I input) {
        RecipeFilterContext.Frame frame = RecipeFilterContext.current();
        if (frame == null) return matches;
        return matches.filter(holder -> !RecipeFilterContext.isBlocked(holder, frame, input));
    }
}

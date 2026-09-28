package com.portingdeadmods.researchd.api;

import com.portingdeadmods.portingdeadlibs.utils.PlayerUtils;
import com.portingdeadmods.researchd.api.research.RegistryDisplay;
import com.portingdeadmods.researchd.api.research.Research;
import com.portingdeadmods.researchd.api.research.ResearchInstance;
import com.portingdeadmods.researchd.api.research.ResearchManager;
import com.portingdeadmods.researchd.api.research.effects.ResearchEffect;
import com.portingdeadmods.researchd.api.research.effects.ResearchEffectData;
import com.portingdeadmods.researchd.api.research.effects.ResearchEffectList;
import com.portingdeadmods.researchd.api.research.effects.ResearchEffectManager;
import com.portingdeadmods.researchd.api.research.serializers.ResearchEffectDataType;
import com.portingdeadmods.researchd.api.team.ResearchTeam;
import com.portingdeadmods.researchd.api.team.ResearchTeamManager;
import com.portingdeadmods.researchd.client.ClientResearchdApi;
import com.portingdeadmods.researchd.client.cache.ResearchTeamCache;
import com.portingdeadmods.researchd.data.ResearchdAttachments;
import com.portingdeadmods.researchd.data.saved.TeamResearchEffectSavedData;
import com.portingdeadmods.researchd.data.saved.TeamSavedData;
import com.portingdeadmods.researchd.impl.research.ResearchManagerImpl;
import com.portingdeadmods.researchd.impl.research.effect.RecipeUnlockEffect;
import com.portingdeadmods.researchd.impl.research.effect.data.DimensionUnlockEffectData;
import com.portingdeadmods.researchd.impl.research.effect.data.ItemUnlockEffectData;
import com.portingdeadmods.researchd.impl.research.effect.data.RecipeUnlockEffectData;
import com.portingdeadmods.researchd.registries.ResearchdEffectDataTypes;
import com.portingdeadmods.researchd.utils.registries.ResearchdManagers;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

public final class ResearchdApi {
    /* Research Screen Api */
    public static void openScreen() {
        if (FMLEnvironment.getDist().isClient()) {
            ClientResearchdApi.openResearchScreen();
        }
    }

    public static void openTeamScreen() {
        if (FMLEnvironment.getDist().isClient()) {
            ClientResearchdApi.openTeamScreen();
        }
    }

    public static void openScreenForResearch(ResourceKey<Research> research) {
        if (FMLEnvironment.getDist().isClient()) {
            ClientResearchdApi.openScreenForResearch(research);
        }
    }

    /* Research Team Api */
    public static @Nullable ResearchTeamManager getTeamManager(Level level) {
        if (!level.isClientSide()) {
            return TeamSavedData.getData((ServerLevel) level);
        }
        return ResearchTeamCache.researchTeamMap;
    }

    /* Research Api */
    public static @Nullable ResearchManager getResearchManager() {
        return ResearchManagerImpl.getInstance();
    }

    public static ResearchEffectManager getResearchEffectManager(Level level) {
        if (!level.isClientSide()) {
            return TeamResearchEffectSavedData.getData((ServerLevel) level);
        }
        return ResearchTeamCache.teamResearchEffectDataMap;
    }

    /* Research Effect Data Api */
    public static <T extends ResearchEffectData<?>> @Nullable T getEffectDataForPlayer(
            Player player, ResearchEffectDataType<T> type) {
        ResearchTeamManager teamManager = getTeamManager(player.level());
        if (teamManager == null) return null;
        ResearchTeam team = teamManager.getTeamByPlayer(player);
        if (team == null) return null;
        return getEffectDataForTeam(player.level(), team.getId(), type);
    }

    public static <T extends ResearchEffectData<?>> @Nullable T getEffectDataForPlayer(
            Player player, Supplier<ResearchEffectDataType<T>> type) {
        return getEffectDataForPlayer(player, type.get());
    }

    public static <T extends ResearchEffectData<?>> @Nullable T getEffectDataForTeam(
            Level level, UUID teamId, ResearchEffectDataType<T> type) {
        if (teamId == null) return null;
        ResearchEffectManager researchEffectManager = getResearchEffectManager(level);
        if (researchEffectManager == null) return null;
        return researchEffectManager.getEffectData(teamId, type);
    }

    public static <T extends ResearchEffectData<?>> @Nullable T getEffectDataForTeam(
            Level level, UUID teamId, Supplier<ResearchEffectDataType<T>> type) {
        return getEffectDataForTeam(level, teamId, type.get());
    }

    /* Blocked Item / Recipe / Dimension Api */
    public static boolean isItemBlocked(Player player, Item item) {
        ItemUnlockEffectData data = getEffectDataForPlayer(player, ResearchdEffectDataTypes.ITEM_UNLOCK);
        return data != null && data.isBlocked(item);
    }

    public static boolean isItemBlocked(Player player, ItemStack stack) {
        return isItemBlocked(player, stack.getItem());
    }

    public static boolean isItemBlocked(Player player, ItemLike item) {
        return isItemBlocked(player, item.asItem());
    }

    public static boolean isItemBlocked(Player player, ResourceKey<Item> itemKey) {
        ItemUnlockEffectData data = getEffectDataForPlayer(player, ResearchdEffectDataTypes.ITEM_UNLOCK);
        return data != null && data.blockedItems().contains(itemKey);
    }

    public static boolean isItemBlocked(Level level, UUID teamId, Item item) {
        ItemUnlockEffectData data = getEffectDataForTeam(level, teamId, ResearchdEffectDataTypes.ITEM_UNLOCK);
        return data != null && data.isBlocked(item);
    }

    public static boolean isItemBlocked(Level level, UUID teamId, ItemStack stack) {
        return isItemBlocked(level, teamId, stack.getItem());
    }

    public static boolean isItemBlocked(Level level, UUID teamId, ItemLike item) {
        return isItemBlocked(level, teamId, item.asItem());
    }

    public static boolean isItemBlocked(Level level, UUID teamId, ResourceKey<Item> itemKey) {
        ItemUnlockEffectData data = getEffectDataForTeam(level, teamId, ResearchdEffectDataTypes.ITEM_UNLOCK);
        return data != null && data.blockedItems().contains(itemKey);
    }

    /**
     * The researches whose Research Effect unlocks {@code recipeId}, combined effects included, that {@code player}'s
     * team hasn't completed yet, sorted by id. Datapack and KubeJS researches alike. Empty when the player has no team,
     * on the client, or when nothing unlocks the recipe.
     * <p>
     * Takes and returns only vanilla types, so other mods can call it by reflection.
     */
    public static List<ResourceKey<Research>> researchesUnlocking(Player player, ResourceKey<Recipe<?>> recipeId) {
        Level level = player.level();
        if (level.isClientSide()) return List.of();
        ResearchTeamManager teamManager = getTeamManager(level);
        ResearchTeam team = teamManager == null ? null : teamManager.getTeamByPlayer(player);
        if (team == null) return List.of();

        return ResearchdManagers.getResearchesManager(level).getLookup().entrySet().stream()
                .filter(entry -> unlocksRecipe(entry.getValue().researchEffect(), recipeId))
                .map(Map.Entry::getKey)
                .filter(key -> {
                    ResearchInstance instance = team.getResearches().get(key);
                    return instance == null || !instance.isResearched();
                })
                .sorted(Comparator.comparing(key -> key.identifier().toString()))
                .toList();
    }

    private static boolean unlocksRecipe(ResearchEffect effect, ResourceKey<Recipe<?>> recipeId) {
        if (effect instanceof RecipeUnlockEffect unlock) return unlock.recipes().contains(recipeId);
        if (effect instanceof ResearchEffectList list) {
            return list.effects().stream().anyMatch(e -> unlocksRecipe(e, recipeId));
        }
        return false;
    }

    /**
     * The display name of {@code research}, as the research screen shows it, or its lang key when it isn't loaded.
     * Takes and returns only vanilla types, so other mods can call it by reflection.
     */
    @SuppressWarnings("unchecked")
    public static Component researchName(Level level, ResourceKey<Research> research) {
        Research value =
                ResearchdManagers.getResearchesManager(level).getLookup().get(research);
        if (value instanceof RegistryDisplay<?> display) {
            return ((RegistryDisplay<Research>) display).getDisplayName(research);
        }
        return Research.getLangName(research);
    }

    public static boolean isRecipeBlocked(Player player, ResourceKey<Recipe<?>> recipeId) {
        RecipeUnlockEffectData data = getEffectDataForPlayer(player, ResearchdEffectDataTypes.RECIPE_UNLOCK);
        return data != null && data.contains(recipeId);
    }

    public static boolean isRecipeBlocked(Player player, RecipeHolder<?> holder) {
        return isRecipeBlocked(player, holder.id());
    }

    public static boolean isRecipeBlocked(Level level, UUID teamId, ResourceKey<Recipe<?>> recipeId) {
        RecipeUnlockEffectData data = getEffectDataForTeam(level, teamId, ResearchdEffectDataTypes.RECIPE_UNLOCK);
        return data != null && data.contains(recipeId);
    }

    public static boolean isRecipeBlocked(Level level, UUID teamId, RecipeHolder<?> holder) {
        return isRecipeBlocked(level, teamId, holder.id());
    }

    public static boolean isDimensionBlocked(Player player, ResourceKey<DimensionType> dimension) {
        DimensionUnlockEffectData data = getEffectDataForPlayer(player, ResearchdEffectDataTypes.DIMENSION_UNLOCK);
        return data != null && data.blockedDimensions().contains(dimension);
    }

    public static boolean isDimensionBlocked(Level level, UUID teamId, ResourceKey<DimensionType> dimension) {
        DimensionUnlockEffectData data = getEffectDataForTeam(level, teamId, ResearchdEffectDataTypes.DIMENSION_UNLOCK);
        return data != null && data.blockedDimensions().contains(dimension);
    }

    /* Placement Owner Api */
    // Returns the owning team UUID for a block entity, silently migrating legacy player-UUID
    // values to the placer's current team UUID on first read.
    public static @Nullable UUID getOrMigratePlacedByTeam(BlockEntity be, Level level) {
        UUID stored = be.getData(ResearchdAttachments.PLACED_BY_UUID);
        if (stored == null || stored.equals(PlayerUtils.EmptyUUID)) return stored;

        ResearchTeamManager mgr = getTeamManager(level);
        if (mgr == null) return stored;

        if (mgr.getTeamById(stored) != null) return stored;

        ResearchTeam team = mgr.getTeamByPlayerId(stored);
        if (team != null) {
            be.setData(ResearchdAttachments.PLACED_BY_UUID, team.getId());
            be.setChanged();
            return team.getId();
        }
        return stored;
    }
}

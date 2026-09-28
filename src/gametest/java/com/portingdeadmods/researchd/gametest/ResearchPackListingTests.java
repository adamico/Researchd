package com.portingdeadmods.researchd.gametest;

import com.portingdeadmods.researchd.Researchd;
import com.portingdeadmods.researchd.ResearchdRegistries;
import com.portingdeadmods.researchd.api.research.packs.ResearchPack;
import com.portingdeadmods.researchd.client.ResearchPackViewers;
import com.portingdeadmods.researchd.compat.jei.ResearchPackSubtypeInterpreter;
import com.portingdeadmods.researchd.data.ResearchdDataComponents;
import com.portingdeadmods.researchd.data.components.ResearchPackComponent;
import com.portingdeadmods.researchd.impl.research.ResearchPackImpl;
import com.portingdeadmods.researchd.impl.research.ResearchPackListing;
import com.portingdeadmods.researchd.networking.registries.UpdateResearchPacksPayload;
import com.portingdeadmods.researchd.registries.ResearchdItems;
import com.portingdeadmods.researchd.utils.registries.ReloadableRegistryManager;
import com.portingdeadmods.researchd.utils.registries.ResearchdManagers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * JEI and EMI list one entry per Research Pack. The client's listing follows each {@link UpdateResearchPacksPayload},
 * and JEI's subtype interpreter tells the packs apart. The KubeJS pack comes from
 * {@code src/gametest/kubejs/server_scripts/researchd_gametest.js}.
 */
@EventBusSubscriber(modid = Researchd.MODID)
public final class ResearchPackListingTests {
    private static final ResourceKey<ResearchPack> KUBEJS_PACK =
            ResourceKey.create(ResearchdRegistries.RESEARCH_PACK_KEY, Identifier.parse("researchd_kjs_test:pack"));

    private static final String NAME = "research_pack_listing";
    private static final int MAX_TICKS = 20;

    private static final List<GameTestCase> TESTS = List.of(
            test("subtype_separates_packs", ResearchPackListingTests::subtypeSeparatesPacks),
            test("empty_pack_has_no_subtype", ResearchPackListingTests::emptyPackHasNoSubtype),
            test("payload_lists_a_pack_added_later", ResearchPackListingTests::payloadListsAPackAddedLater),
            test("payload_unlists_a_removed_pack", ResearchPackListingTests::payloadUnlistsARemovedPack),
            test("listing_never_holds_the_bare_pack", ResearchPackListingTests::listingNeverHoldsTheBarePack),
            test("change_rebuilds_the_creative_tab", ResearchPackListingTests::changeRebuildsTheCreativeTab));

    private static GameTestCase test(String name, Consumer<GameTestHelper> function) {
        return new GameTestCase(NAME + "/" + name, MAX_TICKS, function);
    }

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        GameTestCase.registerFunctions(event, TESTS);
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment =
                event.registerEnvironment(Researchd.rl(NAME), new TestEnvironmentDefinition.AllOf(List.of()));
        GameTestCase.registerInstances(event, environment, TESTS);
    }

    private static void subtypeSeparatesPacks(GameTestHelper helper) {
        List<ResourceKey<ResearchPack>> packs = List.copyOf(serverPacks(helper).keySet());
        helper.assertTrue(packs.size() >= 2, "need two packs, the server has " + packs);
        ResourceKey<ResearchPack> first = packs.get(0);
        ResourceKey<ResearchPack> second = packs.get(1);

        Object firstSubtype = subtype(ResearchPackImpl.asStack(first));
        helper.assertTrue(firstSubtype != null, first.identifier() + " has no subtype");
        helper.assertValueEqual(firstSubtype, subtype(ResearchPackImpl.asStack(first)), "the same pack's subtype");
        helper.assertTrue(
                !Objects.equals(firstSubtype, subtype(ResearchPackImpl.asStack(second))),
                first.identifier() + " and " + second.identifier() + " share a subtype");
        helper.succeed();
    }

    private static void emptyPackHasNoSubtype(GameTestHelper helper) {
        ItemStack empty = ResearchdItems.RESEARCH_PACK.toStack();
        empty.set(ResearchdDataComponents.RESEARCH_PACK.get(), ResearchPackComponent.EMPTY);
        helper.assertTrue(subtype(empty) == null, "the empty pack has subtype " + subtype(empty));
        Object bare = subtype(ResearchdItems.RESEARCH_PACK.toStack());
        helper.assertTrue(bare == null, "the bare pack has subtype " + bare);
        helper.succeed();
    }

    private static void payloadListsAPackAddedLater(GameTestHelper helper) {
        ReloadableRegistryManager<ResearchPack> manager = clientManager(helper);
        ResearchPackListing listing = new ResearchPackListing();
        Map<Identifier, ResearchPack> withoutKubeJS = withoutKubeJSPack(helper);

        ResearchPackListing.Change first = UpdateResearchPacksPayload.apply(manager, listing, withoutKubeJS);
        helper.assertValueEqual(withoutKubeJS.size(), first.added().size(), "packs added by the first sync");
        helper.assertTrue(!listed(listing, KUBEJS_PACK), "the KubeJS pack is listed before it's synced");

        ResearchPackListing.Change second = UpdateResearchPacksPayload.apply(manager, listing, byName(helper));
        helper.assertValueEqual(List.of(KUBEJS_PACK), second.added(), "packs added by the second sync");
        helper.assertValueEqual(List.of(), second.removed(), "packs removed by the second sync");
        helper.assertTrue(listed(listing, KUBEJS_PACK), "the KubeJS pack isn't listed after it's synced");
        helper.succeed();
    }

    private static void payloadUnlistsARemovedPack(GameTestHelper helper) {
        ReloadableRegistryManager<ResearchPack> manager = clientManager(helper);
        ResearchPackListing listing = new ResearchPackListing();
        UpdateResearchPacksPayload.apply(manager, listing, byName(helper));

        ResearchPackListing.Change change = UpdateResearchPacksPayload.apply(manager, listing, withoutKubeJSPack(helper));
        helper.assertValueEqual(List.of(), change.added(), "packs added");
        helper.assertValueEqual(List.of(KUBEJS_PACK), change.removed(), "packs removed");
        helper.assertTrue(!listed(listing, KUBEJS_PACK), "the removed KubeJS pack is still listed");
        helper.succeed();
    }

    private static void listingNeverHoldsTheBarePack(GameTestHelper helper) {
        ResearchPackListing listing = new ResearchPackListing();
        UpdateResearchPacksPayload.apply(clientManager(helper), listing, byName(helper));

        List<ItemStack> stacks = listing.stacks();
        helper.assertValueEqual(serverPacks(helper).size(), stacks.size(), "listed stacks");
        for (ItemStack stack : stacks) {
            helper.assertTrue(stack.is(ResearchdItems.RESEARCH_PACK.get()), stack + " isn't a Research Pack");
            ResearchPackComponent component = stack.get(ResearchdDataComponents.RESEARCH_PACK.get());
            helper.assertTrue(
                    component != null && component.researchPackKey().isPresent(), stack + " is the bare pack");
        }
        helper.succeed();
    }

    // JEI is loaded here but its runtime never starts on a server, so this also covers a change with no JEI runtime
    private static void changeRebuildsTheCreativeTab(GameTestHelper helper) {
        CreativeModeTab.ItemDisplayParameters before = CreativeModeTabs.CACHED_PARAMETERS;
        try {
            CreativeModeTab.ItemDisplayParameters built = new CreativeModeTab.ItemDisplayParameters(
                    FeatureFlagSet.of(), false, helper.getLevel().registryAccess());
            CreativeModeTabs.CACHED_PARAMETERS = built;
            ResearchPackViewers.refresh(new ResearchPackListing.Change(List.of(), List.of()));
            helper.assertTrue(
                    CreativeModeTabs.CACHED_PARAMETERS == built, "an empty change made the creative tabs rebuild");

            ResearchPackViewers.refresh(new ResearchPackListing.Change(List.of(KUBEJS_PACK), List.of()));
            helper.assertTrue(
                    CreativeModeTabs.CACHED_PARAMETERS == null, "adding a pack didn't make the creative tabs rebuild");
        } finally {
            CreativeModeTabs.CACHED_PARAMETERS = before;
        }
        helper.succeed();
    }

    private static Object subtype(ItemStack stack) {
        return new ResearchPackSubtypeInterpreter().getSubtypeData(stack, UidContext.Ingredient);
    }

    private static boolean listed(ResearchPackListing listing, ResourceKey<ResearchPack> pack) {
        return listing.stacks().stream()
                .map(stack -> stack.get(ResearchdDataComponents.RESEARCH_PACK.get()))
                .anyMatch(component -> component != null && component.researchPackKey().equals(Optional.of(pack)));
    }

    /** A manager like the one each client level starts with, before any payload. */
    private static ReloadableRegistryManager<ResearchPack> clientManager(GameTestHelper helper) {
        return new ReloadableRegistryManager<>(
                helper.getLevel().registryAccess(), ResearchdRegistries.RESEARCH_PACK_KEY, ResearchPack.CODEC);
    }

    private static Map<ResourceKey<ResearchPack>, ResearchPack> serverPacks(GameTestHelper helper) {
        return ResearchdManagers.getResearchPacksManager(helper.getLevel()).getLookup();
    }

    private static Map<Identifier, ResearchPack> byName(GameTestHelper helper) {
        Map<Identifier, ResearchPack> packs = new HashMap<>(
                ResearchdManagers.getResearchPacksManager(helper.getLevel()).getByName());
        helper.assertTrue(packs.containsKey(KUBEJS_PACK.identifier()), "the KubeJS pack isn't loaded");
        return packs;
    }

    private static Map<Identifier, ResearchPack> withoutKubeJSPack(GameTestHelper helper) {
        Map<Identifier, ResearchPack> packs = byName(helper);
        packs.remove(KUBEJS_PACK.identifier());
        return packs;
    }
}

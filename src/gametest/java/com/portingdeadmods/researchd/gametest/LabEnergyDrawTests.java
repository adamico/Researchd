package com.portingdeadmods.researchd.gametest;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.portingdeadmods.researchd.Researchd;
import com.portingdeadmods.researchd.ResearchdConfig;
import com.portingdeadmods.researchd.ResearchdRegistries;
import com.portingdeadmods.researchd.api.research.Research;
import com.portingdeadmods.researchd.api.research.packs.ResearchPack;
import com.portingdeadmods.researchd.api.team.ResearchTeam;
import com.portingdeadmods.researchd.impl.research.ResearchPackImpl;
import com.portingdeadmods.researchd.resources.contents.ResearchdResearchPacks;
import com.portingdeadmods.researchd.resources.contents.ResearchdResearches;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Lab Energy Draw scenarios. Each test builds a Research Lab the way a player would, gives a fresh team a
 * current research, feeds the Lab through a Lab Part and checks only what a player could observe.
 * <p>
 * The draw is a global config value, so each draw is a test environment, which makes it a batch of its own.
 * Batches run one at a time; the environment sets the draw before the batch starts and restores it once it ends.
 */
@EventBusSubscriber(modid = Researchd.MODID)
public final class LabEnergyDrawTests {
    private static final int DRAW = 10;
    private static final int PACKS = 5;
    /** Duration of one pack for the {@code nether} research in the default datapack. */
    private static final int PACK_DURATION = 100;
    /** The Lab takes no packs before its first tick, see {@link TestLab}. */
    private static final int STOCK_TICK = 1;
    /** GameTest's default timeout on 1.21.1. */
    private static final int MAX_TICKS = 100;

    private static final ResourceKey<Research> RESEARCH =
            ResourceKey.create(ResearchdRegistries.RESEARCH_KEY, ResearchdResearches.NETHER_LOC);
    private static final ResourceKey<ResearchPack> PACK =
            ResourceKey.create(ResearchdRegistries.RESEARCH_PACK_KEY, ResearchdResearchPacks.OVERWORLD_PACK_LOC);

    private static final String NAME = "lab_energy_draw";

    private static final List<GameTestCase> DRAW_OFF = List.of(
            test("draw_zero_progresses_and_ignores_energy", LabEnergyDrawTests::drawZeroProgressesAndIgnoresEnergy),
            test("empty_lab_does_not_stall_a_stocked_one", LabEnergyDrawTests::emptyLabDoesNotStallAStockedOne),
            test("two_stocked_labs_research_twice_as_fast", LabEnergyDrawTests::twoStockedLabsResearchTwiceAsFast));
    private static final List<GameTestCase> DRAW_ON = List.of(
            test("empty_buffer_stalls_research", LabEnergyDrawTests::emptyBufferStallsResearch),
            test("less_than_one_tick_stalls_research", LabEnergyDrawTests::lessThanOneTickStallsResearch),
            test(
                    "fully_powered_draws_one_tick_per_tick_of_progress",
                    300,
                    LabEnergyDrawTests::fullyPoweredDrawsOneTickPerTickOfProgress),
            test(
                    "lab_parts_accept_energy_but_do_not_give_it_back",
                    LabEnergyDrawTests::labPartsAcceptEnergyButDoNotGiveItBack));

    private static GameTestCase test(String name, Consumer<GameTestHelper> function) {
        return test(name, MAX_TICKS, function);
    }

    private static GameTestCase test(String name, int maxTicks, Consumer<GameTestHelper> function) {
        return new GameTestCase(NAME + "/" + name, maxTicks, function);
    }

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        event.register(
                Registries.TEST_ENVIRONMENT_DEFINITION_TYPE,
                registry -> registry.register(Researchd.rl(NAME), DrawEnvironment.CODEC));
        GameTestCase.registerFunctions(event, DRAW_OFF);
        GameTestCase.registerFunctions(event, DRAW_ON);
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> drawOff =
                event.registerEnvironment(Researchd.rl(NAME + "_off"), new DrawEnvironment(0));
        Holder<TestEnvironmentDefinition<?>> drawOn =
                event.registerEnvironment(Researchd.rl(NAME + "_on"), new DrawEnvironment(DRAW));
        GameTestCase.registerInstances(event, drawOff, DRAW_OFF);
        GameTestCase.registerInstances(event, drawOn, DRAW_ON);
    }

    private static void drawZeroProgressesAndIgnoresEnergy(GameTestHelper helper) {
        Scenario scenario = Scenario.build(helper);
        helper.runAtTickTime(STOCK_TICK, () -> {
            scenario.stockPacks(helper);
            scenario.lab().insertEnergy(1000);
        });

        helper.runAtTickTime(50, () -> {
            helper.assertTrue(scenario.progress() > 0, "Lab should progress with the draw off");
            helper.assertValueEqual(PACKS - 1, scenario.lab().itemCount(), "packs left");
            helper.assertValueEqual(1000, scenario.lab().energyStored(), "energy stored");
            helper.succeed();
        });
    }

    // Two Labs for one team, side by side in the 7x7x7 structure
    private static final BlockPos LEFT_LAB = new BlockPos(1, 1, 3);
    private static final BlockPos RIGHT_LAB = new BlockPos(5, 1, 3);
    private static final int ONE_LAB_TICKS = 50;

    private static void emptyLabDoesNotStallAStockedOne(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ResearchTeam team = TestTeams.create(helper, player);
        TestTeams.queue(helper, team, RESEARCH);
        TestLab stocked = TestLab.place(helper, player, LEFT_LAB, 1);
        TestLab.place(helper, player, RIGHT_LAB, 1);
        helper.runAtTickTime(STOCK_TICK, () -> stocked.insert(helper, ResearchPackImpl.asStack(PACK).copyWithCount(PACKS)));
        helper.runAtTickTime(STOCK_TICK + ONE_LAB_TICKS, () -> {
            float progress = team.getResearchProgresses().get(RESEARCH).getProgress();
            helper.assertTrue(
                    progress >= (ONE_LAB_TICKS - 2f) / PACK_DURATION,
                    "a stocked Lab beside an empty one made " + progress + " progress in " + ONE_LAB_TICKS + " ticks");
            helper.succeed();
        });
    }

    private static void twoStockedLabsResearchTwiceAsFast(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ResearchTeam team = TestTeams.create(helper, player);
        TestTeams.queue(helper, team, RESEARCH);
        TestLab left = TestLab.place(helper, player, LEFT_LAB, 1);
        TestLab right = TestLab.place(helper, player, RIGHT_LAB, 1);
        helper.runAtTickTime(STOCK_TICK, () -> {
            left.insert(helper, ResearchPackImpl.asStack(PACK).copyWithCount(PACKS));
            right.insert(helper, ResearchPackImpl.asStack(PACK).copyWithCount(PACKS));
        });
        helper.runAtTickTime(STOCK_TICK + ONE_LAB_TICKS, () -> {
            float progress = team.getResearchProgresses().get(RESEARCH).getProgress();
            helper.assertTrue(
                    progress >= 2 * (ONE_LAB_TICKS - 2f) / PACK_DURATION,
                    "two stocked Labs made " + progress + " progress in " + ONE_LAB_TICKS + " ticks");
            helper.succeed();
        });
    }

    private static void emptyBufferStallsResearch(GameTestHelper helper) {
        Scenario scenario = Scenario.build(helper);
        helper.runAtTickTime(STOCK_TICK, () -> scenario.stockPacks(helper));

        helper.runAtTickTime(50, () -> {
            scenario.assertIdle(helper);
            helper.assertValueEqual(0, scenario.lab().energyStored(), "energy stored");
            helper.succeed();
        });
    }

    private static void lessThanOneTickStallsResearch(GameTestHelper helper) {
        Scenario scenario = Scenario.build(helper);
        helper.runAtTickTime(STOCK_TICK, () -> {
            scenario.stockPacks(helper);
            scenario.lab().insertEnergy(DRAW - 1);
        });

        helper.runAtTickTime(50, () -> {
            scenario.assertIdle(helper);
            helper.assertValueEqual(DRAW - 1, scenario.lab().energyStored(), "energy stored");
            helper.succeed();
        });
    }

    /**
     * Stores exactly 150 ticks of draw. The Lab must research for exactly 150 ticks, then stop: that proves one
     * draw is taken per tick of progress. 150 ticks span two packs, so two packs must be used.
     */
    private static void fullyPoweredDrawsOneTickPerTickOfProgress(GameTestHelper helper) {
        int poweredTicks = 150;
        Scenario scenario = Scenario.build(helper);
        helper.runAtTickTime(STOCK_TICK, () -> {
            scenario.stockPacks(helper);
            scenario.lab().insertEnergy(DRAW * poweredTicks);
        });

        helper.runAtTickTime(250, () -> {
            float expected = (float) poweredTicks / PACK_DURATION;
            helper.assertTrue(
                    Math.abs(scenario.progress() - expected) < 1e-3,
                    "progress should be " + expected + " but was " + scenario.progress());
            helper.assertValueEqual(0, scenario.lab().energyStored(), "energy stored");
            helper.assertValueEqual(PACKS - 2, scenario.lab().itemCount(), "packs left");
            helper.succeed();
        });
    }

    private static void labPartsAcceptEnergyButDoNotGiveItBack(GameTestHelper helper) {
        TestLab lab = Scenario.build(helper).lab();

        helper.assertValueEqual(500, lab.insertEnergy(500), "energy accepted");
        helper.assertValueEqual(0, lab.extractEnergy(500), "energy extracted");
        helper.assertValueEqual(500, lab.energyStored(), "energy stored");
        helper.succeed();
    }

    /** A Research Lab placed by a mock player whose fresh team is researching {@link #RESEARCH}. */
    private record Scenario(ResearchTeam team, TestLab lab) {
        static Scenario build(GameTestHelper helper) {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ResearchTeam team = TestTeams.create(helper, player);
            TestTeams.queue(helper, team, RESEARCH);
            return new Scenario(team, TestLab.place(helper, player));
        }

        void stockPacks(GameTestHelper helper) {
            this.lab.insert(helper, ResearchPackImpl.asStack(PACK).copyWithCount(PACKS));
        }

        float progress() {
            return this.team.getResearchProgresses().get(RESEARCH).getProgress();
        }

        void assertIdle(GameTestHelper helper) {
            helper.assertValueEqual(0f, this.progress(), "progress");
            helper.assertValueEqual(PACKS, this.lab.itemCount(), "packs left");
        }
    }

    /** Sets the Lab Energy Draw while a batch runs, then restores the configured value. */
    private record DrawEnvironment(int draw) implements TestEnvironmentDefinition<Integer> {
        static final MapCodec<DrawEnvironment> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("draw").forGetter(DrawEnvironment::draw))
                .apply(instance, DrawEnvironment::new));

        @Override
        public Integer setup(ServerLevel level) {
            int configured = ResearchdConfig.Common.researchLabEnergyUsage;
            ResearchdConfig.Common.researchLabEnergyUsage = this.draw;
            return configured;
        }

        @Override
        public void teardown(ServerLevel level, Integer configured) {
            ResearchdConfig.Common.researchLabEnergyUsage = configured;
        }

        @Override
        public MapCodec<DrawEnvironment> codec() {
            return CODEC;
        }
    }
}

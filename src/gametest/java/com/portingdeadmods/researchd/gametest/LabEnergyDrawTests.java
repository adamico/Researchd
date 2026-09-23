package com.portingdeadmods.researchd.gametest;

import com.portingdeadmods.researchd.Researchd;
import com.portingdeadmods.researchd.ResearchdConfig;
import com.portingdeadmods.researchd.ResearchdRegistries;
import com.portingdeadmods.researchd.api.ResearchdApi;
import com.portingdeadmods.researchd.api.research.Research;
import com.portingdeadmods.researchd.api.research.packs.ResearchPack;
import com.portingdeadmods.researchd.api.team.ResearchTeam;
import com.portingdeadmods.researchd.api.team.ResearchTeamManager;
import com.portingdeadmods.researchd.api.team.ResearchTeamRole;
import com.portingdeadmods.researchd.content.blockentities.ResearchLabControllerBE;
import com.portingdeadmods.researchd.content.blockentities.ResearchLabPartBE;
import com.portingdeadmods.researchd.impl.research.ResearchPackImpl;
import com.portingdeadmods.researchd.registries.ResearchdItems;
import com.portingdeadmods.researchd.resources.contents.ResearchdResearchPacks;
import com.portingdeadmods.researchd.resources.contents.ResearchdResearches;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Lab Energy Draw scenarios. Each test builds a Research Lab the way a player would, gives a fresh team a
 * current research, feeds the Lab through a Lab Part and checks only what a player could observe.
 * <p>
 * The draw is a global config value, so tests are grouped into batches by draw. Batches run one at a time;
 * each batch sets the draw before it starts and restores it once it ends.
 */
@GameTestHolder(Researchd.MODID)
@PrefixGameTestTemplate(false)
public final class LabEnergyDrawTests {
    private static final String TEMPLATE = "empty_7x7x7";
    private static final String DRAW_OFF = "lab_energy_draw_off";
    private static final String DRAW_ON = "lab_energy_draw_on";

    private static final int DRAW = 10;
    private static final int PACKS = 5;
    /** Duration of one pack for the {@code nether} research in the default datapack. */
    private static final int PACK_DURATION = 100;
    /** The controller sets up its pack slots on its first tick, so a Lab can't take packs before then. */
    private static final int STOCK_TICK = 1;

    private static final ResourceKey<Research> RESEARCH =
            ResourceKey.create(ResearchdRegistries.RESEARCH_KEY, ResearchdResearches.NETHER_LOC);
    private static final ResourceKey<ResearchPack> PACK =
            ResourceKey.create(ResearchdRegistries.RESEARCH_PACK_KEY, ResearchdResearchPacks.OVERWORLD_PACK_LOC);

    private static int savedDraw;

    @BeforeBatch(batch = DRAW_OFF)
    public static void drawOff(ServerLevel level) {
        savedDraw = ResearchdConfig.Common.researchLabEnergyUsage;
        ResearchdConfig.Common.researchLabEnergyUsage = 0;
    }

    @AfterBatch(batch = DRAW_OFF)
    public static void restoreAfterDrawOff(ServerLevel level) {
        ResearchdConfig.Common.researchLabEnergyUsage = savedDraw;
    }

    @BeforeBatch(batch = DRAW_ON)
    public static void drawOn(ServerLevel level) {
        savedDraw = ResearchdConfig.Common.researchLabEnergyUsage;
        ResearchdConfig.Common.researchLabEnergyUsage = DRAW;
    }

    @AfterBatch(batch = DRAW_ON)
    public static void restoreAfterDrawOn(ServerLevel level) {
        ResearchdConfig.Common.researchLabEnergyUsage = savedDraw;
    }

    @GameTest(template = TEMPLATE, batch = DRAW_OFF)
    public static void drawZeroProgressesAndIgnoresEnergy(GameTestHelper helper) {
        Lab lab = Lab.build(helper);
        helper.runAtTickTime(STOCK_TICK, () -> {
            lab.insertPacks(PACKS);
            lab.energy().receiveEnergy(1000, false);
        });

        helper.runAtTickTime(50, () -> {
            helper.assertTrue(lab.progress() > 0, "Lab should progress with the draw off");
            helper.assertValueEqual(lab.packsLeft(), PACKS - 1, "packs left");
            helper.assertValueEqual(lab.energy().getEnergyStored(), 1000, "energy stored");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, batch = DRAW_ON)
    public static void emptyBufferBlocksResearch(GameTestHelper helper) {
        Lab lab = Lab.build(helper);
        helper.runAtTickTime(STOCK_TICK, () -> lab.insertPacks(PACKS));

        helper.runAtTickTime(50, () -> {
            lab.assertIdle(helper);
            helper.assertValueEqual(lab.energy().getEnergyStored(), 0, "energy stored");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, batch = DRAW_ON)
    public static void lessThanOneTickBlocksResearch(GameTestHelper helper) {
        Lab lab = Lab.build(helper);
        helper.runAtTickTime(STOCK_TICK, () -> {
            lab.insertPacks(PACKS);
            lab.energy().receiveEnergy(DRAW - 1, false);
        });

        helper.runAtTickTime(50, () -> {
            lab.assertIdle(helper);
            helper.assertValueEqual(lab.energy().getEnergyStored(), DRAW - 1, "energy stored");
            helper.succeed();
        });
    }

    /**
     * Stores exactly 150 ticks of draw. The Lab must research for exactly 150 ticks, then stop: that proves one
     * draw is taken per tick of progress. 150 ticks span two packs, so two packs must be used.
     */
    @GameTest(template = TEMPLATE, batch = DRAW_ON, timeoutTicks = 300)
    public static void fullyPoweredDrawsOneTickPerTickOfProgress(GameTestHelper helper) {
        int poweredTicks = 150;
        Lab lab = Lab.build(helper);
        helper.runAtTickTime(STOCK_TICK, () -> {
            lab.insertPacks(PACKS);
            lab.energy().receiveEnergy(DRAW * poweredTicks, false);
        });

        helper.runAtTickTime(250, () -> {
            float expected = (float) poweredTicks / PACK_DURATION;
            helper.assertTrue(
                    Math.abs(lab.progress() - expected) < 1e-3,
                    "progress should be " + expected + " but was " + lab.progress());
            helper.assertValueEqual(lab.energy().getEnergyStored(), 0, "energy stored");
            helper.assertValueEqual(lab.packsLeft(), PACKS - 2, "packs left");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, batch = DRAW_ON)
    public static void labPartsAcceptEnergyButDoNotGiveItBack(GameTestHelper helper) {
        Lab lab = Lab.build(helper);

        helper.assertValueEqual(lab.energy().receiveEnergy(500, false), 500, "energy accepted");
        helper.assertValueEqual(lab.energy().extractEnergy(500, false), 0, "energy extracted");
        helper.assertValueEqual(lab.energy().getEnergyStored(), 500, "energy stored");
        helper.succeed();
    }

    /** A Research Lab placed by a mock player whose fresh team is researching {@link #RESEARCH}. */
    private record Lab(ResearchTeam team, IItemHandler items, IEnergyStorage energy) {
        static Lab build(GameTestHelper helper) {
            ServerLevel level = helper.getLevel();
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ResearchTeam team = createTeam(helper, player);

            ItemStack stack = ResearchdItems.RESEARCH_LAB.toStack();
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            BlockPos target = helper.absolutePos(new BlockPos(3, 1, 3));
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(target), Direction.UP, target, false);
            InteractionResult placed = ResearchdItems.RESEARCH_LAB
                    .get()
                    .place(new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, stack, hit));
            helper.assertTrue(placed.consumesAction(), "Research Lab placement returned " + placed);

            // The Lab spans 3x3x3 around the clicked spot. Any Lab Part exposing both handlers will do.
            boolean hasController = false;
            for (BlockPos pos : BlockPos.betweenClosed(target.offset(-3, -3, -3), target.offset(3, 3, 3))) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ResearchLabControllerBE) hasController = true;
                if (!(be instanceof ResearchLabPartBE)) continue;

                IItemHandler items = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
                IEnergyStorage energy = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, Direction.UP);
                if (items != null && energy != null) return new Lab(team, items, energy);
            }
            helper.assertTrue(hasController, "Research Lab was not placed");
            throw new IllegalStateException("No Lab Part exposes items and energy");
        }

        private static ResearchTeam createTeam(GameTestHelper helper, Player player) {
            ResearchTeamManager teams = ResearchdApi.getTeamManager(helper.getLevel());
            ResearchTeam team = teams.createEmptyTeam("Lab Energy Draw " + player.getUUID());
            team.addMember(player.getUUID(), ResearchTeamRole.OWNER);
            team.init(helper.getLevel());
            teams.addTeam(team);

            helper.assertTrue(team.getQueue().add(team.getResearches().get(RESEARCH)), "Could not queue research");
            return team;
        }

        void insertPacks(int count) {
            ItemStack packs = ResearchPackImpl.asStack(PACK).copyWithCount(count);
            ItemStack rest = ItemHandlerHelper.insertItem(this.items, packs, false);
            if (!rest.isEmpty()) throw new IllegalStateException("Lab refused " + rest.getCount() + " packs");
        }

        int packsLeft() {
            int count = 0;
            for (int i = 0; i < this.items.getSlots(); i++) {
                count += this.items.getStackInSlot(i).getCount();
            }
            return count;
        }

        float progress() {
            return this.team.getResearchProgresses().get(RESEARCH).getProgress();
        }

        void assertIdle(GameTestHelper helper) {
            helper.assertValueEqual(this.progress(), 0f, "progress");
            helper.assertValueEqual(this.packsLeft(), PACKS, "packs left");
        }
    }
}

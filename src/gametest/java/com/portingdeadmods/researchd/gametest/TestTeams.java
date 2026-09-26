package com.portingdeadmods.researchd.gametest;

import com.portingdeadmods.researchd.api.ResearchdApi;
import com.portingdeadmods.researchd.api.research.Research;
import com.portingdeadmods.researchd.api.team.ResearchTeam;
import com.portingdeadmods.researchd.api.team.ResearchTeamManager;
import com.portingdeadmods.researchd.api.team.ResearchTeamRole;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

/** Research teams for GameTests. */
public final class TestTeams {
    private TestTeams() {}

    /** A new team owned by {@code player}, with nothing researched and nothing queued. */
    public static ResearchTeam create(GameTestHelper helper, Player player) {
        ResearchTeamManager teams = ResearchdApi.getTeamManager(helper.getLevel());
        ResearchTeam team = teams.createEmptyTeam("GameTest " + player.getUUID());
        team.addMember(player.getUUID(), ResearchTeamRole.OWNER);
        team.init(helper.getLevel());
        teams.addTeam(team);
        return team;
    }

    /** Queues {@code research}, which becomes the team's current research if the queue was empty. */
    public static void queue(GameTestHelper helper, ResearchTeam team, ResourceKey<Research> research) {
        helper.assertTrue(
                team.getQueue().add(team.getResearches().get(research)), "Could not queue " + research.identifier());
    }
}

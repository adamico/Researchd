package com.portingdeadmods.researchd.networking.team.manager;

import com.portingdeadmods.researchd.Researchd;
import com.portingdeadmods.researchd.client.cache.ResearchTeamCache;
import com.portingdeadmods.researchd.impl.team.ResearchTeamImpl;
import com.portingdeadmods.researchd.networking.PayloadSnapshots;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record AddTeamPayload(ResearchTeamImpl team) implements CustomPacketPayload {
    public static final Type<AddTeamPayload> TYPE = new Type<>(Researchd.rl("add_team"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, AddTeamPayload> STREAM_CODEC =
            ResearchTeamImpl.STREAM_CODEC.map(AddTeamPayload::new, AddTeamPayload::team);

    /** Builds the payload from a copy of the live team; see {@link PayloadSnapshots}. */
    public static AddTeamPayload snapshot(ResearchTeamImpl team) {
        return new AddTeamPayload(PayloadSnapshots.copy(ResearchTeamImpl.STREAM_CODEC, team));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
                    ResearchTeamCache.researchTeamMap.addTeam(team);
                })
                .exceptionally(err -> {
                    Researchd.LOGGER.error("Failed to handle RemoveTeamPayload", err);
                    return null;
                });
    }
}

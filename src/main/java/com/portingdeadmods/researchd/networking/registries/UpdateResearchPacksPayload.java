package com.portingdeadmods.researchd.networking.registries;

import com.portingdeadmods.researchd.Researchd;
import com.portingdeadmods.researchd.api.research.packs.ResearchPack;
import com.portingdeadmods.researchd.client.ResearchPackViewers;
import com.portingdeadmods.researchd.impl.research.ResearchPackImpl;
import com.portingdeadmods.researchd.impl.research.ResearchPackListing;
import com.portingdeadmods.researchd.utils.registries.ReloadableRegistryManager;
import com.portingdeadmods.researchd.utils.registries.ResearchdManagers;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateResearchPacksPayload(HashMap<Identifier, ResearchPack> researchPacks)
        implements CustomPacketPayload {
    public static final StreamCodec<? super RegistryFriendlyByteBuf, UpdateResearchPacksPayload> STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, ResearchPackImpl.STREAM_CODEC)
                    .map(UpdateResearchPacksPayload::new, UpdateResearchPacksPayload::researchPacks);
    public static final Type<UpdateResearchPacksPayload> TYPE = new Type<>(Researchd.rl("update_research_packs"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
                    ResearchPackListing.Change change = apply(
                            ResearchdManagers.getResearchPacksManager(
                                    context.player().level()),
                            ResearchPackListing.CLIENT,
                            this.researchPacks);
                    ResearchPackViewers.refresh(change);
                })
                .exceptionally(err -> {
                    Researchd.LOGGER.error("Failed to handle UpdateResearchPacksPayload", err);
                    return null;
                });
    }

    /** Replaces the client's Research Packs with {@code packs} and lists exactly those. */
    public static ResearchPackListing.Change apply(
            ReloadableRegistryManager<ResearchPack> manager,
            ResearchPackListing listing,
            Map<Identifier, ResearchPack> packs) {
        manager.replaceContents(packs);
        return listing.update(manager.getLookup());
    }
}

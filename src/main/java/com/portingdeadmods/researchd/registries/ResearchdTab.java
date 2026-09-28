package com.portingdeadmods.researchd.registries;

import com.portingdeadmods.researchd.Researchd;
import com.portingdeadmods.researchd.ResearchdRegistries;
import com.portingdeadmods.researchd.api.research.packs.ResearchPack;
import com.portingdeadmods.researchd.impl.research.ResearchPackListing;
import com.portingdeadmods.researchd.utils.registries.RegistryManagersGetter;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class ResearchdTab {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Researchd.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN =
            TABS.register("main", () -> CreativeModeTab.builder()
                    .icon(ResearchdItems.GREEN_RESEARCH_PACK_ICON::toStack)
                    .title(Component.literal(Researchd.MODNAME))
                    .displayItems((params, output) -> {
                        output.accept(ResearchdItems.RESEARCH_LAB.toStack());
                        if (FMLEnvironment.getDist().isClient()) {
                            ResearchPackListing.CLIENT.stacks().forEach(output::accept);
                        } else {
                            ResearchPackListing.stacks(serverPacks(params)).forEach(output::accept);
                        }
                    })
                    .build());

    private static Collection<ResourceKey<ResearchPack>> serverPacks(CreativeModeTab.ItemDisplayParameters params) {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer != null) {
            return ((RegistryManagersGetter) currentServer.getServerResources().managers())
                    .researchd$getResearchPackManager()
                    .getLookup()
                    .keySet();
        }
        return params.holders()
                .lookup(ResearchdRegistries.RESEARCH_PACK_KEY)
                .map(lookup -> lookup.listElements().map(Holder.Reference::key).toList())
                .orElse(Collections.emptyList());
    }
}

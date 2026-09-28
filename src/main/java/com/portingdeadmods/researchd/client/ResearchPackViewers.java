package com.portingdeadmods.researchd.client;

import com.portingdeadmods.researchd.compat.JEICompat;
import com.portingdeadmods.researchd.compat.ResearchdCompatHandler;
import com.portingdeadmods.researchd.impl.research.ResearchPackListing;
import net.minecraft.world.item.CreativeModeTabs;

/** Brings the creative tab and the recipe viewers in line with the client's Research Pack listing. */
public final class ResearchPackViewers {
    public static void refresh(ResearchPackListing.Change change) {
        if (change.isEmpty()) return;

        // The creative screen rebuilds every tab, and its search, the next time it checks these
        CreativeModeTabs.CACHED_PARAMETERS = null;
        if (ResearchdCompatHandler.isJeiLoaded()) {
            JEICompat.updateResearchPacks(change);
        }
    }
}

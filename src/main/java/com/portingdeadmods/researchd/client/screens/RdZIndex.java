package com.portingdeadmods.researchd.client.screens;

/**
 * Every Z offset used for {@code PoseStack#translate}.
 *
 * <p>The 26.1 GUI is 2D and layers elements in draw order (with {@code GuiGraphicsExtractor#nextStratum} for
 * overlays), so the research, lab and team screens no longer use these.
 */
// TODO(26.1 port, 06): the editor still uses these; drop the class once it no longer does.
public final class RdZIndex {
    /** Behind the rest of the screen */
    public static final int EDITOR_SIDEBAR = -1000;

    /** Relative */
    public static final int DROP_DOWN = 10;

    /** Hover overlays on editor list entries and item selectors etc. */
    public static final int EDITOR_HOVER_OVERLAY = 160;

    private RdZIndex() {}
}

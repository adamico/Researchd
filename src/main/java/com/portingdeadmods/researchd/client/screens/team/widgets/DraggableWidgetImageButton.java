package com.portingdeadmods.researchd.client.screens.team.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.portingdeadmods.researchd.client.screens.RdZIndex;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.Identifier;

public class DraggableWidgetImageButton extends ImageButton {
    public DraggableWidgetImageButton(int x, int y, int width, int height, WidgetSprites sprites, OnPress onPress) {
        super(x, y, width, height, sprites, onPress);
    }

    public void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        Identifier resourcelocation = this.sprites.get(this.isActive(), this.isHoveredOrFocused());

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        {
            poseStack.translate(0, 0, RdZIndex.DRAGGABLE_WINDOW);
            guiGraphics.blitSprite(resourcelocation, this.getX(), this.getY(), this.width, this.height);
        }
        poseStack.popPose();
    }
}

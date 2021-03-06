package moe.plushie.rpg_framework.core.client.gui;

import java.io.IOException;

import moe.plushie.rpg_framework.core.client.lib.LibGuiResources;
import moe.plushie.rpg_framework.core.common.inventory.ModContainer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class ModGuiContainer<CONTAINER_TYPE extends ModContainer> extends GuiContainer implements IDialogParent {

    protected static final ResourceLocation TEXTURE_BACKGROUND = new ResourceLocation(LibGuiResources.BACKGROUND);
    protected static final ResourceLocation TEXTURE_BUTTONS = new ResourceLocation(LibGuiResources.BUTTONS);
    protected static final ResourceLocation TEXTURE_ICONS = new ResourceLocation(LibGuiResources.ICONS);

    protected AbstractGuiDialog dialog;
    int oldMouseX;
    int oldMouseY;

    public ModGuiContainer(CONTAINER_TYPE container) {
        super(container);
    }

    @Override
    public void initGui() {
        //this.xSize = 320;
        //this.ySize = 240;
        super.initGui();
        if (isDialogOpen()) {
            dialog.initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        oldMouseX = mouseX;
        oldMouseY = mouseY;
        if (isDialogOpen()) {
            mouseX = mouseY = 0;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!isDialogOpen()) {
            this.renderHoveredToolTip(mouseX, mouseY);
        } else {
            dialog.drawFull(oldMouseX, oldMouseY, partialTicks);
        }
    }

    public CONTAINER_TYPE getContainer() {
        return (CONTAINER_TYPE) inventorySlots;
    }

    public abstract String getName();

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (isDialogOpen()) {
            dialog.mouseClicked(mouseX, mouseY, button);
        } else {
            super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int lastButtonClicked, long timeSinceMouseClick) {
        if (isDialogOpen()) {
            dialog.mouseClickMove(mouseX, mouseY, lastButtonClicked, timeSinceMouseClick);
        } else {
            super.mouseClickMove(mouseX, mouseY, lastButtonClicked, timeSinceMouseClick);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (isDialogOpen()) {
            dialog.mouseMovedOrUp(mouseX, mouseY, state);
        } else {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }
    
    @Override
    public void updateScreen() {
        if (isDialogOpen()) {
            dialog.update();
        }
        super.updateScreen();
    }

    @Override
    protected void keyTyped(char c, int keycode) throws IOException {
        if (isDialogOpen()) {
            dialog.keyTyped(c, keycode);
        } else {
            super.keyTyped(c, keycode);
        }
    }

    @Override
    public void openDialog(AbstractGuiDialog dialog) {
        this.dialog = dialog;
        dialog.initGui();
    }

    @Override
    public boolean isDialogOpen() {
        return dialog != null;
    }

    @Override
    public void closeDialog() {
        this.dialog = null;
    }
}

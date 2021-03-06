package moe.plushie.rpg_framework.loot.client.gui;

import java.util.ArrayList;

import moe.plushie.rpg_framework.api.core.IIdentifier;
import moe.plushie.rpg_framework.api.loot.ILootTablePool;
import moe.plushie.rpg_framework.core.client.gui.AbstractGuiDialog;
import moe.plushie.rpg_framework.core.client.gui.IDialogCallback;
import moe.plushie.rpg_framework.core.client.gui.controls.GuiIconButton;
import moe.plushie.rpg_framework.core.client.gui.controls.GuiLabeledTextField;
import moe.plushie.rpg_framework.core.client.gui.controls.GuiList;
import moe.plushie.rpg_framework.core.client.gui.controls.GuiScrollbar;
import moe.plushie.rpg_framework.core.client.gui.controls.GuiTabPanel;
import moe.plushie.rpg_framework.core.client.gui.controls.GuiTabbed;
import moe.plushie.rpg_framework.core.client.gui.controls.GuiList.GuiListItem;
import moe.plushie.rpg_framework.core.client.gui.controls.GuiList.IGuiListItem;
import moe.plushie.rpg_framework.core.common.IdentifierInt;
import moe.plushie.rpg_framework.core.common.network.PacketHandler;
import moe.plushie.rpg_framework.core.common.network.client.MessageClientRequestSync;
import moe.plushie.rpg_framework.core.common.network.client.MessageClientRequestSync.SyncType;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLootTabPoolEditor extends GuiTabPanel<GuiTabbed> implements IDialogCallback {

    private GuiLabeledTextField textSearchCategories;
    private GuiLabeledTextField textSearchNames;

    private GuiList listCategories;
    private GuiList listNames;

    private GuiScrollbar scrollCategories;
    private GuiScrollbar scrollNames;

    private GuiIconButton buttonAdd;
    private GuiIconButton buttonRemove;
    private GuiIconButton buttonEdit;
    private GuiIconButton buttonRename;

    public GuiLootTabPoolEditor(int tabId, GuiTabbed parent) {
        super(tabId, parent, false);
        textSearchCategories = new GuiLabeledTextField(fontRenderer, x + 5, y + 15, 100, 14);
        textSearchNames = new GuiLabeledTextField(fontRenderer, x + 110, y + 15, 100, 14);

        textSearchCategories.setEmptyLabel("Search categories");
        textSearchNames.setEmptyLabel("Search pools");
    }

    @Override
    public void tabChanged(int tabIndex) {
        if (tabIndex == getTabId()) {
            requestFromServer();
        }

    }

    private void requestFromServer() {
        PacketHandler.NETWORK_WRAPPER.sendToServer(new MessageClientRequestSync(SyncType.LOOT_POOLS));
    }

    public void onGotFromServer(ArrayList<IIdentifier> identifiers, ArrayList<String> names, ArrayList<String> categories) {
        listCategories.clearList();
        listNames.clearList();
        //RpgEconomy.getLogger().info("Got lists " + identifiers.size());
        for (String category : categories) {
            if (!listCategories.contains(category)) {
                listCategories.addListItem(new GuiListItem(category));
            }
        }
        for (int i = 0; i < identifiers.size(); i++) {
            listNames.addListItem(new GuiListItem(names.get(i), String.valueOf(identifiers.get(i).getValue())));
        }
    }

    @Override
    public void initGui(int xPos, int yPos, int width, int height) {
        super.initGui(xPos, yPos, width, height);

        textSearchCategories.x = x + 5;
        textSearchCategories.y = y + 15;
        textSearchNames.x = x + 120;
        textSearchNames.y = y + 15;

        listCategories = new GuiList(x + 5, y + 35, 100, height - 40, 12);
        listNames = new GuiList(x + 120, y + 35, 100, height - 40, 12);

        scrollCategories = new GuiScrollbar(-1, x + 100 + 5, y + 35, 10, height - 40, "", false);
        scrollNames = new GuiScrollbar(-1, x + 210 + 10, y + 35, 10, height - 40, "", false);

        buttonAdd = new GuiIconButton(parent, 0, x + width - 20, y + 20, 16, 16, TEXTURE_BUTTONS);
        buttonAdd.setDrawButtonBackground(false).setIconLocation(208, 176, 16, 16).setHoverText("Add Pool...");

        buttonRemove = new GuiIconButton(parent, -1, x + width - 20, y + 40, 16, 16, TEXTURE_BUTTONS);
        buttonRemove.setDrawButtonBackground(false).setIconLocation(208, 160, 16, 16).setHoverText("Remove Pool...");

        buttonEdit = new GuiIconButton(parent, -1, x + width - 20, y + 60, 16, 16, TEXTURE_BUTTONS);
        buttonEdit.setDrawButtonBackground(false).setIconLocation(208, 144, 16, 16).setHoverText("Edit Pool...");
        
        buttonRename = new GuiIconButton(parent, -1, x + width - 20, y + 80, 16, 16, TEXTURE_BUTTONS);
        buttonRename.setDrawButtonBackground(false).setIconLocation(208, 192, 16, 16).setHoverText("Rename Pool...");

        buttonList.add(scrollCategories);
        buttonList.add(scrollNames);
        buttonList.add(buttonAdd);
        buttonList.add(buttonRemove);
        buttonList.add(buttonEdit);
        buttonList.add(buttonRename);
        // requestFromServer();
    }

    @Override
    public boolean keyTyped(char c, int keycode) {
        if (!isDialogOpen()) {
            if (textSearchCategories.textboxKeyTyped(c, keycode)) {
                return true;
            }
            if (textSearchNames.textboxKeyTyped(c, keycode)) {
                return true;
            }
        }
        return super.keyTyped(c, keycode);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == buttonAdd) {
            openDialog(new GuiLootDialogAdd(parent, "Add Pool", this));
        }
        if (button == buttonRemove) {
            IGuiListItem listItem = listNames.getSelectedListEntry();
            if (listItem != null) {
                IIdentifier identifier = new IdentifierInt(Integer.parseInt(listItem.getTag()));
                openDialog(new GuiLootDialogRemove(parent, "Remove Pool", this, identifier, listItem.getDisplayName()));
            }
        }
        if (button == buttonEdit) {
            IGuiListItem listItem = listNames.getSelectedListEntry();
            if (listItem != null) {
                IIdentifier identifier = new IdentifierInt(Integer.parseInt(listItem.getTag()));
                openDialog(new GuiLootDialogEditPool(parent, "Edit Pool", this, identifier));
            }
        }
        if (button == buttonRename) {
            IGuiListItem listItem = listNames.getSelectedListEntry();
            if (listItem != null) {
                IIdentifier identifier = new IdentifierInt(Integer.parseInt(listItem.getTag()));
                openDialog(new GuiLootDialogRename(parent, "Rename Pool", this, identifier, listItem.getDisplayName(), ""));
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!isDialogOpen()) {
            boolean clicked = false;
            if (textSearchCategories.mouseClicked(mouseX, mouseY, button)) {
                clicked = true;
            }
            if (textSearchNames.mouseClicked(mouseX, mouseY, button)) {
                clicked = true;
            }
            if (listCategories.mouseClicked(mouseX, mouseY, button)) {
                clicked = true;
            }
            if (listNames.mouseClicked(mouseX, mouseY, button)) {
                clicked = true;
            }
            if (textSearchCategories.isFocused() & button == 1) {
                textSearchCategories.setText("");
            }
            if (textSearchNames.isFocused() & button == 1) {
                textSearchNames.setText("");
            }
            if (clicked) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void drawBackgroundLayer(float partialTickTime, int mouseX, int mouseY) {
        if (isDialogOpen()) {
            mouseX = mouseY = 0;
        }
        mc.renderEngine.bindTexture(TEXTURE_BACKGROUND);
        GuiUtils.drawContinuousTexturedBox(x, y, 0, 0, width, height, 64, 64, 5, zLevel);
        listCategories.drawList(mouseX, mouseY, partialTickTime);
        listNames.drawList(mouseX, mouseY, partialTickTime);
        textSearchCategories.drawTextBox();
        textSearchNames.drawTextBox();
        for (int i = 0; i < buttonList.size(); i++) {
            buttonList.get(i).drawButton(mc, mouseX, mouseY, partialTickTime);
        }
        for (int i = 0; i < buttonList.size(); i++) {
            if (buttonList.get(i) instanceof GuiIconButton) {
                ((GuiIconButton) buttonList.get(i)).drawRollover(mc, mouseX, mouseY);
            }
        }
    }

    @Override
    public void dialogResult(AbstractGuiDialog dialog, DialogResult result) {
        closeDialog();
    }
    
    public void gotPoolFromServer(ILootTablePool pool) {
        if (isDialogOpen() && dialog instanceof GuiLootDialogEditPool) {
            ((GuiLootDialogEditPool)dialog).gotPoolFromServer(pool);
        }
    }
}

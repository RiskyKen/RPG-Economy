package moe.plushie.rpgeconomy.shop.client.gui;

import moe.plushie.rpgeconomy.api.core.IItemMatcher;
import moe.plushie.rpgeconomy.api.currency.ICost;
import moe.plushie.rpgeconomy.api.currency.IWallet;
import moe.plushie.rpgeconomy.core.client.gui.AbstractGuiDialog;
import moe.plushie.rpgeconomy.core.client.gui.IDialogCallback;
import moe.plushie.rpgeconomy.core.client.gui.controls.GuiDropDownList;
import moe.plushie.rpgeconomy.core.client.gui.controls.GuiDropDownList.IDropDownListCallback;
import moe.plushie.rpgeconomy.currency.common.Cost;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiShopDialogEditCost extends AbstractGuiDialog implements IDropDownListCallback {

    private GuiButtonExt buttonClose;
    private GuiButtonExt buttonEdit;
    private GuiDropDownList dropDownCostTypes;
    private GuiButtonExt buttonEditType;
    
    private final int slotIndex;
    private ICost cost;
    
    private ICost costNew;
    
    public GuiShopDialogEditCost(GuiScreen parent, String name, IDialogCallback callback, int width, int height, int index, ICost cost) {
        super(parent, name, callback, width, height);
        this.slotIndex = index;
        this.cost = cost;
        this.costNew = cost;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();

        buttonClose = new GuiButtonExt(-1, x + width - 80 - 10, y + height - 30, 80, 20, "Close");
        buttonEdit = new GuiButtonExt(-1, x + width - 160 - 20, y + height - 30, 80, 20, "Edit");
        dropDownCostTypes = new GuiDropDownList(0, x + 10, y + 25, 100, "", this);
        buttonEditType = new GuiButtonExt(-1, x + 120, y + 25, 80, 20, "Edit Cost"); 
        
        String[] costTypes = new String[] { "Free", "Currency", "Items", "Ore Dictionary"};
        for (String type : costTypes) {
            dropDownCostTypes.addListItem(type);
        }

        dropDownCostTypes.setListSelectedIndex(0);
        if (cost != null && cost.hasWalletCost()) {
            dropDownCostTypes.setListSelectedIndex(1);
        }

        if (cost != null && cost.hasItemCost()) {
            dropDownCostTypes.setListSelectedIndex(2);
        }
        
        buttonEditType.enabled = dropDownCostTypes.getListSelectedIndex() > 0;

        buttonList.add(buttonClose);
        buttonList.add(buttonEdit);
        buttonList.add(dropDownCostTypes);
        buttonList.add(buttonEditType);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == buttonClose) {
            returnDialogResult(DialogResult.CANCEL);
        }
        if (button == buttonEdit) {
            returnDialogResult(DialogResult.OK);
        }
        if (button == buttonEditType) {
            if (dropDownCostTypes.getListSelectedIndex() == 1) {
                IWallet wallet = null;
                if (costNew != null) {
                    wallet = costNew.getWalletCost();
                }
                openDialog(new GuiShopDialogEditCostCurrency(parent, "editCurrency", this, 280, 130, wallet));
            }
            if (dropDownCostTypes.getListSelectedIndex() == 2) {
                IItemMatcher[] itemCost = null;
                if (costNew != null) {
                    itemCost = costNew.getItemCost();
                }
                openDialog(new GuiShopDialogEditCostItems(parent, "editItems", this, 190, 80, itemCost));
            }
        }
    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float partialTickTime) {
        super.drawForeground(mouseX, mouseY, partialTickTime);
        String title = "Edit Cost";
        int titleWidth = fontRenderer.getStringWidth(title);
        fontRenderer.drawString(title, x + width / 2 - titleWidth / 2, y + 6, 4210752);
        // drawTitle();

        GlStateManager.color(1F, 1F, 1F, 1F);
        mc.renderEngine.bindTexture(ICONS);
        dropDownCostTypes.drawForeground(mc, mouseX, mouseY, partialTickTime);
    }

    public ICost getCost() {
        return costNew;
    }
    
    public int getSlotIndex() {
        return slotIndex;
    }

    @Override
    public void onDropDownListChanged(GuiDropDownList dropDownList) {
        buttonEditType.enabled = dropDownList.getListSelectedIndex() > 0 & dropDownList.getListSelectedIndex() < 3;
        this.costNew = cost;
        if (dropDownList.getListSelectedIndex() == 0) {
            this.costNew = Cost.NO_COST;
        }
    }
    
    @Override
    public void dialogResult(AbstractGuiDialog dialog, DialogResult result) {
        if (result == DialogResult.CANCEL) {
            closeDialog();
        }
        if (result == DialogResult.OK) {
            if (dialog instanceof GuiShopDialogEditCostCurrency) {
                IWallet wallet = ((GuiShopDialogEditCostCurrency)dialog).getWallet();
                costNew = new Cost(wallet, null);
                closeDialog();
            }
        }
    }
}

package moe.plushie.rpg_framework.core.client.gui.controls;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import moe.plushie.rpg_framework.core.common.lib.LibModInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiList extends Gui {
    
    private static final ResourceLocation texture = new ResourceLocation(LibModInfo.ID.toLowerCase(), "textures/gui/controls/list.png");
    
    /** Local copy of Minecraft */
    protected final Minecraft mc;
    /** Local copy of the font renderer */
    protected final FontRenderer fontRenderer;
    protected final int x;
    protected final int y;
    protected final int width;
    protected final int height;
    protected final int slotHeight;
    public final boolean enabled;
    public boolean visible;
    protected int scrollAmount;
    protected int selectedIndex;
    
    protected List<IGuiListItem> listItems;
    
    public GuiList(int x, int y, int width, int height, int slotHeight) {
        mc = Minecraft.getMinecraft();
        fontRenderer = mc.fontRenderer;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.slotHeight = slotHeight;
        listItems = new ArrayList<IGuiListItem>();
        this.enabled = true;
        this.visible = true;
        selectedIndex  = -1;
    }
    
    public void clearList() {
        listItems.clear();
    }
    
    public void addListItem(IGuiListItem item) {
        listItems.add(item);
    }
    
    public boolean contains(String name) {
        for (int i = 0; i < listItems.size(); i++) {
            if (listItems.get(i).getDisplayName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    public void drawList(int mouseX, int mouseY, float tickTime) {
        if (!this.visible) { return; }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(texture);
        
        GuiUtils.drawContinuousTexturedBox(texture, this.x, this.y, 0, 0, width, height, 11, 11, 1, this.zLevel);
        //this.drawTexturedModalRect(x, y, 0, 0, width, height);
        
        ScaledResolution reso = new ScaledResolution(mc);
        
        double scaleWidth = (double)mc.displayWidth / reso.getScaledWidth_double();
        double scaleHeight = (double)mc.displayHeight / reso.getScaledHeight_double();
        
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) ((x + 1) * scaleWidth),  (mc.displayHeight) - (int)((y + height - 1) * scaleHeight), (int) ((width - 2) * scaleWidth), (int) ((height - 2) * scaleHeight));
        for (int i = 0; i < listItems.size(); i++) {
            int yLocation = y - scrollAmount + 2 + i * slotHeight;
            if (yLocation + 6 >= y & yLocation <= y + height + 1) {
                listItems.get(i).drawListItem(fontRenderer, x + 2, yLocation, mouseX, mouseY, i == selectedIndex, width);
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!this.visible) { return false; }
        for (int i = 0; i < listItems.size(); i++) {
            int yLocation = y - scrollAmount + 2 + i * slotHeight;
            if (mouseY >= y & mouseY <= y + height - 2) {
                if (listItems.get(i).mousePressed(fontRenderer, x + 2, yLocation, mouseX, mouseY, button, width)) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    selectedIndex = i;
                    return true;
                }
            }

        }
        return false;
    }
    
    public void mouseMovedOrUp(int mouseX, int mouseY, int button) {
        if (!this.visible) { return; }
        for (int i = 0; i < listItems.size(); i++) {
            listItems.get(i).mouseReleased(fontRenderer, x, y, mouseX, mouseY, button, width);
        }
    }

    public IGuiListItem getSelectedListEntry() {
        if (selectedIndex >= 0 && selectedIndex < listItems.size()) {
            return this.listItems.get(selectedIndex);
        }
        return null;
    }
    
    public IGuiListItem getListEntry(int index) {
        return this.listItems.get(index);
    }
    
    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }
    
    public void setScrollPercentage(int amount) {
        int listHeight = getTotalListHeight();
        int scrollRange = listHeight - this.height;
        if (scrollRange <= 0) {
            scrollAmount = 0;
            return;
        }
        scrollAmount = (int) (scrollRange / (float)100 * amount);
    }
    
    public void setScrollAmount(int scrollAmount) {
        this.scrollAmount = scrollAmount;
    }
    
    public int getTotalListHeight() {
        return this.listItems.size() * this.slotHeight;
    }
    
    public int getVisibleHeight() {
        return height - 2;
    }
    
    public int getSlotHeight() {
        return slotHeight;
    }

    public int getSize() {
        return this.listItems.size();
    }
    
    public static interface IGuiListItem {
        
        public void drawListItem(FontRenderer fontRenderer, int x, int y, int mouseX, int mouseY, boolean selected, int width);
        
        public boolean mousePressed(FontRenderer fontRenderer, int x, int y, int mouseX, int mouseY, int button, int width);
        
        public void mouseReleased(FontRenderer fontRenderer, int x, int y, int mouseX, int mouseY, int button, int width);

        public String getDisplayName();
        
        public String getTag();
    }
    
    public static class GuiListItem implements IGuiListItem {

        private final String name;
        private final String tag;

        public GuiListItem(String name) {
            this(name, null);
        }
        
        public GuiListItem(String name, String tag) {
            this.name = name;
            this.tag = tag;
        }
        
        @Override
        public void drawListItem(FontRenderer fontRenderer, int x, int y, int mouseX, int mouseY, boolean selected, int width) {
            int colour = 0xCCCCCC;
            boolean hover = isHovering(fontRenderer, x, y, mouseX, mouseY, width);
            if (hover) {
                colour = 0xFFFFFF;
            }
            if (selected) {
                colour = 0xDDDD00;
            }
            if (selected & hover) {
                colour = 0xFFFF00;
            }
            fontRenderer.drawString(getDisplayName(), x, y, colour);
        }

        @Override
        public boolean mousePressed(FontRenderer fontRenderer, int x, int y, int mouseX, int mouseY, int button, int width) {
            return isHovering(fontRenderer, x, y, mouseX, mouseY, width);
        }

        @Override
        public void mouseReleased(FontRenderer fontRenderer, int x, int y, int mouseX, int mouseY, int button, int width) {
        }

        protected boolean isHovering(FontRenderer fontRenderer, int x, int y, int mouseX, int mouseY, int width) {
            return mouseX >= x & mouseY >= y & mouseX <= x + width - 3 & mouseY <= y + 11;
        }

        @Override
        public String getDisplayName() {
            return name;
        }
        
        @Override
        public String getTag() {
            return tag;
        }
    }
}

package moe.plushie.rpg_framework.mail.common;

import moe.plushie.rpg_framework.api.core.IIdentifier;
import moe.plushie.rpg_framework.api.currency.ICost;
import moe.plushie.rpg_framework.api.mail.IMailSystem;
import moe.plushie.rpg_framework.core.RpgEconomy;
import moe.plushie.rpg_framework.core.database.TableMail;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class MailSystem implements IMailSystem, Comparable<IMailSystem> {

	private final IIdentifier identifier;
    private final String name;
    private int characterLimit;
    private ICost messageCost;
    private ICost attachmentCost;
    private int inboxSize;
    private int maxAttachments;

    public MailSystem(IIdentifier identifier, String name) {
    	this.identifier = identifier;
        this.name = name;
    }
    
    @Override
    public IIdentifier getIdentifier() {
    	return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getCharacterLimit() {
        return characterLimit;
    }

    public MailSystem setCharacterLimit(int characterLimit) {
        this.characterLimit = characterLimit;
        return this;
    }

    @Override
    public ICost getMessageCost() {
        return messageCost;
    }

    public MailSystem setMessageCost(ICost messageCost) {
        this.messageCost = messageCost;
        return this;
    }

    @Override
    public ICost getAttachmentCost() {
        return attachmentCost;
    }

    public MailSystem setAttachmentCost(ICost attachmentCost) {
        this.attachmentCost = attachmentCost;
        return this;
    }

    @Override
    public int getInboxSize() {
        return inboxSize;
    }

    public MailSystem setInboxSize(int inboxSize) {
        this.inboxSize = inboxSize;
        return this;
    }

    @Override
    public int getMaxAttachments() {
        return maxAttachments;
    }
    
    @Override
    public int compareTo(IMailSystem o) {
        return name.compareTo(o.getName());
    }

    public MailSystem setMaxAttachments(int maxAttachments) {
        this.maxAttachments = maxAttachments;
        return this;
    }

    public void onClientSendMailMessage(EntityPlayerMP player, MailMessage mailMessage) {
        TableMail.addMessage(mailMessage);
        for (int i = 0; i < mailMessage.getAttachments().size(); i++) {
            ItemStack itemStack = mailMessage.getAttachments().get(i);
            player.entityDropItem(itemStack, player.eyeHeight);
        }
        RpgEconomy.getLogger().info("got mail message from player " + mailMessage);
    }
    
    public void onClientDeleteMessage(EntityPlayerMP player, int messageId) {
        TableMail.deleteMessage(messageId);
    }
}

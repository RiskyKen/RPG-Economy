package moe.plushie.rpgeconomy.api;

import moe.plushie.rpgeconomy.api.bank.IBankManager;
import moe.plushie.rpgeconomy.api.currency.ICurrencyManager;
import moe.plushie.rpgeconomy.api.mail.IMailSystemManager;
import moe.plushie.rpgeconomy.api.shop.IShopManager;
import net.minecraftforge.fml.common.Loader;

public final class RpgEconomyAPI {

    public static final String MOD_ID = "rpg_economy";
    public static final String MOD_API_ID = "rpg_economy_api";
    public static final String MOD_API_VERSION = "0.0";
    
    private static ICurrencyManager currencyManager;
    private static IMailSystemManager mailSystemManager;
    private static IShopManager shopManager;
    private static IBankManager bankManager;
    
    private RpgEconomyAPI() {
    }

    public static boolean isAvailable() {
        return Loader.isModLoaded(MOD_ID);
    }

    public static ICurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    public static IMailSystemManager getMailSystemManager() {
        return mailSystemManager;
    }

    public static IShopManager getShopManager() {
        return shopManager;
    }
    
    public static IBankManager getBankManager() {
        return bankManager;
    }
}

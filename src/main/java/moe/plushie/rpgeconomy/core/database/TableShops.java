package moe.plushie.rpgeconomy.core.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import com.google.gson.JsonArray;

import moe.plushie.rpgeconomy.api.core.IIdentifier;
import moe.plushie.rpgeconomy.api.shop.IShop;
import moe.plushie.rpgeconomy.api.shop.IShop.IShopTab;
import moe.plushie.rpgeconomy.core.common.IdentifierInt;
import moe.plushie.rpgeconomy.core.common.utils.SerializeHelper;
import moe.plushie.rpgeconomy.shop.common.Shop;
import moe.plushie.rpgeconomy.shop.common.serialize.ShopSerializer;

public final class TableShops {

    private TableShops() {
    }

    private static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS shops"
            + "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + "name VARCHAR(80) NOT NULL,"
            + "tabs TEXT NOT NULL,"
            + "last_update DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL)";

    private static void create() {
        SQLiteDriver.executeUpdate(SQL_CREATE_TABLE);
    }

    private static final String SQL_ADD_SHOP = "INSERT INTO shops (id, name, tabs, last_update) VALUES (NULL, ?, ?, CURRENT_TIMESTAMP)";

    public static IShop createNewShop(String name) {
        create();
        IShop shop = null;
        try (Connection conn = SQLiteDriver.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL_ADD_SHOP)) {
            ps.setString(1, name);
            ps.setObject(2, "[]");
            int id = ps.executeUpdate();
            shop = new Shop(new IdentifierInt(id), name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shop;
    }
    
    public static void addNewShop(Shop shop) {
        try (Connection conn = SQLiteDriver.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL_ADD_SHOP)) {
            ps.setString(1, shop.getName());
            ps.setString(2, ShopSerializer.serializeTabs(shop.getTabs(), true).toString());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String SQL_DELETE_SHOP = "DELETE FROM shops WHERE id=?";

    public static void deleteShop(IIdentifier identifier) {
        create();
        try (Connection conn = SQLiteDriver.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL_DELETE_SHOP)) {
            ps.setObject(1, identifier.getValue());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String SQL_GET_SHOP = "SELECT name, tabs FROM shops WHERE id=?";

    public static IShop getShop(IIdentifier identifier) {
        create();
        IShop shop = null;
        try (Connection conn = SQLiteDriver.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL_GET_SHOP)) {
            ps.setObject(1, identifier.getValue());
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                JsonArray tabsJson = SerializeHelper.stringToJson(resultSet.getString("tabs")).getAsJsonArray();
                ArrayList<IShopTab> shopTabs = ShopSerializer.deserializeTabs(tabsJson);
                shop = new Shop(identifier, resultSet.getString("name"), shopTabs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shop;
    }
    
    private static final String SQL_GET_SHOP_LIST = "SELECT id, name, last_update FROM shops";

    public static void getShopList(ArrayList<IIdentifier> identifiers, ArrayList<String> names, ArrayList<Date> dates) {
        create();
        try (Connection conn = SQLiteDriver.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL_GET_SHOP_LIST)) {
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                if (identifiers != null) {
                    identifiers.add(new IdentifierInt(resultSet.getInt("id")));
                }
                if (names != null) {
                    names.add(resultSet.getString("name"));
                }
                if (dates != null) {
                    dates.add(resultSet.getDate("last_update"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String SQL_UPDATE_SHOP = "UPDATE shops SET name=?, tabs=?, last_update=datetime('now') WHERE id=?";

    public static void updateShop(IShop shop) {
        create();
        try (Connection conn = SQLiteDriver.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_SHOP)) {
            ps.setString(1, shop.getName());
            ps.setString(2, ShopSerializer.serializeTabs(shop.getTabs(), true).toString());
            ps.setObject(3, shop.getIdentifier().getValue());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
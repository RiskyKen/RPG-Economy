package moe.plushie.rpg_framework.loot.common.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import moe.plushie.rpg_framework.api.core.IIdentifier;
import moe.plushie.rpg_framework.api.loot.ILootTable;
import moe.plushie.rpg_framework.core.common.IdentifierInt;
import moe.plushie.rpg_framework.core.common.database.DatabaseManager;
import moe.plushie.rpg_framework.core.common.database.DatebaseTable;
import moe.plushie.rpg_framework.core.common.serialize.IdentifierSerialize;
import moe.plushie.rpg_framework.core.common.utils.SerializeHelper;
import moe.plushie.rpg_framework.loot.common.LootTable;

public final class TableLootTables {

    private TableLootTables() {
    }
    
    private static DatebaseTable getDatebaseTable() {
        return DatebaseTable.DATA;
    }
    
    private static Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection(getDatebaseTable());
    }

    private static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS loot_tables"
            + "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + "name VARCHAR(64) NOT NULL,"
            + "category VARCHAR(64) NOT NULL,"
            + "pools TEXT NOT NULL,"
            + "last_update DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL)";

    public static void createTable() {
        try (Connection conn = getConnection(); Statement statement = conn.createStatement()) {
            statement.executeUpdate(SQL_CREATE_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static final String SQL_ADD_LOOT_TABLE = "INSERT INTO loot_tables (id, name, category, pools, last_update) VALUES (NULL, ?, ?, ?, CURRENT_TIMESTAMP)";

    public static ILootTable createNew(String name, String category) {
        createTable();
        ILootTable lootTable = null;
        try (Connection conn = DatabaseManager.getConnection(DatebaseTable.DATA); PreparedStatement ps = conn.prepareStatement(SQL_ADD_LOOT_TABLE)) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setString(3, "[]");
            ps.executeUpdate();
            int row = DatabaseManager.getLastInsertRow(conn);
            lootTable = new LootTable(new IdentifierInt(row), name, category);
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lootTable;
    }

    private static final String SQL_DELETE_LOOT_TABLE = "DELETE FROM loot_tables WHERE id=?";

    public static void delete(IIdentifier identifier) {
        createTable();
        try (Connection conn = DatabaseManager.getConnection(DatebaseTable.DATA); PreparedStatement ps = conn.prepareStatement(SQL_DELETE_LOOT_TABLE)) {
            ps.setObject(1, identifier.getValue());
            ps.executeUpdate();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String SQL_GET_LOOT_TABLE = "SELECT name, category, pools FROM loot_tables WHERE id=?";

    public static ILootTable get(IIdentifier identifier) {
        createTable();
        ILootTable lootTable = null;
        try (Connection conn = DatabaseManager.getConnection(DatebaseTable.DATA); PreparedStatement ps = conn.prepareStatement(SQL_GET_LOOT_TABLE)) {
            ps.setObject(1, identifier.getValue());
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                JsonArray poolsJson = SerializeHelper.stringToJson(resultSet.getString("pools")).getAsJsonArray();
                ArrayList<IIdentifier> lootPools = new ArrayList<IIdentifier>();
                for (JsonElement json : poolsJson) {
                    lootPools.add(IdentifierSerialize.deserializeJson(json));
                }
                lootTable = new LootTable(identifier, resultSet.getString("name"), resultSet.getString("category"), lootPools);
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lootTable;
    }

    private static final String SQL_UPDATE_LOOT_TABLE = "UPDATE loot_tables SET name=?, category=?, pools=?, last_update=datetime('now') WHERE id=?";

    public static void update(ILootTable lootTable) {
        createTable();
        try (Connection conn = DatabaseManager.getConnection(DatebaseTable.DATA); PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_LOOT_TABLE)) {
            ps.setString(1, lootTable.getName());
            ps.setString(2, lootTable.getCategory());
            JsonArray poolsJson = new JsonArray();
            for (IIdentifier identifier : lootTable.getLootPools()) {
                poolsJson.add(IdentifierSerialize.serializeJson(identifier));
            }
            ps.setString(3, poolsJson.toString());
            ps.setObject(4, lootTable.getIdentifier().getValue());
            ps.executeUpdate();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String SQL_GET_LOOT_TABLE_LIST = "SELECT id, name, category, last_update FROM loot_tables";

    public static void getList(ArrayList<IIdentifier> identifiers, ArrayList<String> names, ArrayList<String> categories, ArrayList<Date> dates) {
        createTable();
        try (Connection conn = DatabaseManager.getConnection(DatebaseTable.DATA); PreparedStatement ps = conn.prepareStatement(SQL_GET_LOOT_TABLE_LIST)) {
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                if (identifiers != null) {
                    identifiers.add(new IdentifierInt(resultSet.getInt("id")));
                }
                if (names != null) {
                    names.add(resultSet.getString("name"));
                }
                if (categories != null) {
                    categories.add(resultSet.getString("category"));
                }
                if (dates != null) {
                    dates.add(resultSet.getDate("last_update"));
                }
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

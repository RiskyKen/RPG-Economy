package moe.plushie.rpg_framework.core.common.database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import moe.plushie.rpg_framework.core.RPGFramework;
import moe.plushie.rpg_framework.core.common.config.ConfigStorage;
import moe.plushie.rpg_framework.core.common.config.ConfigStorage.StorageType;
import moe.plushie.rpg_framework.core.common.database.driver.MySqlBuilder;
import moe.plushie.rpg_framework.core.common.database.sql.ISqlBulder;
import moe.plushie.rpg_framework.core.common.database.sql.ISqlBulder.ISqlBulderCreateTable;

public final class TablePlayers {

    private final static String TABLE_NAME = "players";

    private TablePlayers() {
    }

    private static DatebaseTable getDatebaseTable() {
        return DatebaseTable.PLAYER_DATA;
    }

    private static Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection(getDatebaseTable());
    }

    public static void create() {
//        String sql = "CREATE TABLE IF NOT EXISTS players ";
//        sql += "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,";
//        sql += "uuid VARCHAR(36) NOT NULL,";
//        sql += "username VARCHAR(80) NOT NULL,";
//        sql += "first_seen DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,";
//        sql += "last_seen DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL)";
//        try (Connection conn = getConnection(); Statement statement = conn.createStatement()) {
//            RPGFramework.getLogger().info(sql);
//            statement.executeUpdate(sql);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

        try (Connection connection = getConnection()) {
            create(connection, DatabaseManager.getSqlBulder());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void create(Connection connection, ISqlBulder sqlBulder) {
        ISqlBulderCreateTable table = sqlBulder.createTable(TABLE_NAME);
        table.addColumn("id", ISqlBulder.DataType.INT).setUnsigned(true).setNotNull(true).setAutoIncrement(true);
        table.addColumn("uuid", ISqlBulder.DataType.VARCHAR).setSize(36).setNotNull(true);
        table.addColumn("username", ISqlBulder.DataType.VARCHAR).setSize(80).setNotNull(true);
        table.addColumn("first_seen", ISqlBulder.DataType.DATETIME).setNotNull(true).setDefault("CURRENT_TIMESTAMP");
        table.addColumn("last_seen", ISqlBulder.DataType.DATETIME).setNotNull(true).setDefault("CURRENT_TIMESTAMP");
        table.ifNotExists(true);
        table.setPrimaryKey("id");
        try (Statement statement = connection.createStatement()) {
            RPGFramework.getLogger().info(table.build());
            statement.executeUpdate(table.build());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateOrAddPlayer(GameProfile gameProfile) {
        try (Connection conn = getConnection()) {
            DBPlayer dbPlayer = getPlayer(conn, gameProfile);
            if (dbPlayer.isMissing()) {
                addPlayerToDatabase(conn, gameProfile);
            } else {
                updatePlayerLastLogin(conn, gameProfile);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isPlayerInDatabase(GameProfile gameProfile) {
        return !getPlayer(gameProfile).isMissing();
    }

    public static void addPlayerToDatabase(GameProfile gameProfile) {
        try (Connection conn = getConnection()) {
            addPlayerToDatabase(conn, gameProfile);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addPlayerToDatabase(Connection conn, GameProfile gameProfile) {
        String sql = "INSERT INTO players (`id`, `uuid`, `username`, `first_seen`, `last_seen`) VALUES (NULL, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        sql = String.format(sql, gameProfile.getId().toString(), gameProfile.getName());
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gameProfile.getId().toString());
            ps.setString(2, gameProfile.getName());
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updatePlayerLastLogin(GameProfile gameProfile) {
        try (Connection conn = getConnection()) {
            updatePlayerLastLogin(conn, gameProfile);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updatePlayerLastLogin(Connection conn, GameProfile gameProfile) {
        String sql = "UPDATE players SET `username`=?, `last_seen`=datetime('now') WHERE `uuid`=?";
        if (ConfigStorage.getStorageType() == StorageType.MYSQL) {
            sql = "UPDATE players SET `username`=?, `last_seen`=now() WHERE `uuid`=?";
        }

        // last_seen=now()
        sql = String.format(sql, gameProfile.getName(), gameProfile.getId().toString());
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gameProfile.getName());
            ps.setString(2, gameProfile.getId().toString());
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DBPlayerInfo getPlayerInfo(GameProfile gameProfile) {
        String sql = "SELECT * FROM players WHERE ";
        String searchValue;
        if (gameProfile.getId() != null) {
            sql += "uuid=?";
            searchValue = gameProfile.getId().toString();
        } else {
            sql += "username=?";
            searchValue = gameProfile.getName();
        }
        DBPlayerInfo playerInfo = DBPlayerInfo.MISSING_INFO;
        try (Connection conn = DatabaseManager.getConnection(DatebaseTable.PLAYER_DATA); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, searchValue);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                String username = resultSet.getString("username");
                Timestamp firstSeen = resultSet.getTimestamp("first_seen");
                Timestamp lastLogin = resultSet.getTimestamp("last_seen");
                playerInfo = new DBPlayerInfo(id, new GameProfile(uuid, username), firstSeen, lastLogin);
            }
            resultSet.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerInfo;
    }

    public static DBPlayer getPlayer(GameProfile gameProfile) {
        DBPlayer playerInfo = DBPlayer.MISSING;
        try (Connection conn = DatabaseManager.getConnection(DatebaseTable.PLAYER_DATA)) {
            playerInfo = getPlayer(conn, gameProfile);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerInfo;
    }

    public static DBPlayer getPlayer(Connection conn, GameProfile gameProfile) throws SQLException {
        DBPlayer playerInfo = DBPlayer.MISSING;
        if (gameProfile.getId() != null) {
            playerInfo = getPlayerUUID(conn, gameProfile.getId());
        } else {
            playerInfo = getPlayerName(conn, gameProfile.getName());
        }
        return playerInfo;
    }

    private static final String SQL_GET_ALL_PLAYERS = "SELECT uuid, username FROM players";

    public static ArrayList<GameProfile> getAllPlayers() {
        ArrayList<GameProfile> gameProfiles = new ArrayList<GameProfile>();
        try (Connection conn = DatabaseManager.getConnection(DatebaseTable.PLAYER_DATA); PreparedStatement ps = conn.prepareStatement(SQL_GET_ALL_PLAYERS)) {
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                    String username = resultSet.getString("username");
                    gameProfiles.add(new GameProfile(uuid, username));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gameProfiles;
    }

    public static DBPlayer getPlayerName(Connection conn, String username) throws SQLException {
        String sql = "SELECT id FROM players WHERE username=? COLLATE NOCASE";
        if (ConfigStorage.getStorageType() == StorageType.MYSQL) {
            sql = "SELECT id FROM players WHERE username=?";
        }
        DBPlayer playerInfo = DBPlayer.MISSING;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    playerInfo = new DBPlayer(resultSet.getInt("id"));
                }
            }
        }
        return playerInfo;
    }

    private static final String SQL_GET_PLAYER_UUID = "SELECT id FROM players WHERE uuid=?";

    public static DBPlayer getPlayerUUID(Connection conn, UUID uuid) throws SQLException {
        DBPlayer playerInfo = DBPlayer.MISSING;
        try (PreparedStatement ps = conn.prepareStatement(SQL_GET_PLAYER_UUID)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    playerInfo = new DBPlayer(rs.getInt("id"));
                }
            }
        }
        return playerInfo;
    }

    private static final String SQL_GET_PLAYER_ID = "SELECT * FROM players WHERE id=?";

    public static DBPlayerInfo getPlayer(int id) {
        DBPlayerInfo playerInfo = DBPlayerInfo.MISSING_INFO;
        try (Connection conn = DatabaseManager.getConnection(DatebaseTable.PLAYER_DATA)) {
            return getPlayer(conn, id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerInfo;
    }

    public static DBPlayerInfo getPlayer(Connection conn, int id) {
        DBPlayerInfo playerInfo = DBPlayerInfo.MISSING_INFO;
        try (PreparedStatement ps = conn.prepareStatement(SQL_GET_PLAYER_ID)) {
            ps.setInt(1, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                    String username = resultSet.getString("username");
                    Date firstSeen = resultSet.getDate("first_seen");
                    Date lastLogin = resultSet.getDate("last_seen");
                    playerInfo = new DBPlayerInfo(id, new GameProfile(uuid, username), firstSeen, lastLogin);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerInfo;
    }

    public static void importData(ArrayList<DBPlayerInfo> playerInfos, Connection connection, boolean dropTable) {
        if (dropTable) {
            String sqlDrop = "DROP TABLE IF EXISTS players";
            try (PreparedStatement ps = connection.prepareStatement(sqlDrop)) {
                ps.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            create(connection, new MySqlBuilder());
        }

        for (DBPlayerInfo playerInfo : playerInfos) {
            try (PreparedStatement ps = connection.prepareStatement("ALTER TABLE players AUTO_INCREMENT=" + playerInfo.getId())) {
                ps.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String sql = "INSERT INTO players (`id`, `uuid`, `username`, `first_seen`, `last_seen`) VALUES (NULL, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerInfo.getGameProfile().getId().toString());
                ps.setString(2, playerInfo.getGameProfile().getName());
                ps.setObject(3, playerInfo.getFirstSeen());
                ps.setObject(4, playerInfo.getLastLogin());
                ps.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<DBPlayerInfo> exportData(Connection connection) {
        ArrayList<DBPlayerInfo> playerInfos = new ArrayList<DBPlayerInfo>();
        String sql = "SELECT * FROM players";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                    String username = resultSet.getString("username");
                    Date firstSeen = resultSet.getDate("first_seen");
                    Date lastLogin = resultSet.getDate("last_seen");
                    playerInfos.add(new DBPlayerInfo(id, new GameProfile(uuid, username), firstSeen, lastLogin));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerInfos;
    }
}

package moe.plushie.rpg_framework.core.common.command;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.google.common.util.concurrent.FutureCallback;

import moe.plushie.rpg_framework.api.shop.IShop;
import moe.plushie.rpg_framework.bank.common.TableBankAccounts;
import moe.plushie.rpg_framework.bank.common.TableBankAccounts.DBBankAccount;
import moe.plushie.rpg_framework.core.RPGFramework;
import moe.plushie.rpg_framework.core.common.command.CommandExecute.ICommandExecute;
import moe.plushie.rpg_framework.core.common.database.DBPlayerInfo;
import moe.plushie.rpg_framework.core.common.database.DatabaseManager;
import moe.plushie.rpg_framework.core.common.database.DatebaseTable;
import moe.plushie.rpg_framework.core.common.database.TablePlayers;
import moe.plushie.rpg_framework.core.common.database.driver.MySqlDriver;
import moe.plushie.rpg_framework.currency.common.TableWallets;
import moe.plushie.rpg_framework.currency.common.TableWallets.DBWallet;
import moe.plushie.rpg_framework.mail.common.MailMessage;
import moe.plushie.rpg_framework.mail.common.TableMail;
import moe.plushie.rpg_framework.shop.common.TableShops;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class CommandDev extends ModSubCommands {

    public CommandDev(ModCommand parent) {
        super(parent, "dev");
        addSubCommand(new CommandExecute(this, "sql", new ICommandExecute() {

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                if (args.length > 3) {
                    DatebaseTable datebaseTable = DatebaseTable.valueOf(args[2].toUpperCase());
                    String sql = args[3];
                    for (int i = 4; i < args.length; i++) {
                        sql += " " + args[i];
                    }
                    EntityPlayerMP player = getCommandSenderAsPlayer(sender);

                    final String sqla = sql;
                    final ArrayList<String> resultLines = new ArrayList<String>();
                    // int updateCount;

                    DatabaseManager.createTaskAndExecute(new Runnable() {
                        @Override
                        public void run() {
                            try (Connection conn = DatabaseManager.getConnection(datebaseTable); Statement statement = conn.createStatement()) {
                                if (statement.execute(sqla)) {
                                    try (ResultSet rs = statement.getResultSet()) {
                                        while (rs.next()) {
                                            String line = rs.getString(1);
                                            for (int i = 2; i < rs.getMetaData().getColumnCount() + 1; i++) {
                                                line += " - " + rs.getString(i);
                                            }
                                            resultLines.add(line);
                                        }
                                    }
                                } else {
                                    // updateCount = statement.getUpdateCount();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new FutureCallback<Void>() {

                        @Override
                        public void onSuccess(Void result) {
                            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new Runnable() {

                                @Override
                                public void run() {
                                    if (!resultLines.isEmpty()) {
                                        player.sendMessage(new TextComponentString("Query result"));
                                        for (String s : resultLines) {
                                            player.sendMessage(new TextComponentString(s));
                                        }
                                        player.sendMessage(new TextComponentString("End result"));
                                    } else {
                                        // player.sendMessage(new TextComponentString("Update count: " + updateCount));
                                    }

                                }
                            });
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            t.printStackTrace();
                            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new Runnable() {

                                @Override
                                public void run() {
                                    player.sendMessage(new TextComponentString("Query failed"));
                                }
                            });
                        }
                    });
                }
            }
        }));
        addSubCommand(new CommandExecute(this, "make_example_files", new ICommandExecute() {

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                RPGFramework.getProxy().createExampleFiles();
            }
        }));
        addSubCommand(new CommandExecute(this, "export_sqlite_to_mysql", new ICommandExecute() {

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                sender.sendMessage(new TextComponentString("Export started."));
                DatabaseManager.createTaskAndExecute(new Runnable() {

                    @Override
                    public void run() {
                        MySqlDriver mySqlDriver = new MySqlDriver();
                        try (Connection mySqlconn = mySqlDriver.getConnection(null)) {

                            // Players.
                            ArrayList<DBPlayerInfo> playerInfos = TablePlayers.exportData(DatabaseManager.getConnection(DatebaseTable.PLAYER_DATA));
                            TablePlayers.importData(playerInfos, mySqlconn, true);
                            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new Runnable() {

                                @Override
                                public void run() {
                                    sender.sendMessage(new TextComponentString("Exported " + playerInfos.size() + " players."));
                                }
                            });

                            // Shops.
                            ArrayList<IShop> shops = TableShops.exportData(DatabaseManager.getConnection(DatebaseTable.DATA));
                            TableShops.importData(shops, mySqlconn, true);
                            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new Runnable() {

                                @Override
                                public void run() {
                                    sender.sendMessage(new TextComponentString("Exported " + shops.size() + " shops."));
                                }
                            });

                            // Mail messages.
                            ArrayList<MailMessage> mailMessages = TableMail.exportData(DatabaseManager.getConnection(DatebaseTable.PLAYER_DATA));
                            TableMail.importData(mailMessages, mySqlconn, true);
                            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new Runnable() {

                                @Override
                                public void run() {
                                    sender.sendMessage(new TextComponentString("Exported " + mailMessages.size() + " mail messages."));
                                }
                            });

                            // Wallets.
                            ArrayList<DBWallet> wallets = TableWallets.exportData(DatabaseManager.getConnection(DatebaseTable.PLAYER_DATA));
                            TableWallets.importData(wallets, mySqlconn, true);
                            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new Runnable() {

                                @Override
                                public void run() {
                                    sender.sendMessage(new TextComponentString("Exported " + wallets.size() + " wallets."));
                                }
                            });

                            // Banks Accounts
                            ArrayList<DBBankAccount> bankAccounts = TableBankAccounts.exportData(DatabaseManager.getConnection(DatebaseTable.PLAYER_DATA));
                            TableBankAccounts.importData(bankAccounts, mySqlconn, true);
                            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new Runnable() {

                                @Override
                                public void run() {
                                    sender.sendMessage(new TextComponentString("Exported " + bankAccounts.size() + " bank accounts."));
                                    sender.sendMessage(new TextComponentString("Export finished."));
                                }
                            });

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }));
    }
}

package moe.plushie.rpg_framework.stats;

import java.io.File;

import moe.plushie.rpg_framework.core.common.module.ModModule;
import moe.plushie.rpg_framework.stats.common.handler.PlayerStatsHandler;
import moe.plushie.rpg_framework.stats.common.handler.ServerStatsHandler;
import moe.plushie.rpg_framework.stats.common.handler.WorldStatsHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

public class ModuleStats extends ModModule {

    private static PlayerStatsHandler playerStatsHandler;
    private static ServerStatsHandler serverStatsHandler;
    private static WorldStatsHandler worldStatsHandler;

    public ModuleStats(File modDirectory) {
        super("stats");
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        playerStatsHandler = new PlayerStatsHandler();
        serverStatsHandler = new ServerStatsHandler();
        worldStatsHandler = new WorldStatsHandler();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(playerStatsHandler);
        MinecraftForge.EVENT_BUS.register(serverStatsHandler);
        MinecraftForge.EVENT_BUS.register(worldStatsHandler);
    }

    @Override
    public void initRenderers() {
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
    }

    @Override
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
    }

    @Override
    public void serverStarting(FMLServerStartingEvent event) {
    }

    @Override
    public void serverStopping(FMLServerStoppingEvent event) {
    }

    public static PlayerStatsHandler getPlayerStatsHandler() {
        return playerStatsHandler;
    }

    public static ServerStatsHandler getServerStatsHandler() {
        return serverStatsHandler;
    }

    public static WorldStatsHandler getWorldStatsHandler() {
        return worldStatsHandler;
    }
}

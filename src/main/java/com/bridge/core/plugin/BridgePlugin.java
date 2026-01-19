package com.bridge.core.plugin;

import com.bridge.api.utilities.BridgeUtil;
import com.bridge.core.commands.TestCommand;
import com.bridge.core.data.DataManager;
import com.bridge.core.factory.BridgeWorldFactory;
import com.bridge.core.systems.BridgeWorldManagerSystem;
import com.bridge.core.systems.PlayerDeathSystem;
import com.bridge.core.systems.PlayerJoinAndLeaveSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

@Deprecated
public final class BridgePlugin extends JavaPlugin {
    @Nonnull
    private static HytaleLogger logger;

    @Nonnull
    public static HytaleLogger logger() {
        return logger;
    }

    public BridgePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
        logger = getLogger();
        DataManager.init(getDataDirectory());
        registerSystems();
        getCommandRegistry().registerCommand(new TestCommand("test", "An test command"));

        BridgeUtil.getDefaultWorldReady().thenRunAsync(() -> {
            BridgeWorldFactory.load();
        });
    }

    private void registerSystems() {
        getChunkStoreRegistry().registerSystem(new BridgeWorldManagerSystem());
        getEntityStoreRegistry().registerSystem(new PlayerJoinAndLeaveSystem());
        getEntityStoreRegistry().registerSystem(new PlayerDeathSystem());
    }
}
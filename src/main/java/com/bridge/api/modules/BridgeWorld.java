package com.bridge.api.modules;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.bridge.core.plugin.BridgePlugin;
import com.bridge.core.systems.BridgeWorldManagerSystem;
import com.hypixel.hytale.server.core.asset.type.gameplay.WorldConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import java.lang.reflect.Field;

/**
 * Represents a world within the Bridge framework, wrapping the underlying Hypixel Hytale world.
 * This class provides enhanced functionality and easier access to world operations.
 * 
 * <p>A BridgeWorld can be either active (loaded and running) or inactive (unloaded).
 * Each BridgeWorld has an associated {@link BridgeWorldComponent} that provides custom behavior.
 */
public final class BridgeWorld {
    
    /** The display name of this BridgeWorld. */
    public String bWorldName;
    /** Whether this world is currently active and loaded. */
    public boolean active = false;

    private World hWorld;
    private BridgeWorldComponent bWorldComponent;

    public BridgeWorld(@Nonnull String name, @Nullable BridgeWorldComponent bridgeWorldComponent) {
        bWorldName = name;
        if (bridgeWorldComponent != null) {
            bWorldComponent = bridgeWorldComponent;
        }
        else {
            bWorldComponent = new BridgeWorldComponent();
        }
    }

    /**
     * Permanently deletes this world and all associated files.
     * 
     * <p>If the world is currently active, it will be deactivated first.
     * This operation cannot be undone.
     * 
     * @see #deactivate(World)
     * @see BridgeWorldManagerSystem#deleteWorld(BridgeWorld)
     */
    public void delete() {
        if (active)
        {
            deactivate(null);
        }
        BridgeWorldManagerSystem.get().deleteWorld(this);;
    }

    /**
     * Activates this world, loading it into memory and making it available for player interaction.
     * Uses the default callback (null).
     * 
     * @see #activate(Consumer)
     * @see BridgeWorldManagerSystem#activateWorld(BridgeWorld, Consumer)
     */
    public void activate() {
        activate(null);
    }

    /**
     * Activates this world with an optional callback.
     * 
     * <p>Activation loads the world into memory, starts all systems, and makes it ready for players.
     * The callback will be invoked once activation is complete.
     * 
     * @param callback Optional callback to execute after successful activation.
     *                 The callback receives this BridgeWorld instance as a parameter.
     * 
     * @see BridgeWorldManagerSystem#activateWorld(BridgeWorld, Consumer)
     */
    public void activate(@Nullable Consumer<BridgeWorld> callback) {
        BridgeWorldManagerSystem.get().activateWorld(this, callback);
    }

    /**
     * Deactivates this world, unloading it from memory.
     * Players will be moved to the default world.
     * 
     * @return true if deactivation was successful, false otherwise
     * 
     * @see #deactivate(World)
     * @see BridgeWorldManagerSystem#deactivateWorld(BridgeWorld, World)
     */
    public boolean deactivate() {
        return deactivate(null);
    }

    /**
     * Deactivates this world, moving players to the specified destination.
     * 
     * <p>Deactivation stops all world systems, unloads chunks from memory,
     * and moves all players to the destination world.
     * 
     * @param movePlayersTo The world where players should be moved.
     *                      If null, players will be moved to the default world.
     * @return true if deactivation was successful, false otherwise
     * 
     * @see BridgeWorldManagerSystem#deactivateWorld(BridgeWorld, World)
     */
    public boolean deactivate(@Nullable World movePlayersTo) {
        return BridgeWorldManagerSystem.get().deactivateWorld(this, movePlayersTo);
    }

    /**
     * Transfers a player from their current world to this world.
     * 
     * <p>This method safely moves a player between worlds by:
     * 1. Removing the player from their current world
     * 2. Adding them to this world
     * 
     * <p><b>Note:</b> This method executes on the appropriate world threads to ensure thread safety.
     * 
     * @param playerRef The player to transfer. Must not be null.
     * 
     * @throws IllegalArgumentException if playerRef is null
     * @throws IllegalStateException if this world is not active
     * 
     * @see PlayerRef
     */
    public void transferPlayer(@Nonnull PlayerRef playerRef) {
        var currentWorld = Universe.get().getWorld(playerRef.getWorldUuid());
        currentWorld.execute(() -> {
            try {
                playerRef.removeFromStore();
                
                hWorld.execute(() -> {
                    CompletableFuture<PlayerRef> future = hWorld.addPlayer(playerRef);
                    
                    if (future != null) {
                        future.exceptionally(throwable -> {
                            BridgePlugin.logger().atSevere().log(throwable.getMessage());
                            return null;
                        });
                    }
                });
            } catch (Exception e) {
                BridgePlugin.logger().atSevere().log(e.toString());
            }
        });
    }

    /**
     * Sets the underlying Hypixel Hytale world instance.
     * 
     * <p>This is typically called internally by the Bridge framework when a world is
     * created, loaded, or activated.
     * 
     * @param world The Hypixel Hytale world instance to associate with this BridgeWorld.
     * 
     * @see #getWorld()
     */
    public void setWorld(World world) {
        hWorld = world;
    }

    /**
     * Gets the underlying Hypixel Hytale world instance.
     * 
     * @return The associated Hypixel Hytale world, or null if no world is set
     *         (e.g., world hasn't been loaded yet).
     * 
     * @see #setWorld(World)
     */
    @Nullable
    public World getWorld() {
        return hWorld;
    }

    /**
     * Gets the BridgeWorldComponent associated with this world.
     * 
     * @return The component that provides custom behavior for this world.
     *         Never returns null (a default component is created if none was provided).
     * 
     * @see BridgeWorldComponent
     */
    @Nonnull
    public BridgeWorldComponent getBridgeWorldComponent() {
        return bWorldComponent;
    }

    /**
     * Enables or disables block breaking in this world.
     * 
     * @param value true to allow block breaking, false to disallow it
     * 
     * @throws IllegalStateException if the world is not set or the field cannot be accessed
     * 
     * @see #setAllowBlockPlacement(boolean)
     * @see #setAllowBlockGathering(boolean)
     */
    public void setAllowBlockBreaking(boolean value) {
        var config = hWorld.getGameplayConfig().getWorldConfig();
        try {
            Field field = WorldConfig.class.getDeclaredField("allowBlockBreaking");
            field.setAccessible(true);
            field.set(config, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Enables or disables block placement in this world.
     * 
     * @param value true to allow block placement, false to disallow it
     * 
     * @throws IllegalStateException if the world is not set or the field cannot be accessed
     * 
     * @see #setAllowBlockBreaking(boolean)
     * @see #setAllowBlockGathering(boolean)
     */
    public void setAllowBlockPlacement(boolean value) {
        var config = hWorld.getGameplayConfig().getWorldConfig();
        try {
            Field field = WorldConfig.class.getDeclaredField("allowBlockPlacement");
            field.setAccessible(true);
            field.set(config, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Enables or disables block gathering (harvesting) in this world.
     * 
     * @param value true to allow block gathering, false to disallow it
     * 
     * @throws IllegalStateException if the world is not set or the field cannot be accessed
     * 
     * @see #setAllowBlockBreaking(boolean)
     * @see #setAllowBlockPlacement(boolean)
     */
    public void setAllowBlockGathering(boolean value) {
        var config = hWorld.getGameplayConfig().getWorldConfig();
        try {
            Field field = WorldConfig.class.getDeclaredField("allowBlockGathering");
            field.setAccessible(true);
            field.set(config, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Enables or disables PvP (Player vs Player) combat in this world.
     * 
     * @param value true to allow PvP, false to disallow it
     * 
     * @throws IllegalStateException if the world is not set
     */
    public void setAllowPvP(boolean value)
    {
        hWorld.getWorldConfig().setPvpEnabled(value);
    }
}
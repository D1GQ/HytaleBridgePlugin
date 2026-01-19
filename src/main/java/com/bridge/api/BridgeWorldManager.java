package com.bridge.api;

import java.util.Collection;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.bridge.api.modules.BridgeWorld;
import com.bridge.api.modules.BridgeWorldComponent;
import com.bridge.core.factory.BridgeWorldFactory;
import com.bridge.core.systems.BridgeWorldManagerSystem;
import com.hypixel.hytale.server.core.universe.world.World;

/**
 * Main API class for managing BridgeWorld instances in the Bridge framework.
 * This class serves as a facade that provides simplified access to world management operations,
 * abstracting away the underlying implementation details.
 * 
 * <p>All methods in this class are static and thread-safe for easy access from anywhere in the codebase.
 */
public class BridgeWorldManager {
    
    /**
     * Retrieves all currently registered BridgeWorld instances.
     * This includes both active and inactive worlds that have been loaded or created through the Bridge system.
     * 
     * @return A collection of all BridgeWorld instances currently managed by the Bridge framework.
     *         Returns an empty collection if no worlds are registered.
     * 
     * @see BridgeWorldFactory#GetAllBridgeWorlds()
     */
    public static Collection<BridgeWorld> getAllVBridgeWorlds() {
        return BridgeWorldFactory.GetAllBridgeWorlds();
    }

    /**
     * Retrieves the default world as a BridgeWorld instance.
     * The default world is typically the main world that players join by default.
     * 
     * @return The default world wrapped as a BridgeWorld instance.
     *         May return null if no default world has been set or if the Bridge framework hasn't been initialized.
     * 
     * @see BridgeWorldFactory#getDefaultWorldAsBridgeWorld()
     */
    public static BridgeWorld getDefaultWorldAsBridgeWorld() {
        return BridgeWorldFactory.getDefaultWorldAsBridgeWorld();
    }

    /**
     * Updates the internal reference to the default world.
     * This should be called when the server's default world changes (e.g., on server reload or world reset).
     * 
     * <p>This method ensures that {@link #getDefaultWorldAsBridgeWorld()} returns the correct current default world.
     */
    public static void updateDefaultWorld() {
        BridgeWorldFactory.updateDefaultWorld();
    }

    /**
     * Creates a new world with the specified name and optional BridgeWorldComponent.
     * The world is created asynchronously and will be available for use once the callback is invoked.
     * 
     * @param name The name of the world to create. Must not be null or empty.
     * @param bridgeWorldComponent Optional component that provides custom behavior for the world.
     *                             If null, a default component will be used.
     * @param callback Optional callback that will be invoked with the created BridgeWorld instance
     *                 once the world creation is complete. The callback will be invoked on a
     *                 server thread appropriate for world operations.
     * 
     * @throws IllegalArgumentException if name is null or empty
     * 
     * @see BridgeWorldManagerSystem#createWorld(String, BridgeWorldComponent, Consumer)
     */
    public static void createWorld(@Nonnull String name, @Nullable BridgeWorldComponent bridgeWorldComponent,
        @Nullable Consumer<BridgeWorld> callback) {

        BridgeWorldManagerSystem.get().createWorld(name, bridgeWorldComponent, callback);
    }

    /**
     * Creates a copy of an existing world with a new name.
     * This performs a deep copy of all world files, including chunks, entities, and configuration.
     * 
     * <p><b>Note:</b> This operation can be resource-intensive for large worlds and runs asynchronously.
     * 
     * @param sourceWorld The world to copy from. Must not be null and must be a valid, loaded world.
     * @param newName The name for the copied world. Must not be null or empty.
     * @param bridgeWorldComponent Optional component for the new world. If null, will use the
     *                             same component type as the source world if available.
     * @param callback Optional callback that will be invoked with the new BridgeWorld instance
     *                 once the copy operation is complete.
     * 
     * @throws IllegalArgumentException if sourceWorld or newName is null, or if newName is empty
     * @throws IllegalStateException if the source world cannot be accessed or is not loaded
     * 
     * @see BridgeWorldManagerSystem#copyWorld(World, String, BridgeWorldComponent, Consumer)
     */
    public static void copyWorld(@Nonnull World sourceWorld, @Nonnull String newName, @Nullable BridgeWorldComponent bridgeWorldComponent,
        @Nullable Consumer<BridgeWorld> callback) {
        
        BridgeWorldManagerSystem.get().copyWorld(sourceWorld, newName, bridgeWorldComponent, callback);
    }

    /**
     * Deletes a BridgeWorld and all its associated files.
     * This operation is permanent and cannot be undone.
     * 
     * <p>Before deletion, the world will be deactivated if it's currently active,
     * and players will be moved to the default world.
     * 
     * @param bWorld The BridgeWorld to delete. Must not be null.
     * 
     * @throws IllegalArgumentException if bWorld is null
     * @throws IllegalStateException if the world cannot be deleted (e.g., system world)
     * 
     * @see BridgeWorldManagerSystem#deleteWorld(BridgeWorld)
     */
    public static void deleteWorld(@Nonnull BridgeWorld bWorld) {
        BridgeWorldManagerSystem.get().deleteWorld(bWorld);
    }

    /**
     * Activates a BridgeWorld, making it ready for player interaction.
     * This loads the world into memory and starts any associated systems.
     * 
     * <p>Activation is asynchronous; the callback will be invoked once activation is complete.
     * 
     * @param bWorld The BridgeWorld to activate. Must not be null.
     * @param callback Optional callback that will be invoked with the activated BridgeWorld
     *                 once activation is complete.
     * 
     * @throws IllegalArgumentException if bWorld is null
     * @throws IllegalStateException if the world cannot be activated (e.g., already active)
     * 
     * @see BridgeWorldManagerSystem#activateWorld(BridgeWorld, Consumer)
     */
    public static void activateWorld(@Nonnull BridgeWorld bWorld, @Nullable Consumer<BridgeWorld> callback) {
        BridgeWorldManagerSystem.get().activateWorld(bWorld, callback);
    }

    /**
     * Deactivates a BridgeWorld, stopping all activity and unloading it from memory.
     * All players in the world will be moved to the specified destination world.
     * 
     * <p>If no destination world is specified, players will be moved to the default world.
     * 
     * @param bWorld The BridgeWorld to deactivate. Must not be null.
     * @param movePlayersTo The world where players should be moved. If null, the default world will be used.
     * @return true if deactivation was successful, false otherwise
     * 
     * @throws IllegalArgumentException if bWorld is null
     * 
     * @see BridgeWorldManagerSystem#deactivateWorld(BridgeWorld, World)
     */
    public static Boolean deactivateWorld(@Nonnull BridgeWorld bWorld, @Nullable World movePlayersTo) {
        return BridgeWorldManagerSystem.get().deactivateWorld(bWorld, movePlayersTo);
    }
}
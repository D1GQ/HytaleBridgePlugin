package com.bridge.api.modules;

import com.hypixel.hytale.server.core.universe.PlayerRef;

/**
 * Base component class that provides customizable behavior for {@link BridgeWorld} instances.
 * 
 * <p>This class serves as a template for creating custom world behavior. Developers should
 * extend this class and override the lifecycle methods to implement specific functionality
 * for their worlds.
 * 
 * <p>All methods in this class are no-op by default and can be safely overridden.
 * 
 */
public class BridgeWorldComponent {
    
    /**
     * Called every server tick for active worlds.
     * This method is invoked on the main server thread.
     * 
     * <p><b>Performance Note:</b> Keep this method lightweight as it runs every tick
     * for every active world with this component.
     * 
     * @param bWorld The BridgeWorld instance that owns this component
     * @param deltaTime The time elapsed since the last tick, in seconds
     * 
     * @see BridgeWorld
     */
    public void onTick(BridgeWorld bWorld, float deltaTime) {
        // Default implementation does nothing
    }
    
    /**
     * Called when a world is first created and registered with the Bridge system.
     * 
     * <p>This method is called after the world is created but before it becomes active.
     * 
     * @param bWorld The BridgeWorld instance that was created
     * 
     * @see BridgeWorld
     */
    public void onCreateWorld(BridgeWorld bWorld) {  
        // Default implementation does nothing
    }

    /**
     * Called when a world is about to be deleted.
     * 
     * <p><b>Important:</b> This method is called before the world files are deleted.
     * Any data that needs to be preserved should be saved elsewhere.
     * 
     * @param bWorld The BridgeWorld instance that will be deleted
     * 
     * @see BridgeWorld#delete()
     */
    public void onDeleteWorld(BridgeWorld bWorld) {
        // Default implementation does nothing
    }

    /**
     * Called when a player joins this world.
     * 
     * <p>This method is called after the player has been successfully transferred
     * to the world and is ready to interact with it.
     * 
     * @param bWorld The BridgeWorld instance the player joined
     * @param playerRef The player who joined the world
     * 
     * @see BridgeWorld#transferPlayer(PlayerRef)
     * @see PlayerRef
     */
    public void onPlayerJoinWorld(BridgeWorld bWorld, PlayerRef playerRef) {
        // Default implementation does nothing
    }

    /**
     * Called when a player dies in this world.
     * 
     * <p>This method is called when the player's death is processed by the server.
     * 
     * @param bWorld The BridgeWorld instance where the death occurred
     * @param playerRef The player who died
     * 
     * @see PlayerRef
     */
    public void onPlayerDieInWorld(BridgeWorld bWorld, PlayerRef playerRef) {
        // Default implementation does nothing
    }

    /**
     * Called when a player leaves this world.
     * 
     * <p>This method is called before the player is transferred to another world
     * or disconnects from the server.
     * 
     * @param bWorld The BridgeWorld instance the player is leaving
     * @param playerRef The player who is leaving the world
     * 
     * @see BridgeWorld#transferPlayer(PlayerRef)
     * @see PlayerRef
     */
    public void onPlayerLeaveWorld(BridgeWorld bWorld, PlayerRef playerRef) {
        // Default implementation does nothing
    }
}
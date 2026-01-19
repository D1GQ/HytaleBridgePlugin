package com.bridge.api.utilities;

import java.util.concurrent.CompletableFuture;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Utility class providing common helper methods for Bridge framework operations.
 */
public class BridgeUtil {
    
    /**
     * Converts a {@link PlayerRef} to a {@link Player} entity instance.
     * 
     * <p>This method safely extracts the actual Player entity from a PlayerRef,
     * performing null checks and reference validation along the way.
     * 
     * 
     * @param playerRef The player reference to convert. May be null.
     * @return The Player entity if the reference is valid and the player exists,
     *         otherwise null. Returns null if:
     *         <ul>
     *           <li>playerRef is null</li>
     *           <li>The underlying reference is invalid or expired</li>
     *           <li>The player no longer exists in the entity store</li>
     *         </ul>
     * 
     * @see PlayerRef
     * @see Player
     * @see EntityStore
     */
    public Player getPlayerFromPlayerRef(PlayerRef playerRef) {
        if(playerRef == null)
        {
            return null;
        }
        Ref<EntityStore> ref = playerRef.getReference();
        if (ref == null || !ref.isValid()) {
            return null;
        }
        Store<EntityStore> store = ref.getStore();
        return store.getComponent(ref, Player.getComponentType());
    }

    private static CompletableFuture<Void> defaultWorldReady;

    /**
     * Gets or creates a {@link CompletableFuture} that completes when the default world is ready.
     * 
     * <p>This method provides a safe way to wait for the Universe and its default world
     * to be fully initialized before attempting to use them. This is especially useful
     * during server startup when systems may try to access the default world before
     * it's fully loaded.
     * 
     * @return A CompletableFuture that completes when both {@link Universe#get()} returns
     *         a non-null Universe and {@link Universe#getDefaultWorld()} returns a non-null World.
     *         The future completes with null (Void) on success.
     * 
     * @throws RuntimeException if the waiting thread is interrupted
     * 
     * @see Universe
     * @see CompletableFuture
     */
    public static synchronized CompletableFuture<Void> getDefaultWorldReady() {
        if (defaultWorldReady != null) {
            return defaultWorldReady;
        }

        defaultWorldReady = CompletableFuture.runAsync(() -> {
            while (Universe.get() == null || Universe.get().getDefaultWorld() == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted waiting for DefaultWorld", e);
                }
            }
        });
        
        return defaultWorldReady;
    }
}
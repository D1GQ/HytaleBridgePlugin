package com.bridge.core.factory;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.bridge.api.modules.BridgeWorld;
import com.bridge.api.modules.BridgeWorldComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

@Deprecated
public class BridgeWorldFactory {
    private static Map<UUID, BridgeWorld> map = new ConcurrentHashMap<>();
    private static BridgeWorld defaultWBWorld;

    public static BridgeWorld getDefaultWorldAsBridgeWorld() {
        return defaultWBWorld;
    }

    public static void load() {
        for (var hWorld : Universe.get().getWorlds().values()) {
            var bWorld = getBridgeWorldFromWorld(hWorld, hWorld.getName(), null);
            bWorld.setWorld(hWorld);
        }

        updateDefaultWorld();
    }

    public static void updateDefaultWorld() {
        var defaultWorld = Universe.get().getDefaultWorld();
        if (defaultWorld != null) {
            defaultWBWorld = getBridgeWorldFromWorld(defaultWorld, defaultWorld.getName(), null);
            defaultWBWorld.setWorld(defaultWorld);
        }
    }

    public static Collection<BridgeWorld> GetAllBridgeWorlds() {
        return map.values();
    }

    @Nonnull
    public static BridgeWorld getBridgeWorldFromWorld(@Nonnull World world, @Nonnull String name, @Nullable BridgeWorldComponent bridgeWorldComponent) {
        UUID worldUuid = world.getWorldConfig().getUuid();
        BridgeWorld bridgeWorld = map.get(worldUuid);
        
        if (bridgeWorld == null) {
            bridgeWorld = new BridgeWorld(name, bridgeWorldComponent);
            map.put(worldUuid, bridgeWorld);
        }
        
        return bridgeWorld;
    }

    @Nullable
    public static BridgeWorld getBridgeWorldByUUID(UUID uuid) {
        BridgeWorld bridgeWorld = map.get(uuid);
        return bridgeWorld;
    }

    public static void releaseBridgeWorld(@Nonnull BridgeWorld bWorld) {
        map.remove(bWorld.getWorld().getWorldConfig().getUuid());
    }
    
    public static boolean hasBridgeWorld(@Nonnull World world) {
        return map.containsKey(world.getWorldConfig().getUuid());
    }
}
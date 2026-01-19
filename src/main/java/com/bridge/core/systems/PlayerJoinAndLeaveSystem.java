package com.bridge.core.systems;

import com.bridge.core.factory.BridgeWorldFactory;
import com.bridge.core.plugin.BridgePlugin;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;;

@Deprecated
public final class PlayerJoinAndLeaveSystem extends RefSystem<EntityStore> {

    @Override
    public final Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }

    @Override
    public final void onEntityAdded(Ref<EntityStore> ref, AddReason reason, Store<EntityStore> store,
        CommandBuffer<EntityStore> cmd) {
            
        var player = store.getComponent(ref, Player.getComponentType());
        var playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        var hWorld = player.getWorld();
        if (hWorld != null) {
            var bWorld = BridgeWorldFactory.getBridgeWorldFromWorld(hWorld, hWorld.getName(), null);
            bWorld.getBridgeWorldComponent().onPlayerJoinWorld(bWorld, playerRef);
        }
    }

    @Override
    public final void onEntityRemove(Ref<EntityStore> ref, RemoveReason reason, Store<EntityStore> store,
            CommandBuffer<EntityStore> cmd) {

        var player = store.getComponent(ref, Player.getComponentType());
        var playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        var hWorld = player.getWorld();
        if (hWorld != null) {
            var bWorld = BridgeWorldFactory.getBridgeWorldFromWorld(hWorld, hWorld.getName(), null);
            bWorld.getBridgeWorldComponent().onPlayerLeaveWorld(bWorld, playerRef);

            BridgePlugin.logger().atFine().log("TEST");
        }
    }
}

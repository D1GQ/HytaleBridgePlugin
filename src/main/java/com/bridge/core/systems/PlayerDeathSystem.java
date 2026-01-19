package com.bridge.core.systems;
import com.bridge.core.factory.BridgeWorldFactory;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems.OnDeathSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;;

@Deprecated
public final class PlayerDeathSystem extends OnDeathSystem {
    @Override
    public final Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
    @Override
    public final void onComponentAdded(Ref<EntityStore> ref, DeathComponent deathComponent, Store<EntityStore> store,
            CommandBuffer<EntityStore> cmd) {

        var playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        var player = store.getComponent(ref, Player.getComponentType());
        var hWorld = player.getWorld();
        var bWorld = BridgeWorldFactory.getBridgeWorldFromWorld(hWorld, hWorld.getName(), null);
        bWorld.getBridgeWorldComponent().onPlayerDieInWorld(bWorld, playerRef);
    }
}

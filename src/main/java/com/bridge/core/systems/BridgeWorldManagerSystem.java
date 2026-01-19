package com.bridge.core.systems;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.bridge.api.modules.BridgeWorld;
import com.bridge.api.modules.BridgeWorldComponent;
import com.bridge.core.data.DataManager;
import com.bridge.core.factory.BridgeWorldFactory;
import com.bridge.core.plugin.BridgePlugin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickableSystem;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

@Deprecated
public final class BridgeWorldManagerSystem implements TickableSystem<ChunkStore> {
    private static BridgeWorldManagerSystem worldManagerSystem;

    public BridgeWorldManagerSystem()
    {
        worldManagerSystem = this;
    }

    @Nonnull
    public static BridgeWorldManagerSystem get() {
        return worldManagerSystem;
    }

    @Nonnull
    private List<BridgeWorld> activeWorlds = new ArrayList<>();

    @Override
    public final void tick(float deltaTime, int index, Store store) {
        for (var bWorld : activeWorlds) {
            bWorld.getBridgeWorldComponent().onTick(bWorld, deltaTime);
        }
    }

    public void createWorld(@Nonnull String name, @Nullable BridgeWorldComponent bridgeWorldComponent,
        @Nullable Consumer<BridgeWorld> callback) {
        
        BridgePlugin.logger().atInfo().log("Starting creating new world: " + name);

        Universe.get().makeWorld(name, DataManager.getWorldsPath(name), new WorldConfig())
            .thenAccept(world -> {
                try {
                    var bWorld = BridgeWorldFactory.getBridgeWorldFromWorld(world, name, bridgeWorldComponent);
                    bWorld.setWorld(world);
                    bWorld.getBridgeWorldComponent().onCreateWorld(bWorld);
                    
                    BridgePlugin.logger().atInfo().log("Done creating new world: " + name);

                    if (callback != null) {
                        callback.accept(bWorld);
                    }
                } catch (Exception e) {
                    BridgePlugin.logger().atSevere().withCause(e).log("Failed to create world: " + name);
                }
            })
            .exceptionally(throwable -> {
                BridgePlugin.logger().atSevere().withCause(throwable).log("Failed to make world: " + name);
                return null;
            });
    }

    public void copyWorld(@Nonnull World sourceWorld, @Nonnull String newName, @Nullable BridgeWorldComponent bridgeWorldComponent,
        @Nullable Consumer<BridgeWorld> callback) {
        
        BridgePlugin.logger().atInfo().log("Starting copying world " + sourceWorld.getName() + " to " + newName);
        
        CompletableFuture.runAsync(() -> {
            try {
                Path sourcePath = sourceWorld.getSavePath();
                Path newSavePath = DataManager.getWorldsPath(newName);
                boolean copied = copyWorldFiles(sourcePath, newSavePath);
                
                if (!copied) {
                    throw new IOException("Failed to copy world files");
                }

                updateWorldConfigForCopy(newSavePath);
                
                // After copying files, load the world
                Universe.get().loadWorld(newName).thenAccept(world -> {
                    var bWorld = BridgeWorldFactory.getBridgeWorldFromWorld(sourceWorld, newName, bridgeWorldComponent);
                    bWorld.setWorld(world);
                    BridgePlugin.logger().atInfo().log("Done copying world " + sourceWorld.getName() + " to " + newName);
                    
                    if (callback != null) {
                        callback.accept(bWorld);
                    }
                }).exceptionally(throwable -> {
                    BridgePlugin.logger().atSevere().withCause(throwable)
                        .log("Failed to load copied world: " + newName);
                    return null;
                });
                
            } catch (Exception e) {
                BridgePlugin.logger().atSevere().withCause(e)
                    .log("Failed to copy world files from " + sourceWorld.getName() + " to " + newName);
            }
        }).exceptionally(throwable -> {
            BridgePlugin.logger().atSevere().withCause(throwable)
                .log("Async task failed for copying world");
            return null;
        });
    }

    private Boolean copyWorldFiles(Path sourcePath, Path destinationPath) throws IOException {
        if (Files.exists(destinationPath)) {
            Files.walk(destinationPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
        
        Files.walk(sourcePath).forEach(source -> {
            try {
                Path destination = destinationPath.resolve(sourcePath.relativize(source));
                if (Files.isDirectory(source)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        return true;
    }

    private void updateWorldConfigForCopy(Path worldFolder) {
        Path configFile = worldFolder.resolve("config.json");
        
        if (!Files.exists(configFile)) {
            BridgePlugin.logger().atWarning().log("No config.json found in " + worldFolder);
            return;
        }
        
        try {
            String content = Files.readString(configFile);
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
        
            UUID newUuid = UUID.randomUUID();
            byte[] uuidBytes = new byte[16];
            ByteBuffer.wrap(uuidBytes)
                .putLong(newUuid.getMostSignificantBits())
                .putLong(newUuid.getLeastSignificantBits());
            
            String base64Uuid = Base64.getEncoder().encodeToString(uuidBytes);
            
            JsonObject uuidObject = new JsonObject();
            uuidObject.addProperty("$binary", base64Uuid);
            uuidObject.addProperty("$type", "04");
            
            json.add("UUID", uuidObject);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(configFile, gson.toJson(json));
            
            BridgePlugin.logger().atInfo().log("Updated world.json with new UUID and name");
        } catch (Exception e) {
            BridgePlugin.logger().atSevere().withCause(e).log("Failed to update world config");
        }
    }

    public void deleteWorld(@Nonnull BridgeWorld bWorld) {
        BridgePlugin.logger().atInfo().log("Deleting " + bWorld.bWorldName);
        bWorld.getBridgeWorldComponent().onDeleteWorld(bWorld);
        Universe.get().removeWorld(bWorld.bWorldName);
        BridgeWorldFactory.releaseBridgeWorld(bWorld);
    }

    public void activateWorld(@Nonnull BridgeWorld bWorld, @Nullable Consumer<BridgeWorld> callback) {
        
        BridgePlugin.logger().atInfo().log("Starting activating world " + bWorld.bWorldName);
        
        bWorld.getWorld().init()
            .thenAccept(world -> {
                try {
                    bWorld.setWorld(world);
                    activeWorlds.add(bWorld);
                    bWorld.active = true;
                    BridgePlugin.logger().atInfo().log("Done activating world " + bWorld.bWorldName);
                    
                    if (callback != null) {
                        callback.accept(bWorld);
                    }
                } catch (Exception e) {
                    BridgePlugin.logger().atSevere().withCause(e)
                        .log("Failed to activate world " + bWorld.bWorldName);
                }
            })
            .exceptionally(throwable -> {
                BridgePlugin.logger().atSevere().withCause(throwable)
                    .log("Failed to initialize world " + bWorld.bWorldName);
                return null;
            });
    }

    public Boolean deactivateWorld(@Nonnull BridgeWorld bWorld, @Nullable World movePlayersTo) {
        BridgePlugin.logger().atInfo().log("Starting deactivating world " + bWorld.bWorldName);
        if (movePlayersTo == null)
        {
            movePlayersTo = Universe.get().getDefaultWorld();
        }

        bWorld.active = false;
        activeWorlds.remove(bWorld);
        BridgePlugin.logger().atInfo().log("Moving players from " + bWorld.bWorldName + " to " + movePlayersTo.getName());
        bWorld.getWorld().drainPlayersTo(movePlayersTo);
        bWorld.getWorld().stopIndividualWorld();;
        BridgePlugin.logger().atInfo().log("Done deactivating world " + bWorld.bWorldName);
        return true;
    }
}

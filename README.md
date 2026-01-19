# Bridge - Hytale World Management Library

A powerful Hytale plugin library for creating, managing, and orchestrating temporary worlds with ease. Bridge provides a robust API for handling world lifecycle, player transfers, and customizable world behavior.

## ✨ Features

- **Easy World Management**: Create, activate, deactivate, and delete worlds with simple API calls
- **Player Transfers**: Safely move players between worlds with built-in thread safety
- **Custom World Behavior**: Extend `BridgeWorldComponent` to add custom logic to your worlds
- **Asynchronous Operations**: Most operations run asynchronously with optional callbacks
- **World Configuration**: Control block breaking, placement, gathering, and PvP settings
- **Lifecycle Events**: Hook into world creation, deletion, and player events

## 🚀 Installation

1. Download the latest Bridge jar from the releases page
2. Place it in your Hytale server's `plugins` folder
3. Add Bridge as a dependency in your plugin's build configuration

## 📚 Core API

### Main Classes

#### `BridgeWorldManager`
The primary entry point for world management operations. All methods are static and thread-safe.

```java
// Create a new world
BridgeWorldManager.createWorld("arena", new ArenaComponent(), world -> {
    // World is ready!
});

// Get all managed worlds
Collection<BridgeWorld> worlds = BridgeWorldManager.getAllVBridgeWorlds();

// Get the default world
BridgeWorld defaultWorld = BridgeWorldManager.getDefaultWorldAsBridgeWorld();
```

#### `BridgeWorld`
Represents a managed world with enhanced functionality.

```java
// Activate a world (load into memory)
bridgeWorld.activate(world -> {
    // World is now active and ready for players
});

// Transfer a player to this world
bridgeWorld.transferPlayer(playerRef);

// Configure world settings
bridgeWorld.setAllowPvP(true);
bridgeWorld.setAllowBlockBreaking(false);

// Deactivate world (unload from memory)
bridgeWorld.deactivate();
```

#### `BridgeWorldComponent`
Base class for creating custom world behavior. Extend this to add game logic to your worlds.

```java
public class ArenaComponent extends BridgeWorldComponent {
    @Override
    public void onPlayerJoinWorld(BridgeWorld bWorld, PlayerRef playerRef) {
        // Give players arena gear when they join
        giveArenaKit(playerRef);
    }
    
    @Override
    public void onTick(BridgeWorld bWorld, float deltaTime) {
        // Run arena logic every tick
        updateArenaGameState();
    }
}
```

## 🔧 Usage Examples

### Creating a Temporary Arena World

```java
public class ArenaManager {
    private BridgeWorld arenaWorld;
    
    public void setupArena() {
        // Create arena world with custom component
        BridgeWorldManager.createWorld("tournament-arena", new ArenaComponent(), world -> {
            arenaWorld = world;
            arenaWorld.setAllowPvP(true);
            arenaWorld.setAllowBlockBreaking(false);
            arenaWorld.activate();
        });
    }
    
    public void teleportPlayersToArena(List<PlayerRef> players) {
        if (arenaWorld != null && arenaWorld.active) {
            for (PlayerRef player : players) {
                arenaWorld.transferPlayer(player);
            }
        }
    }
}
```

### Managing Mini-game Worlds

```java
public class MiniGameWorld extends BridgeWorldComponent {
    private int gameTimer = 300; // 5 minutes
    
    @Override
    public void onCreateWorld(BridgeWorld bWorld) {
        // Initialize game state
        setupGame();
    }
    
    @Override
    public void onTick(BridgeWorld bWorld, float deltaTime) {
        gameTimer -= deltaTime;
        if (gameTimer <= 0) {
            endGame(bWorld);
        }
    }
    
    @Override
    public void onDeleteWorld(BridgeWorld bWorld) {
        // Clean up resources
        cleanupGame();
    }
}
```

## 📖 API Reference

### Key Methods

#### BridgeWorldManager
- `createWorld(String name, BridgeWorldComponent component, Consumer<BridgeWorld> callback)`
- `getAllVBridgeWorlds()` - Get all managed worlds
- `getDefaultWorldAsBridgeWorld()` - Get the default world
- `deleteWorld(BridgeWorld world)` - Permanently delete a world
- `activateWorld(BridgeWorld world, Consumer<BridgeWorld> callback)`
- `deactivateWorld(BridgeWorld world, World movePlayersTo)`

#### BridgeWorld
- `activate(Consumer<BridgeWorld> callback)` - Load world into memory
- `deactivate(World movePlayersTo)` - Unload world
- `transferPlayer(PlayerRef player)` - Move player to this world
- `delete()` - Delete this world and its files
- `setAllowPvP(boolean enabled)` - Toggle PvP
- `setAllowBlockBreaking(boolean enabled)` - Toggle block breaking

#### BridgeWorldComponent Lifecycle Methods
- `onCreateWorld(BridgeWorld world)` - When world is created
- `onDeleteWorld(BridgeWorld world)` - Before world deletion
- `onPlayerJoinWorld(BridgeWorld world, PlayerRef player)`
- `onPlayerLeaveWorld(BridgeWorld world, PlayerRef player)`
- `onPlayerDieInWorld(BridgeWorld world, PlayerRef player)`
- `onTick(BridgeWorld world, float deltaTime)` - Called every tick

## ⚡ Performance Tips

1. **Only activate worlds when needed**: Worlds consume memory while active
2. **Implement onTick efficiently**: Keep tick logic lightweight
3. **Use callbacks for async operations**: Don't block the main thread
4. **Clean up in onDeleteWorld**: Release any external resources
5. **Deactivate unused worlds**: Free up server resources

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 🐛 Issues

Found a bug or have a feature request? Please open an issue on the GitHub repository.

---

**Bridge** - Simplifying Hytale world management, one world at a time.
package net.havoccasino.room;

import net.havoccasino.HavocCasino;
import net.havoccasino.schematic.BlockSchematic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns the layout of player gambling rooms: assigns each new room a plot on a
 * grid, pastes the configured schematic there, records ownership, and teleports.
 */
public final class RoomManager {

    private final HavocCasino plugin;
    private final File file;
    private final ConcurrentHashMap<UUID, Location> spawns = new ConcurrentHashMap<>();
    private final java.util.Set<UUID> building = ConcurrentHashMap.newKeySet();
    private int nextIndex = 0;

    public RoomManager(HavocCasino plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "rooms.yml");
    }

    public void load() {
        if (!file.exists()) {
            return;
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        nextIndex = cfg.getInt("next-index", 0);
        ConfigurationSection root = cfg.getConfigurationSection("rooms");
        if (root != null) {
            for (String key : root.getKeys(false)) {
                try {
                    ConfigurationSection s = root.getConfigurationSection(key);
                    if (s == null) {
                        continue;
                    }
                    World world = Bukkit.getWorld(s.getString("world", ""));
                    if (world == null) {
                        continue;
                    }
                    spawns.put(UUID.fromString(key),
                            new Location(world, s.getDouble("x"), s.getDouble("y"), s.getDouble("z")));
                } catch (IllegalArgumentException ignored) {
                    // Skip malformed entries.
                }
            }
        }
    }

    public void save() {
        FileConfiguration cfg = new YamlConfiguration();
        cfg.set("next-index", nextIndex);
        spawns.forEach((uuid, loc) -> {
            String base = "rooms." + uuid;
            cfg.set(base + ".world", loc.getWorld() != null ? loc.getWorld().getName() : "");
            cfg.set(base + ".x", loc.getX());
            cfg.set(base + ".y", loc.getY());
            cfg.set(base + ".z", loc.getZ());
        });
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save rooms.yml: " + e.getMessage());
        }
    }

    public boolean hasRoom(UUID uuid) {
        return spawns.containsKey(uuid);
    }

    public void teleport(Player player) {
        Location loc = spawns.get(player.getUniqueId());
        if (loc != null) {
            player.teleport(loc);
        }
    }

    public boolean deleteRoom(java.util.UUID uuid) {
        boolean existed = spawns.remove(uuid) != null;
        if (existed) {
            save();
        }
        return existed;
    }

    public void createOrTeleport(Player player) {
        UUID uuid = player.getUniqueId();
        if (building.contains(uuid)) {
            plugin.messages().send(player, "room.building");
            return;
        }
        if (hasRoom(uuid)) {
            teleport(player);
            plugin.messages().send(player, "room.teleported");
            return;
        }

        FileConfiguration c = plugin.getConfig();
        String schematicName = c.getString("rooms.schematic", "");
        if (schematicName.isEmpty() || !plugin.schematics().exists(schematicName)) {
            plugin.messages().send(player, "room.not-configured");
            return;
        }
        World world = Bukkit.getWorld(c.getString("rooms.world", player.getWorld().getName()));
        if (world == null) {
            plugin.messages().send(player, "room.not-configured");
            return;
        }
        BlockSchematic schematic = plugin.schematics().load(schematicName);
        if (schematic == null) {
            plugin.messages().send(player, "room.not-configured");
            return;
        }

        double cost = c.getDouble("rooms.cost", 0);
        if (cost > 0) {
            if (!plugin.currencyService().has(player, cost)) {
                plugin.messages().send(player, "room.cant-afford", "cost", plugin.currencyService().format(cost));
                return;
            }
            if (!plugin.currencyService().withdraw(player, cost)) {
                plugin.messages().send(player, "room.cant-afford", "cost", plugin.currencyService().format(cost));
                return;
            }
        }

        int index = nextIndex;
        Location origin = plotOrigin(index, world, c);

        building.add(uuid);
        plugin.messages().send(player, "room.building");

        plugin.schematics().paste(schematic, origin, c.getBoolean("rooms.clear-air", true), () -> {
            building.remove(uuid);
            Location spawn = origin.clone().add(
                    c.getDouble("rooms.teleport-offset.x", 0.5),
                    c.getDouble("rooms.teleport-offset.y", 1.0),
                    c.getDouble("rooms.teleport-offset.z", 0.5));
            spawns.put(uuid, spawn);
            nextIndex++;
            save();
            if (player.isOnline()) {
                player.teleport(spawn);
                plugin.messages().send(player, "room.created");
            }
        });
    }

    private Location plotOrigin(int index, World world, FileConfiguration c) {
        int baseX = c.getInt("rooms.origin.x", 0);
        int baseY = c.getInt("rooms.origin.y", 100);
        int baseZ = c.getInt("rooms.origin.z", 0);
        int spacingX = Math.max(1, c.getInt("rooms.spacing.x", 50));
        int spacingZ = Math.max(1, c.getInt("rooms.spacing.z", 50));
        int columns = Math.max(1, c.getInt("rooms.columns", 10));

        int col = index % columns;
        int row = index / columns;
        return new Location(world, baseX + (double) col * spacingX, baseY, baseZ + (double) row * spacingZ);
    }
}

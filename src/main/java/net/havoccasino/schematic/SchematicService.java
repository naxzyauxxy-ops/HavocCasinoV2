package net.havoccasino.schematic;

import net.havoccasino.HavocCasino;
import net.havoccasino.region.Selection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A lightweight, self-contained schematic system (no WorldEdit needed).
 * Captures non-air blocks in a selection and can paste them back, batching
 * the block writes across ticks so a large room doesn't freeze the server.
 */
public final class SchematicService {

    public enum SaveResult {
        OK, INCOMPLETE, TOO_BIG, ERROR
    }

    private final HavocCasino plugin;
    private final File dir;

    public SchematicService(HavocCasino plugin) {
        this.plugin = plugin;
        this.dir = new File(plugin.getDataFolder(), "schematics");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public long maxVolume() {
        return plugin.getConfig().getLong("rooms.max-volume", 100000);
    }

    public SaveResult save(String name, Selection selection) {
        if (!selection.isComplete()) {
            return SaveResult.INCOMPLETE;
        }
        if (selection.volume() > maxVolume()) {
            return SaveResult.TOO_BIG;
        }

        World world = selection.world();
        int minX = selection.minX();
        int minY = selection.minY();
        int minZ = selection.minZ();
        int maxX = selection.maxX();
        int maxY = selection.maxY();
        int maxZ = selection.maxZ();

        List<String> entries = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType().isAir()) {
                        continue;
                    }
                    entries.add((x - minX) + "," + (y - minY) + "," + (z - minZ)
                            + "=" + block.getBlockData().getAsString());
                }
            }
        }

        FileConfiguration cfg = new YamlConfiguration();
        cfg.set("width", maxX - minX + 1);
        cfg.set("height", maxY - minY + 1);
        cfg.set("length", maxZ - minZ + 1);
        cfg.set("blocks", entries);
        try {
            cfg.save(new File(dir, name.toLowerCase() + ".yml"));
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save schematic '" + name + "': " + e.getMessage());
            return SaveResult.ERROR;
        }
        return SaveResult.OK;
    }

    public boolean exists(String name) {
        return name != null && new File(dir, name.toLowerCase() + ".yml").exists();
    }

    public List<String> list() {
        List<String> out = new ArrayList<>();
        File[] files = dir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String n = file.getName();
                out.add(n.substring(0, n.length() - 4));
            }
        }
        return out;
    }

    public BlockSchematic load(String name) {
        File file = new File(dir, name.toLowerCase() + ".yml");
        if (!file.exists()) {
            return null;
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        int width = cfg.getInt("width");
        int height = cfg.getInt("height");
        int length = cfg.getInt("length");
        Map<String, String> blocks = new HashMap<>();
        for (String entry : cfg.getStringList("blocks")) {
            int eq = entry.indexOf('=');
            if (eq > 0) {
                blocks.put(entry.substring(0, eq), entry.substring(eq + 1));
            }
        }
        return new BlockSchematic(width, height, length, blocks);
    }

    /**
     * Pastes the schematic with its minimum corner at {@code origin}. When
     * {@code clearAir} is true the whole bounding box is set to air first so
     * the structure reproduces faithfully. Runs {@code onDone} when finished.
     */
    public void paste(BlockSchematic schematic, Location origin, boolean clearAir, Runnable onDone) {
        World world = origin.getWorld();
        if (world == null) {
            if (onDone != null) {
                onDone.run();
            }
            return;
        }
        int ox = origin.getBlockX();
        int oy = origin.getBlockY();
        int oz = origin.getBlockZ();

        Deque<int[]> airOps = new ArrayDeque<>();      // coords to clear to air
        Deque<int[]> blockCoords = new ArrayDeque<>();  // coords to set
        Deque<String> blockData = new ArrayDeque<>();   // matching block data strings

        for (int dx = 0; dx < schematic.width(); dx++) {
            for (int dy = 0; dy < schematic.height(); dy++) {
                for (int dz = 0; dz < schematic.length(); dz++) {
                    String data = schematic.blocks().get(dx + "," + dy + "," + dz);
                    if (data != null) {
                        blockCoords.add(new int[]{ox + dx, oy + dy, oz + dz});
                        blockData.add(data);
                    } else if (clearAir) {
                        airOps.add(new int[]{ox + dx, oy + dy, oz + dz});
                    }
                }
            }
        }

        int perTick = Math.max(256, plugin.getConfig().getInt("rooms.blocks-per-tick", 2000));
        BlockData air = Bukkit.createBlockData(Material.AIR);

        new BukkitRunnable() {
            @Override
            public void run() {
                int count = 0;
                while (count < perTick && !airOps.isEmpty()) {
                    int[] op = airOps.poll();
                    world.getBlockAt(op[0], op[1], op[2]).setBlockData(air, false);
                    count++;
                }
                while (count < perTick && !blockCoords.isEmpty()) {
                    int[] op = blockCoords.poll();
                    String data = blockData.poll();
                    try {
                        world.getBlockAt(op[0], op[1], op[2]).setBlockData(Bukkit.createBlockData(data), false);
                    } catch (IllegalArgumentException ignored) {
                        // Skip block data that can't be parsed on this server version.
                    }
                    count++;
                }
                if (airOps.isEmpty() && blockCoords.isEmpty()) {
                    cancel();
                    if (onDone != null) {
                        onDone.run();
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}

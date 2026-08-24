package net.havoccasino.game;

import net.havoccasino.HavocCasino;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads crate definitions from crates.yml.
 */
public final class CrateManager {

    private final HavocCasino plugin;
    private final File file;
    private final Map<String, Crate> crates = new LinkedHashMap<>();

    public CrateManager(HavocCasino plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "crates.yml");
    }

    public void load() {
        if (!file.exists()) {
            plugin.saveResource("crates.yml", false);
        }
        crates.clear();

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = cfg.getConfigurationSection("crates");
        if (root == null) {
            plugin.getLogger().warning("crates.yml has no 'crates' section.");
            return;
        }

        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            String display = section.getString("display", id);
            double cost = section.getDouble("cost", 100);
            Material icon = matchMaterial(section.getString("icon", "CHEST"), Material.CHEST);

            List<CrateReward> rewards = new ArrayList<>();
            for (Map<?, ?> raw : section.getMapList("rewards")) {
                CrateReward reward = parseReward(id, raw);
                if (reward != null) {
                    rewards.add(reward);
                }
            }
            if (rewards.isEmpty()) {
                plugin.getLogger().warning("Crate '" + id + "' has no valid rewards; skipping.");
                continue;
            }
            crates.put(id.toLowerCase(), new Crate(id, display, cost, icon, rewards));
        }
        plugin.getLogger().info("Loaded " + crates.size() + " crate(s).");
    }

    private CrateReward parseReward(String crateId, Map<?, ?> raw) {
        try {
            Object nameObj = raw.get("name");
            String name = nameObj != null ? String.valueOf(nameObj) : "<gray>Reward";
            Object materialObj = raw.get("material");
            Material material = matchMaterial(materialObj != null ? String.valueOf(materialObj) : "PAPER", Material.PAPER);
            int weight = raw.get("weight") instanceof Number n ? n.intValue() : 1;
            double multiplier = raw.get("multiplier") instanceof Number n ? n.doubleValue() : 0;
            return new CrateReward(name, material, weight, multiplier);
        } catch (Exception e) {
            plugin.getLogger().warning("Skipping a bad reward in crate '" + crateId + "': " + e.getMessage());
            return null;
        }
    }

    private Material matchMaterial(String name, Material fallback) {
        if (name == null) {
            return fallback;
        }
        Material material = Material.matchMaterial(name.trim().toUpperCase());
        return material != null ? material : fallback;
    }

    public Crate get(String id) {
        return id == null ? null : crates.get(id.toLowerCase());
    }

    public Collection<Crate> all() {
        return crates.values();
    }

    public boolean isEmpty() {
        return crates.isEmpty();
    }
}

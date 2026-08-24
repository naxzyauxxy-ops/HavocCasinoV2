package net.havoccasino.settings;

import net.havoccasino.HavocCasino;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player, client-side preferences persisted in settings.yml.
 * Currently tracks whether a player wants to receive HavocCasino messages.
 * Messages default to ON for players with no stored preference.
 */
public final class PlayerSettings {

    private final HavocCasino plugin;
    private final File file;
    private final ConcurrentHashMap<UUID, Boolean> messagesEnabled = new ConcurrentHashMap<>();

    public PlayerSettings(HavocCasino plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "settings.yml");
    }

    public void load() {
        if (!file.exists()) {
            return;
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (String key : cfg.getKeys(false)) {
            try {
                messagesEnabled.put(UUID.fromString(key), cfg.getBoolean(key + ".messages", true));
            } catch (IllegalArgumentException ignored) {
                // Skip malformed keys.
            }
        }
    }

    public void save() {
        FileConfiguration cfg = new YamlConfiguration();
        messagesEnabled.forEach((uuid, enabled) -> cfg.set(uuid.toString() + ".messages", enabled));
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save settings.yml: " + e.getMessage());
        }
    }

    public boolean messagesEnabled(UUID uuid) {
        return messagesEnabled.getOrDefault(uuid, true);
    }

    public void setMessagesEnabled(UUID uuid, boolean enabled) {
        messagesEnabled.put(uuid, enabled);
    }

    /** Flips the preference and returns the new state. */
    public boolean toggleMessages(UUID uuid) {
        boolean next = !messagesEnabled(uuid);
        messagesEnabled.put(uuid, next);
        return next;
    }
}

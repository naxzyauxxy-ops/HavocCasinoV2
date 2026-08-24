package net.havoccasino.message;

import net.havoccasino.HavocCasino;
import net.havoccasino.hook.HavocExpansion;
import net.havoccasino.hook.Papi;
import net.havoccasino.util.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * Loads customizable message templates from messages.yml and resolves them.
 *
 * Templates support MiniMessage formatting plus placeholders:
 *   - internal tokens like {amount}, {multiplier}, {player}, {pool}, {chance}
 *   - PlaceholderAPI %...% placeholders when PlaceholderAPI is installed
 *
 * Sending goes through {@link Msg}, so per-player message toggles are respected
 * (except {@link #force}, used for the toggle confirmation itself).
 */
public final class Messages {

    private final HavocCasino plugin;
    private final File file;
    private FileConfiguration cfg;

    public Messages(HavocCasino plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "messages.yml");
    }

    public void load() {
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    public String template(String key) {
        String value = cfg != null ? cfg.getString(key) : null;
        return value != null ? value : key;
    }

    private String resolve(CommandSender recipient, String key, String... pairs) {
        String text = template(key);
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            text = text.replace("{" + pairs[i] + "}", pairs[i + 1]);
        }
        if (recipient instanceof Player player) {
            text = text.replace("{player}", player.getName());
            if (Papi.AVAILABLE) {
                text = HavocExpansion.apply(player, text);
            }
        }
        return text;
    }

    /** Sends a prefixed message, respecting the player's message toggle. */
    public void send(CommandSender recipient, String key, String... pairs) {
        Msg.send(recipient, resolve(recipient, key, pairs));
    }

    /** Sends a prefixed message even if the player disabled messages. */
    public void force(CommandSender recipient, String key, String... pairs) {
        Msg.force(recipient, resolve(recipient, key, pairs));
    }

    /** Resolved component with no prefix (for GUI titles). */
    public Component line(CommandSender recipient, String key, String... pairs) {
        return Msg.parse(resolve(recipient, key, pairs));
    }

    /** Resolved component with italics disabled (for GUI item names/lore). */
    public Component item(CommandSender recipient, String key, String... pairs) {
        return Msg.item(resolve(recipient, key, pairs));
    }
}

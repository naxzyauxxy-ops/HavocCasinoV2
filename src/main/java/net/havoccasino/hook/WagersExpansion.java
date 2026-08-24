package net.havoccasino.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.havoccasino.HavocCasino;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Exposes %wagers_messages% for server settings dialogs (colored ON/OFF status
 * of a player's HavocCasino alerts) and %wagers_messages_raw% (plain ON/OFF).
 *
 * References PlaceholderAPI directly, so it is only loaded when PAPI is present —
 * always guard access behind {@link Papi#AVAILABLE}.
 */
public final class WagersExpansion extends PlaceholderExpansion {

    private final HavocCasino plugin;

    public WagersExpansion(HavocCasino plugin) {
        this.plugin = plugin;
    }

    public static void register(HavocCasino plugin) {
        new WagersExpansion(plugin).register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "wagers";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Havoc";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        switch (params.toLowerCase()) {
            case "messages": {
                boolean enabled = player != null
                        && plugin.playerSettings().messagesEnabled(player.getUniqueId());
                String raw = enabled
                        ? plugin.getConfig().getString("alerts-placeholder.on", "&aON")
                        : plugin.getConfig().getString("alerts-placeholder.off", "&cOFF");
                return ChatColor.translateAlternateColorCodes('&', raw);
            }
            case "messages_raw":
                return (player != null && plugin.playerSettings().messagesEnabled(player.getUniqueId()))
                        ? "ON" : "OFF";
            default:
                return null;
        }
    }
}

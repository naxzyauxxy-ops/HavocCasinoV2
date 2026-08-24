package net.havoccasino.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.havoccasino.HavocCasino;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Exposes HavocCasino placeholders and applies PlaceholderAPI to message text.
 *
 * This class references PlaceholderAPI directly, so it is ONLY loaded when PAPI
 * is installed — always guard access behind {@link Papi#AVAILABLE}.
 *
 * Provided placeholders:
 *   %havoccasino_jackpot%      -> formatted jackpot pool
 *   %havoccasino_jackpot_raw%  -> raw jackpot pool
 *   %havoccasino_messages%     -> ON / OFF (this player's message preference)
 */
public final class HavocExpansion extends PlaceholderExpansion {

    private final HavocCasino plugin;

    public HavocExpansion(HavocCasino plugin) {
        this.plugin = plugin;
    }

    /** Registers the expansion and marks PAPI available. Call only when PAPI is present. */
    public static void register(HavocCasino plugin) {
        new HavocExpansion(plugin).register();
        Papi.AVAILABLE = true;
    }

    /** Resolves %...% placeholders in text for the given player. */
    public static String apply(Player player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "havoccasino";
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
            case "jackpot":
                return plugin.currencyService().format(plugin.jackpotManager().pool());
            case "jackpot_raw":
                return String.valueOf(plugin.jackpotManager().pool());
            case "messages":
                return (player != null && plugin.playerSettings().messagesEnabled(player.getUniqueId()))
                        ? "ON" : "OFF";
            default:
                return null;
        }
    }
}

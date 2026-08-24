package net.havoccasino.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.havoccasino.config.CasinoConfig;
import net.havoccasino.settings.PlayerSettings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Low-level MiniMessage sender. {@link #send}/{@link #raw} respect each player's
 * per-player message toggle; the {@code force*} variants ignore it (used for the
 * toggle confirmation itself and admin output).
 */
public final class Msg {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static CasinoConfig config;
    private static PlayerSettings settings;

    private Msg() {
    }

    public static void init(CasinoConfig cfg, PlayerSettings playerSettings) {
        config = cfg;
        settings = playerSettings;
    }

    public static Component parse(String miniMessage) {
        return MM.deserialize(miniMessage);
    }

    /** Parses and disables the default italic on item names/lore. */
    public static Component item(String miniMessage) {
        return MM.deserialize(miniMessage).decoration(TextDecoration.ITALIC, false);
    }

    public static String prefix() {
        return config != null ? config.prefix() : "";
    }

    private static boolean allowed(CommandSender target) {
        if (target instanceof Player player) {
            return settings == null || settings.messagesEnabled(player.getUniqueId());
        }
        return true;
    }

    public static void send(CommandSender target, String miniMessage) {
        if (!allowed(target)) {
            return;
        }
        target.sendMessage(MM.deserialize(prefix() + miniMessage));
    }

    public static void raw(CommandSender target, String miniMessage) {
        if (!allowed(target)) {
            return;
        }
        target.sendMessage(MM.deserialize(miniMessage));
    }

    public static void force(CommandSender target, String miniMessage) {
        target.sendMessage(MM.deserialize(prefix() + miniMessage));
    }

    public static void forceRaw(CommandSender target, String miniMessage) {
        target.sendMessage(MM.deserialize(miniMessage));
    }
}

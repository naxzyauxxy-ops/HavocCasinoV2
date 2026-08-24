package net.havoccasino.command;

import net.havoccasino.HavocCasino;
import net.havoccasino.gui.CasinoMenuGui;
import net.havoccasino.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Opens the casino hub menu. Designed to be attached to an NPC, e.g. with
 * Citizens: {@code /npc command add casinomenu} (runs as the clicking player).
 */
public final class CasinoMenuCommand implements CommandExecutor {

    private final HavocCasino plugin;

    public CasinoMenuCommand(HavocCasino plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "<red>Only players can open the casino menu.");
            return true;
        }
        if (!player.hasPermission("havoccasino.menu")) {
            Msg.send(player, "<red>You don't have permission to use this.");
            return true;
        }
        new CasinoMenuGui(plugin, player).open();
        return true;
    }
}

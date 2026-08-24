package net.havoccasino.command;

import net.havoccasino.HavocCasino;
import net.havoccasino.util.Msg;
import net.havoccasino.util.Numbers;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SlotsCommand implements CommandExecutor {

    private final HavocCasino plugin;

    public SlotsCommand(HavocCasino plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "<red>Only players can play slots.");
            return true;
        }
        if (!player.hasPermission("havoccasino.slots")) {
            Msg.send(player, "<red>You don't have permission to use this.");
            return true;
        }
        if (args.length == 0) {
            Msg.send(player, "<gray>Usage: <white>/slots <bet>");
            return true;
        }

        Double parsed = Numbers.parsePositive(args[0]);
        if (parsed == null) {
            Msg.send(player, "<red>'" + args[0] + "' is not a valid bet.");
            return true;
        }
        plugin.games().openSlots(player, parsed);
        return true;
    }
}

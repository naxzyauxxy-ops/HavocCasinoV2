package net.havoccasino.command;

import net.havoccasino.HavocCasino;
import net.havoccasino.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class GamblingRoomCommand implements CommandExecutor {

    private final HavocCasino plugin;

    public GamblingRoomCommand(HavocCasino plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "<red>Only players can use this.");
            return true;
        }
        if (!player.hasPermission("havoccasino.room")) {
            Msg.send(player, "<red>You don't have permission to use this.");
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("delete")) {
            if (!plugin.roomManager().hasRoom(player.getUniqueId())) {
                plugin.messages().send(player, "room.none");
                return true;
            }
            plugin.roomManager().deleteRoom(player.getUniqueId());
            plugin.messages().send(player, "room.deleted");
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("tp")) {
            if (!plugin.roomManager().hasRoom(player.getUniqueId())) {
                plugin.messages().send(player, "room.none");
                return true;
            }
            plugin.roomManager().teleport(player);
            plugin.messages().send(player, "room.teleported");
            return true;
        }

        plugin.roomManager().createOrTeleport(player);
        return true;
    }
}

package net.havoccasino.command;

import net.havoccasino.HavocCasino;
import net.havoccasino.game.Crate;
import net.havoccasino.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class CratesCommand implements CommandExecutor, TabCompleter {

    private final HavocCasino plugin;

    public CratesCommand(HavocCasino plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "<red>Only players can open crates.");
            return true;
        }
        if (!player.hasPermission("havoccasino.crates")) {
            Msg.send(player, "<red>You don't have permission to use this.");
            return true;
        }
        if (plugin.crateManager().isEmpty()) {
            Msg.send(player, "<gray>No crates are configured.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            listCrates(player);
            return true;
        }

        Crate crate = plugin.crateManager().get(args[0]);
        if (crate == null) {
            plugin.messages().send(player, "crates.unknown", "name", args[0]);
            return true;
        }

        plugin.games().openCrate(player, crate);
        return true;
    }

    private void listCrates(Player player) {
        plugin.messages().send(player, "crates.list-header");
        for (Crate crate : plugin.crateManager().all()) {
            plugin.messages().send(player, "crates.list-entry",
                    "crate", crate.display(),
                    "id", crate.id(),
                    "cost", plugin.currencyService().format(crate.cost()));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length != 1 || !sender.hasPermission("havoccasino.crates")) {
            return List.of();
        }
        String prefix = args[0].toLowerCase();
        List<String> out = new ArrayList<>();
        out.add("list");
        for (Crate crate : plugin.crateManager().all()) {
            out.add(crate.id());
        }
        out.removeIf(s -> !s.toLowerCase().startsWith(prefix));
        return out;
    }
}

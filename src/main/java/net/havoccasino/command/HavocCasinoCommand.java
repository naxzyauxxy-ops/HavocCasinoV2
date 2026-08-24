package net.havoccasino.command;

import net.havoccasino.HavocCasino;
import net.havoccasino.gui.SettingsGui;
import net.havoccasino.schematic.SchematicService;
import net.havoccasino.util.Msg;
import net.havoccasino.util.Numbers;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class HavocCasinoCommand implements CommandExecutor, TabCompleter {

    private final HavocCasino plugin;

    public HavocCasinoCommand(HavocCasino plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        // Player-facing self toggle — available without admin permission.
        if (args.length >= 1 && args[0].equalsIgnoreCase("messages")) {
            return handleMessages(sender, args);
        }

        if (!sender.hasPermission("havoccasino.admin")) {
            Msg.force(sender, "<red>You don't have permission to use this.");
            return true;
        }
        if (args.length == 0) {
            help(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.casinoConfig().reload();
                plugin.crateManager().load();
                Msg.force(sender, "<green>Configuration and crates reloaded.");
                return true;
            case "jackpot":
                return handleJackpot(sender, args);
            case "wand":
                return handleWand(sender);
            case "schem":
                return handleSchem(sender, args);
            case "room":
                return handleRoom(sender, args);
            default:
                help(sender);
                return true;
        }
    }

    private boolean handleMessages(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.force(sender, "<red>Only players can change message settings.");
            return true;
        }
        if (!player.hasPermission("havoccasino.messages")) {
            Msg.force(player, "<red>You don't have permission to use this.");
            return true;
        }
        if (args.length >= 2) {
            String value = args[1].toLowerCase();
            if (value.equals("on") || value.equals("enable")) {
                plugin.playerSettings().setMessagesEnabled(player.getUniqueId(), true);
                plugin.playerSettings().save();
                plugin.messages().force(player, "settings.enabled");
                return true;
            }
            if (value.equals("off") || value.equals("disable")) {
                plugin.playerSettings().setMessagesEnabled(player.getUniqueId(), false);
                plugin.playerSettings().save();
                plugin.messages().force(player, "settings.disabled");
                return true;
            }
        }
        new SettingsGui(plugin, player).open();
        return true;
    }

    private boolean handleWand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            Msg.force(sender, "<red>Players only.");
            return true;
        }
        player.getInventory().addItem(plugin.wandItem().create());
        Msg.force(player, "<green>Casino wand added. <gray>Left-click = corner 1, right-click = corner 2, then <white>/hc schem save <name></white>.");
        return true;
    }

    private boolean handleSchem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.force(sender, "<red>Players only.");
            return true;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("list")) {
            java.util.List<String> names = plugin.schematics().list();
            Msg.force(player, "<gray>Schematics: <white>" + (names.isEmpty() ? "<none>" : String.join(", ", names)));
            return true;
        }
        if (args.length >= 3 && args[1].equalsIgnoreCase("save")) {
            SchematicService.SaveResult result = plugin.schematics().save(args[2], plugin.selections().get(player));
            switch (result) {
                case OK -> Msg.force(player, "<green>Saved schematic <white>" + args[2].toLowerCase() + "<green>. Set it as the room template with <white>/hc room schematic " + args[2].toLowerCase() + "</white>.");
                case INCOMPLETE -> Msg.force(player, "<red>Select both corners with the wand first.");
                case TOO_BIG -> Msg.force(player, "<red>Selection too large (max <white>" + plugin.schematics().maxVolume() + "</white> blocks).");
                case ERROR -> Msg.force(player, "<red>Failed to save (see console).");
            }
            return true;
        }
        Msg.force(sender, "<gray>Usage: <white>/hc schem <save <name>|list>");
        return true;
    }

    private boolean handleRoom(CommandSender sender, String[] args) {
        if (args.length >= 3 && args[1].equalsIgnoreCase("schematic")) {
            if (!plugin.schematics().exists(args[2])) {
                Msg.force(sender, "<red>No schematic named '<white>" + args[2] + "</white>'.");
                return true;
            }
            plugin.getConfig().set("rooms.schematic", args[2].toLowerCase());
            plugin.saveConfig();
            Msg.force(sender, "<green>Room schematic set to <white>" + args[2].toLowerCase() + "<green>.");
            return true;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("origin")) {
            if (!(sender instanceof Player player)) {
                Msg.force(sender, "<red>Players only for origin.");
                return true;
            }
            Location l = player.getLocation();
            plugin.getConfig().set("rooms.world", l.getWorld().getName());
            plugin.getConfig().set("rooms.origin.x", l.getBlockX());
            plugin.getConfig().set("rooms.origin.y", l.getBlockY());
            plugin.getConfig().set("rooms.origin.z", l.getBlockZ());
            plugin.saveConfig();
            Msg.force(player, "<green>Room paste origin set to <white>" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + "</white> in <white>" + l.getWorld().getName() + "</white>.");
            return true;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("info")) {
            var c = plugin.getConfig();
            Msg.force(sender, "<gray>Schematic: <white>" + c.getString("rooms.schematic", "<none>"));
            Msg.force(sender, "<gray>World: <white>" + c.getString("rooms.world", "world")
                    + " <gray>Origin: <white>" + c.getInt("rooms.origin.x", 0) + ", "
                    + c.getInt("rooms.origin.y", 100) + ", " + c.getInt("rooms.origin.z", 0));
            return true;
        }
        Msg.force(sender, "<gray>Usage: <white>/hc room <schematic <name>|origin|info>");
        return true;
    }

    private boolean handleJackpot(CommandSender sender, String[] args) {
        // /hc jackpot <set|add> <amount>
        if (args.length < 3) {
            Msg.force(sender, "<gray>Usage: <white>/hc jackpot <set|add> <amount>");
            return true;
        }
        Double amount = Numbers.parsePositive(args[2]);
        if (amount == null) {
            Msg.force(sender, "<red>Invalid amount.");
            return true;
        }
        if (args[1].equalsIgnoreCase("set")) {
            plugin.jackpotManager().setPool(amount);
        } else if (args[1].equalsIgnoreCase("add")) {
            plugin.jackpotManager().addToPool(amount);
        } else {
            Msg.force(sender, "<gray>Usage: <white>/hc jackpot <set|add> <amount>");
            return true;
        }
        plugin.jackpotManager().save();
        Msg.force(sender, "<green>Jackpot pool is now <gold>"
                + plugin.currencyService().format(plugin.jackpotManager().pool())
                + "<green>.");
        return true;
    }

    private void help(CommandSender sender) {
        Msg.force(sender, "<gold><bold>HavocCasino</bold> <gray>admin commands:");
        Msg.forceRaw(sender, "<gray>• <white>/hc reload");
        Msg.forceRaw(sender, "<gray>• <white>/hc jackpot <set|add> <amount>");
        Msg.forceRaw(sender, "<gray>• <white>/hc wand <gray>| <white>/hc schem save <name> <gray>| <white>/hc schem list");
        Msg.forceRaw(sender, "<gray>• <white>/hc room schematic <name> <gray>| <white>/hc room origin <gray>| <white>/hc room info");
        Msg.forceRaw(sender, "<gray>• <white>/hc messages <gray>(toggle your messages)");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            if (sender.hasPermission("havoccasino.messages")) {
                subs.add("messages");
            }
            if (sender.hasPermission("havoccasino.admin")) {
                subs.addAll(Arrays.asList("reload", "jackpot", "wand", "schem", "room"));
            }
            return filter(subs, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("messages")
                && sender.hasPermission("havoccasino.messages")) {
            return filter(Arrays.asList("on", "off"), args[1]);
        }
        if (!sender.hasPermission("havoccasino.admin")) {
            return List.of();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("jackpot")) {
            return filter(Arrays.asList("set", "add"), args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("schem")) {
            return filter(Arrays.asList("save", "list"), args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("room")) {
            return filter(Arrays.asList("schematic", "origin", "info"), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("room") && args[1].equalsIgnoreCase("schematic")) {
            return filter(plugin.schematics().list(), args[2]);
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> out = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lower)) {
                out.add(option);
            }
        }
        return out;
    }
}

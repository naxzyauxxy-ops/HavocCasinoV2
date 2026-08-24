package net.havoccasino.command;

import net.havoccasino.HavocCasino;
import net.havoccasino.economy.CurrencyService;
import net.havoccasino.util.Msg;
import net.havoccasino.util.Numbers;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class JackpotCommand implements CommandExecutor {

    private final HavocCasino plugin;

    public JackpotCommand(HavocCasino plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "<red>Only players can play the jackpot.");
            return true;
        }
        if (!player.hasPermission("havoccasino.jackpot")) {
            Msg.send(player, "<red>You don't have permission to use this.");
            return true;
        }

        CurrencyService bank = plugin.currencyService();

        if (args.length == 0) {
            plugin.messages().send(player, "jackpot.info-pool",
                    "pool", bank.format(plugin.jackpotManager().pool()));
            plugin.messages().send(player, "jackpot.info-howto",
                    "chance", Numbers.trim(plugin.casinoConfig().jackpotWinChance() * 100.0));
            return true;
        }

        Double parsed = Numbers.parsePositive(args[0]);
        if (parsed == null) {
            Msg.send(player, "<red>'" + args[0] + "' is not a valid amount.");
            return true;
        }
        plugin.games().enterJackpot(player, parsed);
        return true;
    }
}

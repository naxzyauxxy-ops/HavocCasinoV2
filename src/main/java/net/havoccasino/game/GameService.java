package net.havoccasino.game;

import net.havoccasino.HavocCasino;
import net.havoccasino.economy.CurrencyService;
import net.havoccasino.gui.CrateGui;
import net.havoccasino.gui.MinesGui;
import net.havoccasino.gui.SlotGui;
import net.havoccasino.util.Msg;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Central place for launching each game: validates the bet, takes the money,
 * and opens the relevant GUI or resolves the outcome. Both the slash commands
 * and the casino menu call these, so the rules stay identical everywhere.
 */
public final class GameService {

    private final HavocCasino plugin;

    public GameService(HavocCasino plugin) {
        this.plugin = plugin;
    }

    private boolean checkBet(Player player, double bet) {
        CurrencyService bank = plugin.currencyService();
        double min = plugin.casinoConfig().minBet();
        double max = plugin.casinoConfig().maxBet();
        if (bet < min || bet > max) {
            Msg.send(player, "<red>Bet must be between <white>" + bank.format(min)
                    + " <red>and <white>" + bank.format(max) + "<red>.");
            return false;
        }
        if (!bank.has(player, bet)) {
            Msg.send(player, "<red>You can't afford that bet. Balance: <white>"
                    + bank.format(bank.balance(player)));
            return false;
        }
        return true;
    }

    public boolean openSlots(Player player, double bet) {
        if (!checkBet(player, bet)) {
            return false;
        }
        if (!plugin.currencyService().withdraw(player, bet)) {
            Msg.send(player, "<red>Transaction failed. Try again.");
            return false;
        }
        SlotResult result = plugin.slotMachine().spin(bet);
        new SlotGui(plugin, player, result, bet).open();
        return true;
    }

    public boolean openMines(Player player, double bet, int mines) {
        int minMines = Math.max(1, plugin.casinoConfig().minesMin());
        int maxMines = Math.min(MinesGui.TOTAL_TILES - 1, plugin.casinoConfig().minesMax());
        if (mines < minMines || mines > maxMines) {
            Msg.send(player, "<red>Mines must be between <white>" + minMines
                    + " <red>and <white>" + maxMines + "<red>.");
            return false;
        }
        if (!checkBet(player, bet)) {
            return false;
        }
        if (!plugin.currencyService().withdraw(player, bet)) {
            Msg.send(player, "<red>Transaction failed. Try again.");
            return false;
        }
        new MinesGui(plugin, player, bet, mines, plugin.casinoConfig().minesHouseEdge()).open();
        return true;
    }

    public boolean enterJackpot(Player player, double bet) {
        CurrencyService bank = plugin.currencyService();
        double minEntry = plugin.casinoConfig().jackpotMinBet();
        if (bet < minEntry) {
            Msg.send(player, "<red>Minimum entry is <white>" + bank.format(minEntry) + "<red>.");
            return false;
        }
        if (!bank.has(player, bet)) {
            Msg.send(player, "<red>You can't afford that. Balance: <white>"
                    + bank.format(bank.balance(player)));
            return false;
        }
        if (!bank.withdraw(player, bet)) {
            Msg.send(player, "<red>Transaction failed. Try again.");
            return false;
        }

        JackpotManager jackpot = plugin.jackpotManager();
        JackpotManager.JackpotOutcome outcome = jackpot.roll(bet);
        jackpot.save();

        if (outcome.won) {
            bank.deposit(player, outcome.amountWon);
            if (player.isOnline()) {
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }
            String amount = bank.format(outcome.amountWon);
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                plugin.messages().send(online, "jackpot.win-broadcast",
                        "player", player.getName(), "amount", amount);
            }
        } else {
            plugin.messages().send(player, "jackpot.lose", "pool", bank.format(outcome.newPool));
        }
        return true;
    }

    public boolean openCrate(Player player, Crate crate) {
        CurrencyService bank = plugin.currencyService();
        if (!bank.has(player, crate.cost())) {
            Msg.send(player, "<red>You can't afford that crate. Cost: <white>"
                    + bank.format(crate.cost()) + " <red>· Balance: <white>"
                    + bank.format(bank.balance(player)));
            return false;
        }
        if (!bank.withdraw(player, crate.cost())) {
            Msg.send(player, "<red>Transaction failed. Try again.");
            return false;
        }
        CrateReward winner = crate.roll(ThreadLocalRandom.current());
        new CrateGui(plugin, player, crate, winner).open();
        return true;
    }
}

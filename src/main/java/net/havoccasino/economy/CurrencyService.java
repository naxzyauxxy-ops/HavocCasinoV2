package net.havoccasino.economy;

import net.havoccasino.config.CasinoConfig;
import net.havoccasino.util.Numbers;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

/**
 * Money transactions via Vault. This plugin uses server money only.
 */
public final class CurrencyService {

    private final VaultHook vault;
    private final CasinoConfig config;
    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");

    public CurrencyService(VaultHook vault, CasinoConfig config) {
        this.vault = vault;
        this.config = config;
    }

    public boolean isReady() {
        return vault.isEnabled();
    }

    public double balance(Player player) {
        return vault.isEnabled() ? vault.economy().getBalance(player) : 0D;
    }

    public boolean has(Player player, double amount) {
        return vault.isEnabled() && vault.economy().has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        return vault.isEnabled() && vault.economy().withdrawPlayer(player, amount).transactionSuccess();
    }

    public void deposit(Player player, double amount) {
        if (vault.isEnabled()) {
            vault.economy().depositPlayer(player, amount);
        }
    }

    public String format(double amount) {
        if (config.compactDisplay() && Math.abs(amount) >= 1000) {
            return config.moneySymbol() + Numbers.compact(amount);
        }
        return config.moneySymbol() + moneyFormat.format(amount);
    }
}

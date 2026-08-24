package net.havoccasino.config;

import net.havoccasino.HavocCasino;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Typed view over config.yml. Call reload() to refresh from disk.
 */
public final class CasinoConfig {

    private final HavocCasino plugin;

    private String moneySymbol;
    private boolean compactDisplay;
    private double minBet;
    private double maxBet;
    private double slotTwoMatchMultiplier;
    private int minesDefault;
    private int minesMin;
    private int minesMax;
    private double minesHouseEdge;
    private double jackpotSeed;
    private double jackpotRake;
    private double jackpotWinChance;
    private double jackpotMinBet;
    private String prefix;

    public CasinoConfig(HavocCasino plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration c = plugin.getConfig();

        moneySymbol = c.getString("currency.money-symbol", "$");
        compactDisplay = c.getBoolean("currency.compact-display", true);

        minBet = c.getDouble("betting.min-bet", 10);
        maxBet = c.getDouble("betting.max-bet", 10000);

        slotTwoMatchMultiplier = c.getDouble("slots.two-match-multiplier", 1.5);

        minesDefault = c.getInt("mines.default-mines", 3);
        minesMin = c.getInt("mines.min-mines", 1);
        minesMax = c.getInt("mines.max-mines", 24);
        minesHouseEdge = c.getDouble("mines.house-edge", 0.03);

        jackpotSeed = c.getDouble("jackpot.seed", 1000);
        jackpotRake = c.getDouble("jackpot.contribution-percent", 0.85);
        jackpotWinChance = c.getDouble("jackpot.win-chance", 0.02);
        jackpotMinBet = c.getDouble("jackpot.min-entry", 50);

        prefix = c.getString("messages.prefix",
                "<gradient:#f7971e:#ffd200><bold>HavocCasino</bold></gradient> <dark_gray>»</dark_gray> ");
    }

    public String moneySymbol() {
        return moneySymbol;
    }

    public boolean compactDisplay() {
        return compactDisplay;
    }

    public double minBet() {
        return minBet;
    }

    public double maxBet() {
        return maxBet;
    }

    public double slotTwoMatchMultiplier() {
        return slotTwoMatchMultiplier;
    }

    public int minesDefault() {
        return minesDefault;
    }

    public int minesMin() {
        return minesMin;
    }

    public int minesMax() {
        return minesMax;
    }

    public double minesHouseEdge() {
        return minesHouseEdge;
    }

    public double jackpotSeed() {
        return jackpotSeed;
    }

    public double jackpotRake() {
        return jackpotRake;
    }

    public double jackpotWinChance() {
        return jackpotWinChance;
    }

    public double jackpotMinBet() {
        return jackpotMinBet;
    }

    public String prefix() {
        return prefix;
    }
}

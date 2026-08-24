package net.havoccasino.game;

import net.havoccasino.HavocCasino;
import net.havoccasino.config.CasinoConfig;
import net.havoccasino.economy.CurrencyService;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Holds and persists the progressive jackpot pool.
 */
public final class JackpotManager {

    private final HavocCasino plugin;
    private final CasinoConfig config;
    private final CurrencyService currency;
    private final File file;
    private double pool;

    public JackpotManager(HavocCasino plugin, CasinoConfig config, CurrencyService currency) {
        this.plugin = plugin;
        this.config = config;
        this.currency = currency;
        this.file = new File(plugin.getDataFolder(), "jackpot.yml");
    }

    public void load() {
        if (file.exists()) {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            pool = cfg.getDouble("pool", config.jackpotSeed());
        } else {
            pool = config.jackpotSeed();
        }
    }

    public void save() {
        FileConfiguration cfg = new YamlConfiguration();
        cfg.set("pool", pool);
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save jackpot.yml: " + e.getMessage());
        }
    }

    public double pool() {
        return pool;
    }

    public void setPool(double value) {
        pool = Math.max(0, value);
    }

    public void addToPool(double amount) {
        pool += amount;
    }

    /**
     * Adds the entry's contribution to the pool and rolls for a win.
     */
    public JackpotOutcome roll(double bet) {
        addToPool(bet * config.jackpotRake());
        boolean won = ThreadLocalRandom.current().nextDouble() < config.jackpotWinChance();
        if (won) {
            double winnings = pool;
            pool = config.jackpotSeed();
            return new JackpotOutcome(true, winnings, pool);
        }
        return new JackpotOutcome(false, 0, pool);
    }

    public static final class JackpotOutcome {
        public final boolean won;
        public final double amountWon;
        public final double newPool;

        JackpotOutcome(boolean won, double amountWon, double newPool) {
            this.won = won;
            this.amountWon = amountWon;
            this.newPool = newPool;
        }
    }
}

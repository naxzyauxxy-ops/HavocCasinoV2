package net.havoccasino.game;

import org.bukkit.Material;

import java.util.Random;

/**
 * Reel symbols. Rarer symbols (lower weight) pay a higher triple multiplier.
 * The house edge is baked into these weights vs. payouts.
 */
public enum SlotSymbol {

    CHERRY(Material.SWEET_BERRIES, "<red>Cherry", 30, 3),
    LEMON(Material.GOLD_NUGGET, "<yellow>Lemon", 26, 4),
    BELL(Material.BELL, "<gold>Bell", 18, 6),
    DIAMOND(Material.DIAMOND, "<aqua>Diamond", 12, 12),
    RUBY(Material.REDSTONE, "<dark_red>Ruby", 8, 25),
    SEVEN(Material.NETHER_STAR, "<light_purple><bold>7", 4, 77);

    private final Material material;
    private final String display;
    private final int weight;
    private final double tripleMultiplier;

    SlotSymbol(Material material, String display, int weight, double tripleMultiplier) {
        this.material = material;
        this.display = display;
        this.weight = weight;
        this.tripleMultiplier = tripleMultiplier;
    }

    public Material material() {
        return material;
    }

    public String display() {
        return display;
    }

    public double tripleMultiplier() {
        return tripleMultiplier;
    }

    private static final int TOTAL_WEIGHT;

    static {
        int total = 0;
        for (SlotSymbol symbol : values()) {
            total += symbol.weight;
        }
        TOTAL_WEIGHT = total;
    }

    public static SlotSymbol random(Random random) {
        int roll = random.nextInt(TOTAL_WEIGHT);
        int cumulative = 0;
        for (SlotSymbol symbol : values()) {
            cumulative += symbol.weight;
            if (roll < cumulative) {
                return symbol;
            }
        }
        return CHERRY;
    }
}

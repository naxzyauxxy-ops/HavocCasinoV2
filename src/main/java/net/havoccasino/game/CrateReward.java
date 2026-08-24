package net.havoccasino.game;

import org.bukkit.Material;

/**
 * A single weighted outcome inside a crate. Payout is the crate cost times the
 * multiplier; a multiplier of 0 is a bust (the player loses their cost).
 */
public final class CrateReward {

    private final String name;
    private final Material material;
    private final int weight;
    private final double multiplier;

    public CrateReward(String name, Material material, int weight, double multiplier) {
        this.name = name;
        this.material = material;
        this.weight = Math.max(1, weight);
        this.multiplier = Math.max(0, multiplier);
    }

    public String name() {
        return name;
    }

    public Material material() {
        return material;
    }

    public int weight() {
        return weight;
    }

    public double multiplier() {
        return multiplier;
    }

    public boolean isBust() {
        return multiplier <= 0;
    }
}

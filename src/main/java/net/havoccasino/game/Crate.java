package net.havoccasino.game;

import org.bukkit.Material;

import java.util.List;
import java.util.Random;

/**
 * A named crate: a cost and a weighted table of rewards.
 */
public final class Crate {

    private final String id;
    private final String display;
    private final double cost;
    private final Material icon;
    private final List<CrateReward> rewards;
    private final int totalWeight;

    public Crate(String id, String display, double cost, Material icon, List<CrateReward> rewards) {
        this.id = id;
        this.display = display;
        this.cost = cost;
        this.icon = icon;
        this.rewards = rewards;
        int total = 0;
        for (CrateReward reward : rewards) {
            total += reward.weight();
        }
        this.totalWeight = Math.max(1, total);
    }

    public String id() {
        return id;
    }

    public String display() {
        return display;
    }

    public double cost() {
        return cost;
    }

    public Material icon() {
        return icon;
    }

    public List<CrateReward> rewards() {
        return rewards;
    }

    public CrateReward roll(Random random) {
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (CrateReward reward : rewards) {
            cumulative += reward.weight();
            if (roll < cumulative) {
                return reward;
            }
        }
        return rewards.get(rewards.size() - 1);
    }
}

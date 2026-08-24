package net.havoccasino.game;

import net.havoccasino.config.CasinoConfig;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class SlotMachine {

    private final CasinoConfig config;

    public SlotMachine(CasinoConfig config) {
        this.config = config;
    }

    public SlotResult spin(double bet) {
        Random random = ThreadLocalRandom.current();
        SlotSymbol[] reels = new SlotSymbol[] {
                SlotSymbol.random(random),
                SlotSymbol.random(random),
                SlotSymbol.random(random)
        };
        return new SlotResult(reels, evaluate(reels), bet);
    }

    private double evaluate(SlotSymbol[] r) {
        if (r[0] == r[1] && r[1] == r[2]) {
            return r[0].tripleMultiplier();
        }
        if (r[0] == r[1] || r[1] == r[2] || r[0] == r[2]) {
            return config.slotTwoMatchMultiplier();
        }
        return 0;
    }
}

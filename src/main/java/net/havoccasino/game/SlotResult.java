package net.havoccasino.game;

public final class SlotResult {

    private final SlotSymbol[] reels;
    private final double multiplier;
    private final double payout;

    public SlotResult(SlotSymbol[] reels, double multiplier, double bet) {
        this.reels = reels;
        this.multiplier = multiplier;
        this.payout = bet * multiplier;
    }

    public SlotSymbol[] reels() {
        return reels;
    }

    public double multiplier() {
        return multiplier;
    }

    public double payout() {
        return payout;
    }

    public boolean win() {
        return multiplier > 0;
    }
}

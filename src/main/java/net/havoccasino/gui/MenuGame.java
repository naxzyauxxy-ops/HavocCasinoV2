package net.havoccasino.gui;

/** Games reachable from the casino menu's bet picker. */
public enum MenuGame {
    SLOTS("Slots"),
    MINES("Mines"),
    JACKPOT("Jackpot");

    private final String display;

    MenuGame(String display) {
        this.display = display;
    }

    public String display() {
        return display;
    }
}

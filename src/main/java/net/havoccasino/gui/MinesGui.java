package net.havoccasino.gui;

import net.kyori.adventure.text.Component;
import net.havoccasino.HavocCasino;
import net.havoccasino.util.Msg;
import net.havoccasino.util.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A 5x5 mines board. The player reveals tiles one at a time; every safe tile
 * raises the multiplier. Hitting a mine loses the bet; cashing out banks
 * bet * current multiplier. When the round ends the board shows Play Again /
 * Change / Close buttons. Closing mid-round auto-cashes-out (or refunds).
 *
 * The bet is withdrawn before the board opens; only payouts are deposited here.
 */
public final class MinesGui {

    public static final int TOTAL_TILES = 25;

    private static final int SIZE = 54;
    private static final int GRID_DIM = 5;
    private static final int CASHOUT_SLOT = 49;
    private static final int BET_SLOT = 46;
    private static final int MINES_SLOT = 52;
    private static final int PLAY_AGAIN_SLOT = 47;
    private static final int CHANGE_SLOT = 49;
    private static final int CLOSE_SLOT = 51;

    private final HavocCasino plugin;
    private final Player player;
    private final double bet;
    private final int mines;
    private final double houseEdge;
    private final Inventory inventory;

    private final int[] gridSlots = new int[TOTAL_TILES];
    private final Map<Integer, Integer> slotToIndex = new HashMap<>();
    private final boolean[] isMine = new boolean[TOTAL_TILES];
    private final boolean[] revealed = new boolean[TOTAL_TILES];

    private int safeRevealed = 0;
    private boolean finished = false;

    public MinesGui(HavocCasino plugin, Player player, double bet,
                    int mines, double houseEdge) {
        this.plugin = plugin;
        this.player = player;
        this.bet = bet;
        this.mines = mines;
        this.houseEdge = houseEdge;

        MinesHolder holder = new MinesHolder();
        this.inventory = Bukkit.createInventory(holder, SIZE, Msg.parse("<dark_gray>✦ <red>Mines</red> ✦"));
        holder.setInventory(inventory);
        holder.setGui(this);

        computeGridSlots();
        placeMines();
        render();
    }

    public void open() {
        player.openInventory(inventory);
    }

    /** Fair multiplier for surviving {@code revealed} picks with {@code mines} mines. */
    public static double multiplier(int mines, int revealed, double houseEdge) {
        if (revealed <= 0) {
            return 1.0;
        }
        double product = 1.0;
        for (int i = 0; i < revealed; i++) {
            int denom = TOTAL_TILES - mines - i;
            if (denom <= 0) {
                break;
            }
            product *= (double) (TOTAL_TILES - i) / denom;
        }
        return product * (1.0 - houseEdge);
    }

    // ---- interaction, called from the listener ----

    public void handleClick(int rawSlot) {
        if (finished) {
            if (rawSlot == PLAY_AGAIN_SLOT) {
                plugin.games().openMines(player, bet, mines);
            } else if (rawSlot == CHANGE_SLOT) {
                new MinesSetupGui(plugin, player).open();
            } else if (rawSlot == CLOSE_SLOT) {
                player.closeInventory();
            }
            return;
        }
        if (rawSlot == CASHOUT_SLOT) {
            attemptCashout();
            return;
        }
        Integer index = slotToIndex.get(rawSlot);
        if (index == null || revealed[index]) {
            return;
        }
        reveal(index);
    }

    public void handleClose() {
        if (finished) {
            return;
        }
        finished = true;
        if (safeRevealed == 0) {
            plugin.currencyService().deposit(player, bet);
            plugin.messages().send(player, "mines.refund", "amount", fmt(bet));
        } else {
            double payout = bet * currentMultiplier();
            plugin.currencyService().deposit(player, payout);
            plugin.messages().send(player, "mines.auto-cashout",
                    "amount", fmt(payout), "multiplier", Numbers.trim(currentMultiplier()));
        }
    }

    // ---- game logic ----

    private void reveal(int index) {
        revealed[index] = true;
        if (isMine[index]) {
            explode(index);
            return;
        }
        safeRevealed++;
        inventory.setItem(gridSlots[index], safeItem());
        playRevealSound();
        updateCashoutButton();

        if (safeRevealed >= TOTAL_TILES - mines) {
            finished = true;
            double payout = bet * currentMultiplier();
            plugin.currencyService().deposit(player, payout);
            plugin.messages().send(player, "mines.board-cleared",
                    "amount", fmt(payout), "multiplier", Numbers.trim(currentMultiplier()));
            if (player.isOnline()) {
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }
            showResultButtons();
        }
    }

    private void explode(int index) {
        finished = true;
        inventory.setItem(gridSlots[index], explodedItem());
        for (int i = 0; i < TOTAL_TILES; i++) {
            if (isMine[i] && i != index) {
                inventory.setItem(gridSlots[i], mineItem());
            }
        }
        if (player.isOnline()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
        }
        plugin.messages().send(player, "mines.boom", "amount", fmt(bet));
        showResultButtons();
    }

    private void attemptCashout() {
        if (safeRevealed == 0) {
            plugin.messages().send(player, "mines.cashout-need-reveal");
            return;
        }
        finished = true;
        double payout = bet * currentMultiplier();
        plugin.currencyService().deposit(player, payout);
        plugin.messages().send(player, "mines.cashout",
                "amount", fmt(payout), "multiplier", Numbers.trim(currentMultiplier()));
        if (player.isOnline()) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        }
        showResultButtons();
    }

    private double currentMultiplier() {
        return multiplier(mines, safeRevealed, houseEdge);
    }

    // ---- setup ----

    private void computeGridSlots() {
        int idx = 0;
        for (int r = 0; r < GRID_DIM; r++) {
            for (int c = 0; c < GRID_DIM; c++) {
                int slot = r * 9 + (c + 2);
                gridSlots[idx] = slot;
                slotToIndex.put(slot, idx);
                idx++;
            }
        }
    }

    private void placeMines() {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < TOTAL_TILES; i++) {
            positions.add(i);
        }
        Collections.shuffle(positions, ThreadLocalRandom.current());
        for (int i = 0; i < mines; i++) {
            isMine[positions.get(i)] = true;
        }
    }

    private void render() {
        ItemStack filler = icon(Material.BLACK_STAINED_GLASS_PANE, "<gray>");
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, filler);
        }
        for (int idx = 0; idx < TOTAL_TILES; idx++) {
            inventory.setItem(gridSlots[idx], hiddenItem());
        }
        inventory.setItem(BET_SLOT, icon(Material.PAPER, "<yellow>Your bet", "<white>" + fmt(bet)));
        inventory.setItem(MINES_SLOT, icon(Material.TNT,
                "<red>Mines: <white>" + mines + " / " + TOTAL_TILES,
                "<gray>Safe tiles: <white>" + (TOTAL_TILES - mines)));
        inventory.setItem(CASHOUT_SLOT, cashoutItem());
    }

    private void updateCashoutButton() {
        inventory.setItem(CASHOUT_SLOT, cashoutItem());
    }

    private void showResultButtons() {
        inventory.setItem(PLAY_AGAIN_SLOT, icon(Material.EMERALD_BLOCK, "<green><bold>Play Again</bold>",
                "<gray>Same bet & mines",
                "<gray>Bet: <white>" + fmt(bet),
                "<gray>Mines: <white>" + mines));
        inventory.setItem(CHANGE_SLOT, icon(Material.ANVIL, "<yellow><bold>Change Bet / Mines</bold>",
                "<gray>Pick a new bet or mine count."));
        inventory.setItem(CLOSE_SLOT, icon(Material.BARRIER, "<red><bold>Close</bold>", "<gray>Leave the board."));
    }

    // ---- items ----

    private ItemStack cashoutItem() {
        if (safeRevealed == 0) {
            double firstChance = 100.0 * (TOTAL_TILES - mines) / TOTAL_TILES;
            return icon(Material.GOLD_NUGGET, "<gold><bold>Cash Out</bold>",
                    "<gray>Reveal a tile to start.",
                    "<gray>Safe chance: <white>" + Numbers.trim(firstChance) + "%",
                    "<gray>Next multiplier: <white>x" + Numbers.trim(multiplier(mines, 1, houseEdge)));
        }
        double mult = currentMultiplier();
        int remainingTiles = TOTAL_TILES - safeRevealed;
        int remainingSafe = (TOTAL_TILES - mines) - safeRevealed;
        double nextChance = remainingTiles > 0 ? 100.0 * remainingSafe / remainingTiles : 0;
        double nextMult = multiplier(mines, safeRevealed + 1, houseEdge);
        return icon(Material.GOLD_INGOT, "<gold><bold>Cash Out</bold>",
                "<gray>Multiplier: <white>x" + Numbers.trim(mult),
                "<gray>Payout: <green>" + fmt(bet * mult),
                "<gray>Next pick safe: <white>" + Numbers.trim(nextChance) + "%",
                "<gray>Next multiplier: <white>x" + Numbers.trim(nextMult));
    }

    private ItemStack hiddenItem() {
        return icon(Material.GRAY_CONCRETE, "<white>?", "<dark_gray>Click to reveal");
    }

    private ItemStack safeItem() {
        return icon(Material.EMERALD, "<green>Safe",
                "<gray>Multiplier: <white>x" + Numbers.trim(currentMultiplier()));
    }

    private ItemStack mineItem() {
        return icon(Material.TNT, "<red>Mine");
    }

    private ItemStack explodedItem() {
        return icon(Material.FIRE_CHARGE, "<dark_red><bold>BOOM!</bold>");
    }

    private ItemStack icon(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Msg.item(name));
            if (loreLines.length > 0) {
                List<Component> lore = new ArrayList<>();
                for (String line : loreLines) {
                    lore.add(Msg.item(line));
                }
                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    // ---- helpers ----

    private String fmt(double amount) {
        return plugin.currencyService().format(amount);
    }

    private void playRevealSound() {
        if (!player.isOnline()) {
            return;
        }
        float pitch = (float) Math.min(2.0, 0.8 + safeRevealed * 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, pitch);
    }
}

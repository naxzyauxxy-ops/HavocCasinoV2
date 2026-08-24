package net.havoccasino.gui;

import net.havoccasino.HavocCasino;
import net.havoccasino.util.Msg;
import net.havoccasino.util.Numbers;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One screen to set up a Mines round: pick a bet and a mine count (both show the
 * odds), then hit Play. Defaults are pre-selected so a player can just click Play.
 */
public final class MinesSetupGui implements Clickable {

    private static final int SIZE = 54;
    private static final int PLAY_SLOT = 49;
    private static final int[] BET_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int[] MINE_SLOTS = {28, 29, 30, 31, 32, 33, 34};
    private static final int[] MINE_CANDIDATES = {1, 2, 3, 5, 8, 12, 18, 24};

    private final HavocCasino plugin;
    private final Player player;
    private final Inventory inventory;
    private final Map<Integer, Double> betOptions = new HashMap<>();
    private final Map<Integer, Integer> mineOptions = new HashMap<>();

    private double selectedBet;
    private int selectedMines;

    public MinesSetupGui(HavocCasino plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        List<Double> presets = presets();
        this.selectedBet = presets.isEmpty() ? plugin.casinoConfig().minBet() : presets.get(0);
        this.selectedMines = clampMines(plugin.casinoConfig().minesDefault());

        MenuHolder holder = new MenuHolder();
        this.inventory = Bukkit.createInventory(holder, SIZE, plugin.messages().line(player, "menu.mines-title"));
        holder.setInventory(inventory);
        holder.setClickable(this);
        render();
    }

    public void open() {
        player.openInventory(inventory);
    }

    private List<Double> presets() {
        List<Double> list = plugin.getConfig().getDoubleList("casino-menu.bet-presets");
        if (list == null || list.isEmpty()) {
            return List.of(10.0, 50.0, 100.0, 500.0, 1000.0);
        }
        return list;
    }

    private int minMines() {
        return Math.max(1, plugin.casinoConfig().minesMin());
    }

    private int maxMines() {
        return Math.min(MinesGui.TOTAL_TILES - 1, plugin.casinoConfig().minesMax());
    }

    private int clampMines(int value) {
        return Math.max(minMines(), Math.min(maxMines(), value));
    }

    private void render() {
        betOptions.clear();
        mineOptions.clear();

        ItemStack filler = icon(Material.BLACK_STAINED_GLASS_PANE, "<gray>", null);
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, filler);
        }
        inventory.setItem(4, icon(Material.PAPER, "<gold><bold>Choose your bet",
                List.of("<gray>Pick a bet below, then a mine count.")));
        inventory.setItem(22, icon(Material.TNT, "<gold><bold>Choose mines",
                List.of("<gray>More mines = higher risk, bigger multiplier.")));

        List<Double> presets = presets();
        double edge = plugin.casinoConfig().minesHouseEdge();

        for (int i = 0; i < BET_SLOTS.length && i < presets.size(); i++) {
            double amount = presets.get(i);
            betOptions.put(BET_SLOTS[i], amount);
            boolean sel = amount == selectedBet;
            inventory.setItem(BET_SLOTS[i], icon(sel ? Material.GOLD_BLOCK : Material.GOLD_NUGGET,
                    (sel ? "<green>" : "<yellow>") + "<bold>" + plugin.currencyService().format(amount),
                    List.of(sel ? "<green>✔ Selected" : "<gray>Click to select")));
        }

        int idx = 0;
        for (int candidate : MINE_CANDIDATES) {
            if (candidate < minMines() || candidate > maxMines() || idx >= MINE_SLOTS.length) {
                continue;
            }
            int slot = MINE_SLOTS[idx++];
            mineOptions.put(slot, candidate);
            boolean sel = candidate == selectedMines;
            double firstChance = 100.0 * (MinesGui.TOTAL_TILES - candidate) / MinesGui.TOTAL_TILES;
            double clearedMult = MinesGui.multiplier(candidate, MinesGui.TOTAL_TILES - candidate, edge);
            inventory.setItem(slot, icon(sel ? Material.REDSTONE_BLOCK : Material.TNT,
                    (sel ? "<green>" : "<red>") + "<bold>" + candidate + " Mines",
                    List.of(
                            sel ? "<green>✔ Selected" : "<gray>Click to select",
                            "<gray>Safe chance/pick: <white>" + Numbers.trim(firstChance) + "%",
                            "<gray>x" + Numbers.trim(MinesGui.multiplier(candidate, 1, edge)) + " <gray>after 1 safe",
                            "<gray>Cleared: <white>x" + Numbers.compact(clearedMult))));
        }

        inventory.setItem(PLAY_SLOT, playItem(edge));
    }

    private ItemStack playItem(double edge) {
        double firstChance = 100.0 * (MinesGui.TOTAL_TILES - selectedMines) / MinesGui.TOTAL_TILES;
        double clearedMult = MinesGui.multiplier(selectedMines, MinesGui.TOTAL_TILES - selectedMines, edge);
        return icon(Material.LIME_CONCRETE, "<green><bold>PLAY</bold>",
                List.of(
                        "<gray>Bet: <white>" + plugin.currencyService().format(selectedBet),
                        "<gray>Mines: <white>" + selectedMines + " / " + MinesGui.TOTAL_TILES,
                        "<gray>First pick safe: <white>" + Numbers.trim(firstChance) + "%",
                        "<gray>Max payout: <green>" + plugin.currencyService().format(selectedBet * clearedMult),
                        "<yellow>Click to play!"));
    }

    @Override
    public void handleClick(int slot) {
        if (betOptions.containsKey(slot)) {
            selectedBet = betOptions.get(slot);
            render();
            return;
        }
        if (mineOptions.containsKey(slot)) {
            selectedMines = mineOptions.get(slot);
            render();
            return;
        }
        if (slot == PLAY_SLOT) {
            plugin.games().openMines(player, selectedBet, selectedMines);
        }
    }

    private ItemStack icon(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Msg.item(name));
            if (lore != null) {
                List<Component> lines = new ArrayList<>();
                for (String line : lore) {
                    lines.add(Msg.item(line));
                }
                meta.lore(lines);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}

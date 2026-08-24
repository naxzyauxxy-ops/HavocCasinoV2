package net.havoccasino.gui;

import net.havoccasino.HavocCasino;
import net.havoccasino.util.Msg;
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
 * Presents configurable preset bet buttons for a chosen game.
 */
public final class AmountPickerGui implements Clickable {

    private static final int SIZE = 27;
    private static final int BACK = 22;
    private static final int[] BUTTONS = {10, 11, 12, 13, 14, 15, 16};

    private final HavocCasino plugin;
    private final Player player;
    private final MenuGame game;
    private final Inventory inventory;
    private final Map<Integer, Double> amounts = new HashMap<>();

    public AmountPickerGui(HavocCasino plugin, Player player, MenuGame game) {
        this.plugin = plugin;
        this.player = player;
        this.game = game;

        MenuHolder holder = new MenuHolder();
        this.inventory = Bukkit.createInventory(holder, SIZE,
                plugin.messages().line(player, "menu.bets-title", "game", game.display()));
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

    private void render() {
        ItemStack filler = icon(Material.BLACK_STAINED_GLASS_PANE, "<gray>");
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, filler);
        }
        List<Double> presets = presets();
        for (int i = 0; i < BUTTONS.length && i < presets.size(); i++) {
            double amount = presets.get(i);
            amounts.put(BUTTONS[i], amount);
            inventory.setItem(BUTTONS[i], icon(Material.GOLD_NUGGET,
                    "<green><bold>Bet " + plugin.currencyService().format(amount), "<gray>Click to play."));
        }
        inventory.setItem(BACK, icon(Material.BARRIER, "<red>Back", "<gray>Return to the menu."));
    }

    @Override
    public void handleClick(int slot) {
        if (slot == BACK) {
            new CasinoMenuGui(plugin, player).open();
            return;
        }
        Double amount = amounts.get(slot);
        if (amount == null) {
            return;
        }
        player.closeInventory();
        switch (game) {
            case SLOTS -> plugin.games().openSlots(player, amount);
            case MINES -> plugin.games().openMines(player, amount, plugin.casinoConfig().minesDefault());
            case JACKPOT -> plugin.games().enterJackpot(player, amount);
        }
    }

    private ItemStack icon(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Msg.item(name));
            if (lore.length > 0) {
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

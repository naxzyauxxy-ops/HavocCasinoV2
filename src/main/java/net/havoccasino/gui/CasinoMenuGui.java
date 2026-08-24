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
import java.util.List;

/**
 * The casino hub. Attach {@code /casinomenu} to an NPC so clicking it opens this.
 */
public final class CasinoMenuGui implements Clickable {

    private static final int SIZE = 27;
    private static final int SLOTS = 10;
    private static final int MINES = 12;
    private static final int JACKPOT = 14;
    private static final int CRATES = 16;
    private static final int ROOM = 22;

    private final HavocCasino plugin;
    private final Player player;
    private final Inventory inventory;

    public CasinoMenuGui(HavocCasino plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        MenuHolder holder = new MenuHolder();
        this.inventory = Bukkit.createInventory(holder, SIZE, plugin.messages().line(player, "menu.title"));
        holder.setInventory(inventory);
        holder.setClickable(this);
        render();
    }

    public void open() {
        player.openInventory(inventory);
    }

    private void render() {
        ItemStack filler = icon(Material.BLACK_STAINED_GLASS_PANE, "<gray>");
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, filler);
        }
        inventory.setItem(SLOTS, icon(Material.GOLD_INGOT, "<gold><bold>Slots", "<gray>Spin the reels."));
        inventory.setItem(MINES, icon(Material.TNT, "<red><bold>Mines", "<gray>Reveal safe tiles, cash out."));
        inventory.setItem(JACKPOT, icon(Material.NETHER_STAR, "<light_purple><bold>Jackpot", "<gray>Enter the progressive pool."));
        inventory.setItem(CRATES, icon(Material.CHEST, "<yellow><bold>Crates", "<gray>Open a gambling crate."));
        inventory.setItem(ROOM, icon(Material.OAK_DOOR, "<green><bold>My Gambling Room", "<gray>Create or return to your room."));
    }

    @Override
    public void handleClick(int slot) {
        switch (slot) {
            case SLOTS -> new AmountPickerGui(plugin, player, MenuGame.SLOTS).open();
            case MINES -> new MinesSetupGui(plugin, player).open();
            case JACKPOT -> new AmountPickerGui(plugin, player, MenuGame.JACKPOT).open();
            case CRATES -> new CratePickerGui(plugin, player).open();
            case ROOM -> {
                player.closeInventory();
                plugin.roomManager().createOrTeleport(player);
            }
            default -> {
            }
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

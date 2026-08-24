package net.havoccasino.gui;

import net.havoccasino.HavocCasino;
import net.havoccasino.game.SlotResult;
import net.havoccasino.game.SlotSymbol;
import net.havoccasino.util.Msg;
import net.havoccasino.util.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Renders a 3-reel slot spin as an animated chest GUI, then settles the bet.
 * The bet has already been withdrawn before this GUI is opened; winnings are
 * deposited when the reels lock.
 */
public final class SlotGui {

    private static final int[] REEL_SLOTS = {11, 13, 15};
    private static final int WINDOW_SIZE = 27;

    private final HavocCasino plugin;
    private final Player player;
    private final SlotResult result;
    private final double bet;
    private final Inventory inventory;

    public SlotGui(HavocCasino plugin, Player player, SlotResult result, double bet) {
        this.plugin = plugin;
        this.player = player;
        this.result = result;
        this.bet = bet;

        SlotHolder holder = new SlotHolder();
        this.inventory = Bukkit.createInventory(holder, WINDOW_SIZE, Msg.parse("<dark_gray>✦ <gold>Royal Slots</gold> ✦"));
        holder.setInventory(inventory);
        decorate();
    }

    private void decorate() {
        ItemStack pane = filler();
        for (int i = 0; i < WINDOW_SIZE; i++) {
            inventory.setItem(i, pane);
        }
        for (int slot : REEL_SLOTS) {
            inventory.setItem(slot, symbolItem(SlotSymbol.CHERRY));
        }
    }

    public void open() {
        player.openInventory(inventory);
        animate();
    }

    private void animate() {
        new BukkitRunnable() {
            int ticks = 0;
            final Random rng = ThreadLocalRandom.current();
            final int lastLock = 12 + (REEL_SLOTS.length - 1) * 8;

            @Override
            public void run() {
                ticks++;
                for (int i = 0; i < REEL_SLOTS.length; i++) {
                    int lockAt = 12 + i * 8;
                    if (ticks >= lockAt) {
                        inventory.setItem(REEL_SLOTS[i], symbolItem(result.reels()[i]));
                    } else {
                        inventory.setItem(REEL_SLOTS[i], symbolItem(SlotSymbol.random(rng)));
                    }
                }
                if (player.isOnline()) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.35f, 1.6f);
                }
                if (ticks >= lastLock + 2) {
                    cancel();
                    settle();
                }
            }
        }.runTaskTimer(plugin, 5L, 2L);
    }

    private void settle() {
        if (result.win()) {
            plugin.currencyService().deposit(player, result.payout());
            if (player.isOnline()) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
            }
            plugin.messages().send(player, "slots.win",
                    "amount", plugin.currencyService().format(result.payout()),
                    "multiplier", Numbers.trim(result.multiplier()));
        } else {
            if (player.isOnline()) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
            }
            plugin.messages().send(player, "slots.lose",
                    "amount", plugin.currencyService().format(bet));
        }
    }

    private ItemStack filler() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Msg.item("<gray>"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack symbolItem(SlotSymbol symbol) {
        ItemStack item = new ItemStack(symbol.material());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Msg.item(symbol.display()));
            item.setItemMeta(meta);
        }
        return item;
    }
}

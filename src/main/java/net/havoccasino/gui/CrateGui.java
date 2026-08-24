package net.havoccasino.gui;

import net.havoccasino.HavocCasino;
import net.havoccasino.game.Crate;
import net.havoccasino.game.CrateReward;
import net.havoccasino.util.Msg;
import net.havoccasino.util.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A CS:GO-style crate opening. A horizontal reel scrolls through the middle row
 * and decelerates until the predetermined winner lands under the centre pointer.
 *
 * The crate cost is withdrawn before this GUI opens; only the payout is
 * deposited here, when the reel settles.
 */
public final class CrateGui {

    private static final int SIZE = 27;
    private static final int[] WINDOW = {9, 10, 11, 12, 13, 14, 15, 16, 17};
    private static final int POINTER_TOP = 4;
    private static final int POINTER_BOTTOM = 22;

    private static final int REEL_LENGTH = 45;
    private static final int WINNER_INDEX = REEL_LENGTH - 5;   // where the winner sits in the reel
    private static final int TARGET_POS = WINNER_INDEX - 4;    // scroll offset that centres the winner

    private final HavocCasino plugin;
    private final Player player;
    private final Crate crate;
    private final CrateReward winner;
    private final CrateReward[] reel = new CrateReward[REEL_LENGTH];
    private final Inventory inventory;
    private boolean finished = false;

    public CrateGui(HavocCasino plugin, Player player, Crate crate, CrateReward winner) {
        this.plugin = plugin;
        this.player = player;
        this.crate = crate;
        this.winner = winner;

        CrateHolder holder = new CrateHolder();
        this.inventory = Bukkit.createInventory(holder, SIZE, Msg.parse("<dark_gray>✦ <gold>" + safe(crate.display()) + "</gold> ✦"));
        holder.setInventory(inventory);

        buildReel();
        decorate();
    }

    public void open() {
        player.openInventory(inventory);
        step(0);
    }

    private void buildReel() {
        for (int i = 0; i < REEL_LENGTH; i++) {
            reel[i] = crate.roll(ThreadLocalRandom.current());
        }
        reel[WINNER_INDEX] = winner;
    }

    private void decorate() {
        ItemStack filler = icon(Material.BLACK_STAINED_GLASS_PANE, "<gray>");
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, filler);
        }
        inventory.setItem(POINTER_TOP, icon(Material.HOPPER, "<yellow>▼"));
        inventory.setItem(POINTER_BOTTOM, icon(Material.HOPPER, "<yellow>▲"));
        drawWindow(0);
    }

    private void drawWindow(int pos) {
        for (int i = 0; i < WINDOW.length; i++) {
            CrateReward reward = reel[pos + i];
            inventory.setItem(WINDOW[i], rewardItem(reward));
        }
    }

    private void step(int pos) {
        drawWindow(pos);
        if (player.isOnline()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.4f, 1.2f);
        }
        if (pos >= TARGET_POS) {
            settle();
            return;
        }
        long delay = Math.min(6L, 1L + (long) (pos * 0.18));
        Bukkit.getScheduler().runTaskLater(plugin, () -> step(pos + 1), delay);
    }

    private void settle() {
        if (finished) {
            return;
        }
        finished = true;

        if (!winner.isBust()) {
            double payout = crate.cost() * winner.multiplier();
            plugin.currencyService().deposit(player, payout);
            if (player.isOnline()) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
            }
            plugin.messages().send(player, "crates.win",
                    "crate", crate.display(),
                    "reward", winner.name(),
                    "amount", plugin.currencyService().format(payout),
                    "multiplier", Numbers.trim(winner.multiplier()));
        } else {
            if (player.isOnline()) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
            }
            plugin.messages().send(player, "crates.bust",
                    "crate", crate.display(),
                    "reward", winner.name(),
                    "amount", plugin.currencyService().format(crate.cost()));
        }
    }

    private ItemStack rewardItem(CrateReward reward) {
        return icon(reward.material(), reward.name());
    }

    private ItemStack icon(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Msg.item(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private String safe(String miniMessage) {
        return miniMessage == null ? "Crate" : miniMessage;
    }
}

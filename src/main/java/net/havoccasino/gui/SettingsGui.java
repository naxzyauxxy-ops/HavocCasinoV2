package net.havoccasino.gui;

import net.havoccasino.HavocCasino;
import net.havoccasino.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * A tiny settings screen with one toggle: a green button when messages are ON
 * and a red button when they are OFF. Clicking flips the player's preference.
 */
public final class SettingsGui {

    private static final int SIZE = 27;
    private static final int TOGGLE_SLOT = 13;

    private final HavocCasino plugin;
    private final Player player;
    private final Inventory inventory;

    public SettingsGui(HavocCasino plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        SettingsHolder holder = new SettingsHolder();
        this.inventory = Bukkit.createInventory(holder, SIZE,
                plugin.messages().line(player, "settings.gui-title"));
        holder.setInventory(inventory);
        holder.setGui(this);

        render();
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void handleClick(int rawSlot) {
        if (rawSlot != TOGGLE_SLOT) {
            return;
        }
        boolean nowEnabled = plugin.playerSettings().toggleMessages(player.getUniqueId());
        plugin.playerSettings().save();
        render();
        if (player.isOnline()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, nowEnabled ? 1.4f : 0.8f);
        }
        // Force so the confirmation shows even when the player just turned messages OFF.
        plugin.messages().force(player, nowEnabled ? "settings.enabled" : "settings.disabled");
    }

    private void render() {
        ItemStack filler = icon(Material.BLACK_STAINED_GLASS_PANE, Msg.item("<gray>"), null);
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, filler);
        }
        inventory.setItem(TOGGLE_SLOT, toggleItem());
    }

    private ItemStack toggleItem() {
        boolean enabled = plugin.playerSettings().messagesEnabled(player.getUniqueId());
        Material material = enabled ? Material.LIME_DYE : Material.RED_DYE;
        var name = enabled
                ? plugin.messages().item(player, "settings.toggle-on-name")
                : plugin.messages().item(player, "settings.toggle-off-name");
        var lore = enabled
                ? plugin.messages().item(player, "settings.toggle-on-lore")
                : plugin.messages().item(player, "settings.toggle-off-lore");
        return icon(material, name, List.of(lore));
    }

    private ItemStack icon(Material material, net.kyori.adventure.text.Component name,
                           List<net.kyori.adventure.text.Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            if (lore != null) {
                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}

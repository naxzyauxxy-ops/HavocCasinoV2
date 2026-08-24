package net.havoccasino.region;

import net.havoccasino.HavocCasino;
import net.havoccasino.util.Msg;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Builds the selection wand and recognises it via a persistent data tag,
 * so an ordinary golden axe is never treated as a wand.
 */
public final class WandItem {

    private final NamespacedKey key;

    public WandItem(HavocCasino plugin) {
        this.key = new NamespacedKey(plugin, "casino_wand");
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Msg.item("<gold><bold>Casino Wand</bold>"));
            meta.lore(List.of(
                    Msg.item("<gray>Left-click a block: <white>corner 1"),
                    Msg.item("<gray>Right-click a block: <white>corner 2"),
                    Msg.item("<dark_gray>/hc schem save <name>")));
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isWand(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}

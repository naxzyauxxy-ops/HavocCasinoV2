package net.havoccasino.region;

import net.havoccasino.HavocCasino;
import net.havoccasino.util.Msg;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Sets selection corners when a player left/right-clicks with the wand.
 */
public final class WandListener implements Listener {

    private final HavocCasino plugin;

    public WandListener(HavocCasino plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (!plugin.wandItem().isWand(event.getItem())) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        Player player = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            plugin.selections().get(player).setPos1(block.getLocation());
            feedback(player, "Corner 1", block);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            plugin.selections().get(player).setPos2(block.getLocation());
            feedback(player, "Corner 2", block);
        }
    }

    private void feedback(Player player, String label, Block block) {
        Selection selection = plugin.selections().get(player);
        String extra = selection.isComplete()
                ? "<gray> · volume <white>" + selection.volume()
                : "<gray> · <yellow>need the other corner";
        Msg.force(player, "<green>" + label + " set: <white>"
                + block.getX() + ", " + block.getY() + ", " + block.getZ() + extra);
    }
}

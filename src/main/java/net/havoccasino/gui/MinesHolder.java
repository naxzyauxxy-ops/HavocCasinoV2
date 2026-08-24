package net.havoccasino.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Marker holder for a mines board. Carries the active game so the listener
 * can route clicks and closes without a separate registry.
 */
public final class MinesHolder implements InventoryHolder {

    private Inventory inventory;
    private MinesGui gui;

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    void setGui(MinesGui gui) {
        this.gui = gui;
    }

    public MinesGui getGui() {
        return gui;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

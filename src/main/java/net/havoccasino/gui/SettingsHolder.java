package net.havoccasino.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Marker holder for the message-settings GUI, carrying its controller.
 */
public final class SettingsHolder implements InventoryHolder {

    private Inventory inventory;
    private SettingsGui gui;

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    void setGui(SettingsGui gui) {
        this.gui = gui;
    }

    public SettingsGui getGui() {
        return gui;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

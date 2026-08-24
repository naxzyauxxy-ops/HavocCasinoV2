package net.havoccasino.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/** Generic holder for click-driven menus; routes to its Clickable. */
public final class MenuHolder implements InventoryHolder {

    private Inventory inventory;
    private Clickable clickable;

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    void setClickable(Clickable clickable) {
        this.clickable = clickable;
    }

    public Clickable getClickable() {
        return clickable;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

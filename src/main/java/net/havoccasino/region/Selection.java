package net.havoccasino.region;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * A two-corner cuboid selection made with the wand.
 */
public final class Selection {

    private Location pos1;
    private Location pos2;

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public Location pos1() {
        return pos1;
    }

    public Location pos2() {
        return pos2;
    }

    public boolean isComplete() {
        return pos1 != null && pos2 != null
                && pos1.getWorld() != null
                && pos1.getWorld().equals(pos2.getWorld());
    }

    public World world() {
        return pos1.getWorld();
    }

    public int minX() {
        return Math.min(pos1.getBlockX(), pos2.getBlockX());
    }

    public int minY() {
        return Math.min(pos1.getBlockY(), pos2.getBlockY());
    }

    public int minZ() {
        return Math.min(pos1.getBlockZ(), pos2.getBlockZ());
    }

    public int maxX() {
        return Math.max(pos1.getBlockX(), pos2.getBlockX());
    }

    public int maxY() {
        return Math.max(pos1.getBlockY(), pos2.getBlockY());
    }

    public int maxZ() {
        return Math.max(pos1.getBlockZ(), pos2.getBlockZ());
    }

    public long volume() {
        return (long) (maxX() - minX() + 1) * (maxY() - minY() + 1) * (maxZ() - minZ() + 1);
    }
}

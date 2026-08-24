package net.havoccasino.schematic;

import java.util.Map;

/**
 * A captured structure: dimensions plus a map of "x,y,z" -> block data string
 * for every non-air block, relative to the schematic's minimum corner.
 */
public final class BlockSchematic {

    private final int width;
    private final int height;
    private final int length;
    private final Map<String, String> blocks;

    public BlockSchematic(int width, int height, int length, Map<String, String> blocks) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.blocks = blocks;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int length() {
        return length;
    }

    public Map<String, String> blocks() {
        return blocks;
    }
}

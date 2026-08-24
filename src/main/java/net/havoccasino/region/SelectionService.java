package net.havoccasino.region;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks each player's in-progress wand selection (memory only).
 */
public final class SelectionService {

    private final ConcurrentHashMap<UUID, Selection> selections = new ConcurrentHashMap<>();

    public Selection get(Player player) {
        return selections.computeIfAbsent(player.getUniqueId(), k -> new Selection());
    }

    public void clear(Player player) {
        selections.remove(player.getUniqueId());
    }
}

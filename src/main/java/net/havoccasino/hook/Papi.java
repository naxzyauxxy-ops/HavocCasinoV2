package net.havoccasino.hook;

/**
 * Neutral flag class with no PlaceholderAPI imports. Reading this never forces
 * the PlaceholderAPI classes to load, so the plugin is safe without PAPI.
 * {@link HavocExpansion} (which does import PAPI) is only touched when this is true.
 */
public final class Papi {

    public static volatile boolean AVAILABLE = false;

    private Papi() {
    }
}

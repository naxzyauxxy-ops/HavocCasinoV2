package net.havoccasino.util;

public final class Numbers {

    private Numbers() {
    }

    /**
     * Parses a strictly positive amount, supporting shorthand suffixes:
     * k = thousand, m = million, b = billion, t = trillion (case-insensitive).
     * Examples: "5m" -> 5000000, "2.5k" -> 2500, "1,000" -> 1000.
     * Returns null if invalid.
     */
    public static Double parsePositive(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim().toLowerCase().replace(",", "").replace("_", "");
        if (s.isEmpty()) {
            return null;
        }

        double multiplier = 1.0;
        char last = s.charAt(s.length() - 1);
        switch (last) {
            case 'k':
                multiplier = 1_000d;
                s = s.substring(0, s.length() - 1);
                break;
            case 'm':
                multiplier = 1_000_000d;
                s = s.substring(0, s.length() - 1);
                break;
            case 'b':
                multiplier = 1_000_000_000d;
                s = s.substring(0, s.length() - 1);
                break;
            case 't':
                multiplier = 1_000_000_000_000d;
                s = s.substring(0, s.length() - 1);
                break;
            default:
                break;
        }
        if (s.isEmpty()) {
            return null;
        }

        try {
            double value = Double.parseDouble(s) * multiplier;
            if (value <= 0 || Double.isNaN(value) || Double.isInfinite(value)) {
                return null;
            }
            return Math.round(value * 100.0) / 100.0;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Formats a multiplier compactly (e.g. 12, 1.5). */
    public static String trim(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return stripZeros(Math.round(value * 100.0) / 100.0);
    }

    /**
     * Compact number with a k/m/b/t suffix, e.g. 5000000 -> "5m", 2500 -> "2.5k".
     * Values under 1000 are shown as-is (trimmed).
     */
    public static String compact(double value) {
        String[] suffixes = {"", "k", "m", "b", "t"};
        double v = value;
        int idx = 0;
        while (Math.abs(v) >= 1000 && idx < suffixes.length - 1) {
            v /= 1000.0;
            idx++;
        }
        return stripZeros(Math.round(v * 100.0) / 100.0) + suffixes[idx];
    }

    private static String stripZeros(double d) {
        if (d == Math.floor(d) && !Double.isInfinite(d)) {
            return String.valueOf((long) d);
        }
        String str = String.valueOf(d);
        if (str.contains(".")) {
            str = str.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return str;
    }
}

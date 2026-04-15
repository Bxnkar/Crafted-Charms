package com.craftedcharms.util;

/**
 * ANSI escape code constants for coloured console output.
 * Works on macOS/Linux terminals. Windows requires ANSI support enabled.
 */
public final class ConsoleColors {

    private ConsoleColors() {}   // Utility class — no instantiation

    public static final String RESET         = "\033[0m";
    public static final String BOLD          = "\033[1m";

    public static final String RED           = "\033[0;31m";
    public static final String GREEN         = "\033[0;32m";
    public static final String YELLOW        = "\033[0;33m";
    public static final String BLUE          = "\033[0;34m";
    public static final String PURPLE        = "\033[0;35m";
    public static final String CYAN          = "\033[0;36m";

    public static final String BRIGHT_RED    = "\033[1;31m";
    public static final String BRIGHT_GREEN  = "\033[1;32m";
    public static final String BRIGHT_YELLOW = "\033[1;33m";
    public static final String BRIGHT_BLUE   = "\033[1;34m";
    public static final String BRIGHT_PURPLE = "\033[1;35m";
    public static final String BRIGHT_CYAN   = "\033[1;36m";
    public static final String BRIGHT_WHITE  = "\033[1;37m";

    // ── Convenience print helpers ──────────────────────────────────────

    public static void success(String msg)  { System.out.println(BRIGHT_GREEN  + "  ✓ " + msg + RESET); }
    public static void error(String msg)    { System.out.println(BRIGHT_RED    + "  ✗ " + msg + RESET); }
    public static void warning(String msg)  { System.out.println(BRIGHT_YELLOW + "  ⚠ " + msg + RESET); }
    public static void info(String msg)     { System.out.println(BRIGHT_CYAN   + "  ℹ " + msg + RESET); }

    /** Prints a centred box-drawing header. */
    public static void header(String title) {
        int width = 55;
        String bar = "═".repeat(width);
        int pad = (width - title.length()) / 2;
        String centred = " ".repeat(Math.max(0, pad)) + title;
        System.out.println();
        System.out.println(BRIGHT_PURPLE + "  ╔" + bar + "╗" + RESET);
        System.out.printf(BRIGHT_PURPLE   + "  ║%-" + width + "s║%n" + RESET, centred);
        System.out.println(BRIGHT_PURPLE + "  ╚" + bar + "╝" + RESET);
    }

    /** Prints a thinner section divider. */
    public static void divider() {
        System.out.println(PURPLE + "  " + "─".repeat(57) + RESET);
    }
}

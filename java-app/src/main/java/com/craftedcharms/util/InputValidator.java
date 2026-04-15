package com.craftedcharms.util;

import java.math.BigDecimal;
import java.util.Scanner;

/**
 * Utility class providing validated console input helpers.
 * All methods block until valid input is received.
 * Demonstrates: Static utility methods, input validation, exception handling.
 */
public class InputValidator {

    /** Reads an integer within [min, max] inclusive. */
    public static int readInt(Scanner sc, String prompt, int min, int max) {
        System.out.print(prompt);
        while (true) {
            try {
                int val = Integer.parseInt(sc.nextLine().trim());
                if (val < min || val > max) {
                    System.out.printf("  Please enter a number between %d and %d: ", min, max);
                } else {
                    return val;
                }
            } catch (NumberFormatException e) {
                System.out.print("  Invalid input — enter a whole number: ");
            }
        }
    }

    /** Reads a positive BigDecimal price value. */
    public static BigDecimal readPrice(Scanner sc, String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                BigDecimal val = new BigDecimal(sc.nextLine().trim());
                if (val.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.print("  Price must be greater than 0: ");
                } else {
                    return val;
                }
            } catch (NumberFormatException e) {
                System.out.print("  Invalid price — enter a number (e.g. 1999.00): ");
            }
        }
    }

    /** Reads a non-blank string. */
    public static String readNonEmpty(Scanner sc, String prompt) {
        System.out.print(prompt);
        while (true) {
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.print("  This field is required. Try again: ");
        }
    }

    /** Reads a valid email address (basic regex check). */
    public static String readEmail(Scanner sc, String prompt) {
        System.out.print(prompt);
        while (true) {
            String input = sc.nextLine().trim();
            if (input.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$")) return input;
            System.out.print("  Invalid email. Enter a valid address: ");
        }
    }

    /** Reads a phone number (10–15 digits, strips spaces and dashes). */
    public static String readPhone(Scanner sc, String prompt) {
        System.out.print(prompt);
        while (true) {
            String digits = sc.nextLine().trim().replaceAll("[\\s\\-]", "");
            if (digits.matches("\\d{10,15}")) return digits;
            System.out.print("  Invalid phone. Enter 10–15 digits: ");
        }
    }

    /** Reads an optional string — returns blank string if user presses Enter. */
    public static String readOptional(Scanner sc, String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    /** Reads a yes/no confirmation. Returns true for 'y'/'yes'. */
    public static boolean readYesNo(Scanner sc, String prompt) {
        System.out.print(prompt + " (y/n): ");
        while (true) {
            String in = sc.nextLine().trim().toLowerCase();
            if (in.equals("y") || in.equals("yes")) return true;
            if (in.equals("n") || in.equals("no"))  return false;
            System.out.print("  Enter 'y' or 'n': ");
        }
    }
}

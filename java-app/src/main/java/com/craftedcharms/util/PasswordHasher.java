package com.craftedcharms.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password hashing utility.
 *
 * Uses PBKDF2WithHmacSHA256 for new passwords and supports legacy SHA-256
 * hashes during login to keep existing seeded/demo users functional.
 */
public final class PasswordHasher {

    private static final String ALGO_PBKDF2 = "PBKDF2WithHmacSHA256";
    private static final String PREFIX_PBKDF2 = "pbkdf2";
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_LENGTH_BITS = 256;

    private PasswordHasher() {
        // Utility class.
    }

    /**
     * Hashes a plaintext password for storage.
     * Format: pbkdf2$iterations$base64(salt)$base64(hash)
     */
    public static String hashForStorage(String plainPassword) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = derivePbkdf2(plainPassword, salt, PBKDF2_ITERATIONS);
        return PREFIX_PBKDF2 + "$" + PBKDF2_ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifies a plaintext password against stored hash (PBKDF2 or legacy SHA-256).
     */
    public static boolean verify(String plainPassword, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) return false;

        if (storedHash.startsWith(PREFIX_PBKDF2 + "$")) {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 4) return false;
            int iterations;
            try {
                iterations = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derivePbkdf2(plainPassword, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        }

        // Backward compatibility: existing data uses SHA-256 hex.
        return sha256Hex(plainPassword).equals(storedHash);
    }

    /** Returns true when hash value is in legacy SHA-256 format. */
    public static boolean isLegacySha256(String storedHash) {
        return storedHash != null && storedHash.matches("^[a-f0-9]{64}$");
    }

    private static byte[] derivePbkdf2(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS);
            return SecretKeyFactory.getInstance(ALGO_PBKDF2).generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash password.", e);
        }
    }

    private static String sha256Hex(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainPassword.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable.", e);
        }
    }
}
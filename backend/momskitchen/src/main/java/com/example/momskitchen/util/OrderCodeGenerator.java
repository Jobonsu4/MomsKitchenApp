package com.example.momskitchen.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OrderCodeGenerator generates unique, human-readable order codes.
 * Format example: MK-20250912-0001
 */
public class OrderCodeGenerator {

    // Prefix for all orders (customize if needed)
    private static final String PREFIX = "MK";

    // Thread-safe counter for generating daily sequence numbers
    private static final AtomicInteger counter = new AtomicInteger(0);

    // Date format for embedding into the order code (yyyyMMdd)
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Generates a unique order code.
     * @return a string like "MK-20250912-0001"
     */
    public static String generateOrderCode() {
        String datePart = LocalDateTime.now().format(DATE_FORMAT); // Current date
        int sequence = counter.incrementAndGet();                  // Daily sequence number
        return String.format("%s-%s-%04d", PREFIX, datePart, sequence);
    }

    /**
     * Resets the counter (optional — e.g., if you want to reset daily).
     */
    public static void resetCounter() {
        counter.set(0);
    }

    /**
     * Compact code generator (<= 12 chars) suitable for DB column length.
     * Format: MK + 6 random base32 chars (no ambiguous chars).
     */
    public static String generate() {
        final char[] alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
        StringBuilder sb = new StringBuilder(8);
        sb.append(PREFIX); // e.g., "MK"
        for (int i = 0; i < 6; i++) {
            int idx = ThreadLocalRandom.current().nextInt(alphabet.length);
            sb.append(alphabet[idx]);
        }
        return sb.toString();
    }
}

package com.example;

import java.util.Arrays;

/**
 * Utility class for reading configuration and environment variables.
 */
public class ConfigUtils {

    /**
     * Determines if the application should run in development mode.
     * Checks system property "devMode", environment variable "DEV_MODE",
     * and command-line argument "--dev".
     *
     * @param args command-line arguments
     * @return true if dev mode is enabled, false otherwise
     */
    public static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode")) return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE"))) return true;
        return Arrays.asList(args).contains("--dev");
    }

    /**
     * Resolves a configuration value from system properties or environment variables.
     * Returns null if neither is set.
     *
     * @param propertyKey system property key
     * @param envKey environment variable key
     * @return trimmed configuration value or null if not set
     */
    public static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) v = System.getenv(envKey);
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}

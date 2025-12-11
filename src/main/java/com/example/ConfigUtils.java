package com.example;

import java.util.Arrays;

public class ConfigUtils {
    public static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode")) return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE"))) return true;
        return Arrays.asList(args).contains("--dev");
    }

    public static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) v = System.getenv(envKey);
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}

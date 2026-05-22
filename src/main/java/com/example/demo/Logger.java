package com.example.demo;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class Logger {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static boolean debugMode = true;
    private static LogLevel logLevel = LogLevel.WARNING;

    public enum LogLevel {
        WARNING,
        ERROR
    }

    public static void logInfo(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[" + timestamp + "] INFO: " + message);
    }

    public static void logWarning(String message) {
        if (logLevel == LogLevel.WARNING) {
            String timestamp = LocalDateTime.now().format(formatter);
            System.out.println("[" + timestamp + "] WARNING: " + message);
        }
    }

    public static void logError(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.err.println("[" + timestamp + "] ERROR: " + message);
    }

    public static void logDebug(String message) {
        if (debugMode) {
            String timestamp = LocalDateTime.now().format(formatter);
            System.out.println("[" + timestamp + "] DEBUG: " + message);
        }
    }

    public static void setDebugMode(boolean mode) {
        debugMode = mode;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void setLogLevel(String level) {
        logLevel = LogLevel.valueOf(level.toUpperCase());
    }

    public static String getLogLevel() {
        return logLevel.name().toLowerCase();
    }

    public static void logInfo(String format, Object... args) {
        logInfo(String.format(format, args));
    }

    public static void logWarning(String format, Object... args) {
        logWarning(String.format(format, args));
    }

    public static void logError(String format, Object... args) {
        logError(String.format(format, args));
    }

    public static void logDebug(String format, Object... args) {
        if (debugMode) {
            logDebug(String.format(format, args));
        }
    }
}

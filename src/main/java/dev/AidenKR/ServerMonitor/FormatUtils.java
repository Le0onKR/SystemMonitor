package dev.AidenKR.ServerMonitor;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class FormatUtils {

    // NOOP
    private FormatUtils() {}

    public static String formatNumber(double value, int decimals) {
        String pattern = "0";

        if (decimals > 0) {
            pattern += ".";
            pattern += "0".repeat(decimals);
        }
        return new DecimalFormat(pattern).format(value);
    }

    public static String formatPercentage(double percentage) {
        return formatPercentage(percentage, 2);
    }

    public static String formatPercentage(double percentage, int decimals) {
        if (Double.isNaN(percentage) || Double.isInfinite(percentage)) {
            throw new IllegalArgumentException("Percentage must be a finite number");
        }

        if (decimals < 0) {
            throw new IllegalArgumentException("Decimals cannot be negative");
        }
        return formatNumber(percentage, decimals) + "%";
    }

    public static String formatDuration(long milliseconds) {
        if (milliseconds < 0) {
            throw new IllegalArgumentException("Milliseconds cannot be negative");
        }
        Duration duration = Duration.ofMillis(milliseconds);
        List<String> parts = new ArrayList<>();

        long days = duration.toDaysPart();
        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();
        int seconds = duration.toSecondsPart();

        if (days > 0) parts.add(days + "d");
        if (hours > 0) parts.add(hours + "h");
        if (minutes > 0) parts.add(minutes + "m");
        if (seconds > 0 || parts.isEmpty()) parts.add(seconds + "s");

        return String.join(" ", parts);
    }

    public static String formatBytes(long bytes) {
        return formatBytes(bytes, 2);
    }

    public static String formatBytes(long bytes, int decimals) {
        if (bytes < 0) {
            throw new IllegalArgumentException("Bytes cannot be negative");
        }

        if (decimals < 0) {
            throw new IllegalArgumentException("Decimals cannot be negative");
        }

        if (bytes == 0) {
            return "0 B";
        }
        String[] units = {"B", "KB", "MB", "GB", "TB", "PB"};
        double value = bytes;
        int unit = 0;

        while (value >= 1024 && unit < units.length - 1) {
            value /= 1024;
            unit++;
        }
        return formatNumber(value, decimals) + " " + units[unit];
    }
}

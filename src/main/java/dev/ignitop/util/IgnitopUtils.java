package dev.ignitop.util;

/**
 *
 */
public final class IgnitopUtils {
    /**
     * @param uptimeMillis Uptime millis.
     */
    public static String formattedUptime(long uptimeMillis) {
        long totalSeconds = uptimeMillis / 1000;

        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds / 3600)  % 24;
        long minutes = (totalSeconds / 60) % 60;
        long seconds = totalSeconds % 60;

        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }
}

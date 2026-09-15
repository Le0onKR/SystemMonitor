package dev.AidenKR.ServerMonitor.data;

public record SystemStatus(
        String osName,
        String osVersion,
        String architecture,
        String hostname,
        String javaVersion,
        String jvmName,
        String jvmVersion,
        long uptime,
        long threadCount
) {}

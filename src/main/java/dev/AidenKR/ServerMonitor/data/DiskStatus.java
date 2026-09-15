package dev.AidenKR.ServerMonitor.data;

import java.nio.file.Path;

public record DiskStatus(Path path, long total, long used, long free, double usage) {}

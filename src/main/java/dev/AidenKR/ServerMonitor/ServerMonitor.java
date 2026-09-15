package dev.AidenKR.ServerMonitor;


import com.sun.management.OperatingSystemMXBean;
import dev.AidenKR.ServerMonitor.data.CPUStatus;
import dev.AidenKR.ServerMonitor.data.DiskStatus;
import dev.AidenKR.ServerMonitor.data.MemoryStatus;
import dev.AidenKR.ServerMonitor.data.SystemStatus;

import java.io.IOException;
import java.lang.management.*;
import java.net.InetAddress;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ServerMonitor {

    // NOOP
    private ServerMonitor() {}

    private static final OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private static final ThreadMXBean thread = ManagementFactory.getThreadMXBean();

    public static CPUStatus getCPU() {
        return new CPUStatus(getCPUUsage(), getProcessCount(), getSystemLoad());
    }

    public static double getCPUUsage() {
        double usage = os.getCpuLoad();

        if (usage < 0) {
            return Double.NaN;
        }
        return usage * 100.0;
    }

    public static double getProcessCount() {
        double load = os.getAvailableProcessors();
        return load < 0 ? Double.NaN : load;
    }

    public static double getSystemLoad() {
        return os.getSystemLoadAverage();
    }

    public static MemoryStatus getMemory() {
        long total = getMemoryTotal();
        long free = getMemoryFree();
        long used = total - free;
        double usage = total <= 0 ? 0.0 : ((double) used / total) * 100.0;

        return new MemoryStatus(total, used, free, usage);
    }

    public static long getMemoryTotal() {
        return os.getTotalMemorySize();
    }

    public static long getMemoryUsed() {
        return getMemoryTotal() - getMemoryFree();
    }

    public static long getMemoryFree() {
        return os.getFreeMemorySize();
    }

    public static double getMemoryUsage() {
        return getMemory().usage();
    }

    public static DiskStatus getDisk(Path path) throws IOException {
        long total = getDiskTotal(path);
        long free = getDiskFree(path);
        long used = Math.max(total - free, 0);
        double usage = total <= 0 ? 0.0 : ((double) used / total) * 100.0;

        return new DiskStatus(path.toAbsolutePath().normalize(), total, used, free, usage);
    }

    public static long getDiskTotal(Path path) throws IOException {
        return Files.getFileStore(path).getTotalSpace();
    }

    public static long getDiskUsed(Path path) throws IOException {
        return getDiskTotal(path) - getDiskFree(path);
    }

    public static long getDiskFree(Path path) throws IOException {
        return Files.getFileStore(path).getUsableSpace();
    }

    public static double getDiskUsage(Path path) throws IOException {
        return getDisk(path).usage();
    }

    public static List<FileStore> getDisks() throws IOException {
        List<FileStore> stores = new ArrayList<>();

        for (FileStore store : FileSystems.getDefault().getFileStores()) {
            stores.add(store);
        }
        return stores;
    }

    public static long getHeapUsed() {
        MemoryUsage usage = memory.getHeapMemoryUsage();
        return usage.getUsed();
    }

    public static long getHeapMax() {
        MemoryUsage usage = memory.getHeapMemoryUsage();
        return usage.getMax();
    }

    public static SystemStatus getSystem() {
        return new SystemStatus(
                getOsName(),
                getOsVersion(),
                getArchitecture(),
                getHostname(),
                getJavaVersion(),
                getJvmName(),
                getJvmVersion(),
                getUptime(),
                getThreadCount()
        );
    }

    public static long getThreadCount() {
        return thread.getThreadCount();
    }

    public static long getUptime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }

    public static String getJvmName() {
        return System.getProperty("java.vm.name");
    }

    public static String getJvmVersion() {
        return System.getProperty("java.vm.version");
    }

    public static String getOsName() {
        return System.getProperty("os.name");
    }

    public static String getOsVersion() {
        return System.getProperty("os.version");
    }

    public static String getArchitecture() {
        return System.getProperty("os.arch");
    }

    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}

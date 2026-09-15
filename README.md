# ServerMonitor

A lightweight Java utility library for reading server, JVM, CPU, memory, and disk status.

`ServerMonitor` is responsible for collecting system data; `FormatUtils` is responsible for converting that data into human-readable strings.

[![Java](https://img.shields.io/badge/Java-17%2B-orange)]()

---

## Table of Contents

- [Features](#features)
- [Structure](#structure)
- [CPU](#cpu)
- [Memory](#memory)
- [Disk](#disk)
- [JVM](#jvm)
- [System](#system)
- [FormatUtils](#formatutils)
- [Full Example](#full-example)
- [Design](#design)
- [Requirements](#requirements)
- [Known Limitations](#known-limitations)
- [Scope](#scope)

---

## Features

- CPU status
- Memory status
- Disk status
- JVM status
- System information
- Byte / Percentage / Duration formatting
- Built on Java 17
- No external runtime dependencies (but does rely on a `com.sun.management` internal API — see [Known Limitations](#known-limitations))
- Static utility API

---

## Structure

```text
ServerMonitor/
└── src/
    └── main/
        └── java/
            └── dev/
                └── AidenKR/
                    └── ServerMonitor/
                        ├── ServerMonitor.java
                        ├── FormatUtils.java
                        └── data/
                            ├── CPUStatus.java
                            ├── MemoryStatus.java
                            ├── DiskStatus.java
                            └── SystemStatus.java
```

### ServerMonitor

The core class responsible for querying system information.

```java
ServerMonitor.getCPU();
ServerMonitor.getMemory();
ServerMonitor.getDisk(Path.of("."));
ServerMonitor.getSystem();
```

`ServerMonitor` holds no state — it queries the currently running JVM and OS on demand — so it is never instantiated.

```java
private ServerMonitor() {}
```

### FormatUtils

A utility class that converts raw data into display-ready strings.

```java
FormatUtils.formatBytes(bytes);
FormatUtils.formatPercentage(percentage);
FormatUtils.formatDuration(milliseconds);
```

Data collection and string formatting are kept separate so `ServerMonitor` doesn't depend on any particular output format.

---

## CPU

### Query

```java
CPUStatus cpu = ServerMonitor.getCPU();

System.out.println(cpu.usage());
System.out.println(cpu.processors());
System.out.println(cpu.load());
```

| Field        | Description               |
| ------------ | -------------------------- |
| `usage`      | CPU usage (%)               |
| `processors` | Number of available logical processors |
| `load`       | System Load Average         |

### Individual Values

```java
double usage = ServerMonitor.getCPUUsage();
int processors = ServerMonitor.getProcessCount();
double load = ServerMonitor.getSystemLoad();
```

> ⚠️ `usage` and `load` return **`Double.NaN`** when the value can't be measured (right after JVM startup, or for `load` on platforms the OS doesn't support it on, like Windows). This distinguishes "unavailable" from "actually zero" — check `Double.isNaN(...)` before using either value. Passing NaN straight into `FormatUtils.formatPercentage(...)` throws `IllegalArgumentException`.

---

## Memory

### Query

```java
MemoryStatus memory = ServerMonitor.getMemory();
```

| Field   | Description        |
| ------- | -------------------- |
| `total` | Total memory          |
| `used`  | Memory in use          |
| `free`  | Available memory      |
| `usage` | Memory usage (%)      |

### Individual Values

```java
ServerMonitor.getMemoryTotal();
ServerMonitor.getMemoryUsed();
ServerMonitor.getMemoryFree();
ServerMonitor.getMemoryUsage();
```

---

## Disk

Queries disk information for the file system a given path belongs to.

```java
try {
    DiskStatus disk = ServerMonitor.getDisk(Path.of("."));
} catch (IOException e) {
    // path unreachable (unmounted, permission denied, etc.)
}
```

| Field   | Description       |
| ------- | -------------------- |
| `path`  | Queried path          |
| `total` | Total capacity        |
| `used`  | Used capacity          |
| `free`  | Available capacity     |
| `usage` | Usage (%)              |

### Individual Values

```java
ServerMonitor.getDiskTotal(path);   // throws IOException
ServerMonitor.getDiskUsed(path);    // throws IOException
ServerMonitor.getDiskFree(path);    // throws IOException
ServerMonitor.getDiskUsage(path);   // throws IOException
```

You can also list every `FileStore` on the system:

```java
List<FileStore> disks = ServerMonitor.getDisks();
```

> Every disk `get*` method throws a checked `IOException` — callers must either `try/catch` it or declare `throws`.

---

## JVM

### Heap

```java
long used = ServerMonitor.getHeapUsed();
long max = ServerMonitor.getHeapMax();
```

### Threads

```java
long threads = ServerMonitor.getThreadCount();
```

### Uptime

```java
long uptime = ServerMonitor.getUptime();
```

`getUptime()` returns milliseconds. To convert it into a human-readable form:

```java
String uptime = FormatUtils.formatDuration(ServerMonitor.getUptime());
```

Example:

```text
3d 12h 42m 18s
```

### Java / JVM Information

```java
String javaVersion = ServerMonitor.getJavaVersion();
String jvmName = ServerMonitor.getJvmName();
String jvmVersion = ServerMonitor.getJvmVersion();
```

---

## System

Queries all system information at once.

```java
SystemStatus system = ServerMonitor.getSystem();
```

| Field          | Description                |
| -------------- | ---------------------------- |
| `osName`       | Operating System name         |
| `osVersion`    | Operating System version      |
| `architecture` | System architecture           |
| `hostname`     | Hostname                      |
| `javaVersion`  | Java version                   |
| `jvmName`      | JVM name                      |
| `jvmVersion`   | JVM version                   |
| `uptime`       | JVM uptime (milliseconds)     |
| `threadCount`  | Current thread count          |

---

## FormatUtils

### Format Number

```java
FormatUtils.formatNumber(123.4567, 2);
// 123.46
```

### Format Percentage

```java
FormatUtils.formatPercentage(52.356);
// 52.36%

FormatUtils.formatPercentage(52.356, 1);
// 52.4%
```

### Format Bytes

```java
FormatUtils.formatBytes(1024);
// 1.00 KB

FormatUtils.formatBytes(1024, 0);
// 1 KB
```

Supported units: `B`, `KB`, `MB`, `GB`, `TB`, `PB` (base-1024 conversion)

### Format Duration

```java
FormatUtils.formatDuration(3661000);
// 1h 1m 1s
```

Only non-zero units are shown.

```text
0          → 0s
1000       → 1s
61000      → 1m 1s
3661000    → 1h 1m 1s
90061000   → 1d 1h 1m 1s
```

---

## Full Example

```java
CPUStatus cpu = ServerMonitor.getCPU();
MemoryStatus memory = ServerMonitor.getMemory();
SystemStatus system = ServerMonitor.getSystem();

System.out.println("CPU: " + FormatUtils.formatPercentage(cpu.usage()));
System.out.println("Memory: " + FormatUtils.formatPercentage(memory.usage()));
System.out.println("Uptime: " + FormatUtils.formatDuration(system.uptime()));
System.out.println("Java: " + system.javaVersion());
System.out.println("OS: " + system.osName());
```

Expected output:

```text
CPU: 23.42%
Memory: 61.37%
Uptime: 2d 14h 31m 09s
Java: 17.0.12
OS: macOS
```

---

## Design

```text
ServerMonitor
     │
     ├── CPU
     ├── Memory
     ├── Disk
     ├── JVM
     └── System
          │
          ▼
       Raw Data
          │
          ▼
    FormatUtils
          │
          ▼
  Human-readable Output
```

`ServerMonitor` focuses on **data retrieval**; `FormatUtils` focuses on **data presentation**.

This means avoiding code like:

```java
ServerMonitor.getCPUUsageString();
ServerMonitor.getFormattedMemory();
ServerMonitor.getReadableUptime();
```

and instead keeping raw data and its presentation separate:

```java
ServerMonitor.getCPUUsage();
FormatUtils.formatPercentage(...);

ServerMonitor.getMemoryUsed();
FormatUtils.formatBytes(...);

ServerMonitor.getUptime();
FormatUtils.formatDuration(...);
```

---

## Requirements

- Java 17+ (uses `record`, `Duration`, and the Management API)

---

## Known Limitations

- **Depends on `com.sun.management.OperatingSystemMXBean`**: this is a HotSpot (OpenJDK/Oracle JDK) internal API, not a standard cross-implementation one. "No external runtime dependency" means no third-party library is required — it does not mean the API is guaranteed on every JVM implementation (e.g. some embedded/custom JVMs).
- **Unavailable readings are normalized to `NaN`**: `getCPUUsage()` and `getSystemLoad()` return `Double.NaN` whenever the OS can't provide a value (right after JVM startup, or `load` on platforms like Windows that don't support it). Earlier versions leaked `0.0` and `-1.0` respectively for these cases, which couldn't be told apart from "actually idle" or "actually zero load." Check `Double.isNaN(...)` before using either value.

## Scope

The current version focuses on **querying and presenting data**. It does not include:

- Automatic alerting
- Webhooks
- Server control / process termination / auto-restart
- External monitoring server integration
- Periodic polling
- Threshold management

These are expected to be handled by a separate monitoring/alerting layer built on top of this library.
# ServerMonitor

Java 애플리케이션에서 서버, JVM, CPU, Memory, Disk 등의 시스템 상태를 간단하게 조회할 수 있도록 제공하는 경량 유틸리티 라이브러리입니다.

`ServerMonitor`는 시스템 정보를 수집하는 역할을, `FormatUtils`는 수집된 값을 사람이 읽기 좋은 형태로 변환하는 역할을 담당합니다.

[![Java](https://img.shields.io/badge/Java-17%2B-orange)]()

---

## 목차

- [특징](#특징)
- [구조](#구조)
- [CPU](#cpu)
- [Memory](#memory)
- [Disk](#disk)
- [JVM](#jvm)
- [System](#system)
- [FormatUtils](#formatutils)
- [전체 예제](#전체-예제)
- [설계](#설계)
- [요구사항](#요구사항)
- [알려진 제약사항](#알려진-제약사항)
- [범위(Scope)](#범위scope)

---

## 특징

- CPU 상태 조회
- Memory 상태 조회
- Disk 상태 조회
- JVM 상태 조회
- System 정보 조회
- Byte / Percentage / Duration 포맷팅
- Java 17 기반
- 외부 런타임 의존성 없음 (단, `com.sun.management` 내부 API 사용 — [알려진 제약사항](#알려진-제약사항) 참고)
- Static utility API

---

## 구조

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

시스템 정보를 조회하는 핵심 클래스입니다.

```java
ServerMonitor.getCPU();
ServerMonitor.getMemory();
ServerMonitor.getDisk(Path.of("."));
ServerMonitor.getSystem();
```

`ServerMonitor`는 상태를 저장하지 않고 현재 실행 중인 JVM 및 운영체제의 정보를 조회하기 때문에 인스턴스를 생성하지 않습니다.

```java
private ServerMonitor() {}
```

### FormatUtils

조회한 데이터를 표시용 문자열로 변환하는 유틸리티 클래스입니다.

```java
FormatUtils.formatBytes(bytes);
FormatUtils.formatPercentage(percentage);
FormatUtils.formatDuration(milliseconds);
```

데이터 수집과 문자열 포맷팅을 분리하여 `ServerMonitor`가 특정 출력 형식에 의존하지 않도록 합니다.

---

## CPU

### 조회

```java
CPUStatus cpu = ServerMonitor.getCPU();

System.out.println(cpu.usage());
System.out.println(cpu.processors());
System.out.println(cpu.load());
```

| Field        | Description          |
| ------------ | --------------------- |
| `usage`      | CPU 사용률 (%)          |
| `processors` | 사용 가능한 논리 프로세서 수      |
| `load`       | System Load Average   |

### 개별 값

```java
double usage = ServerMonitor.getCPUUsage();
int processors = ServerMonitor.getProcessCount();
double load = ServerMonitor.getSystemLoad();
```

> ⚠️ `usage`와 `load`는 값을 측정할 수 없는 경우(JVM 시작 직후, 혹은 `load`의 경우 Windows처럼 OS가 아예 미지원하는 플랫폼) **`Double.NaN`을 반환**합니다. "측정 불가"와 "실제로 0"을 구분하기 위한 설계이므로, 사용 전에 반드시 `Double.isNaN(...)`으로 먼저 체크하세요. `FormatUtils.formatPercentage(...)`에 NaN을 그대로 넘기면 `IllegalArgumentException`이 발생합니다.

---

## Memory

### 조회

```java
MemoryStatus memory = ServerMonitor.getMemory();
```

| Field   | Description   |
| ------- | ------------- |
| `total` | 전체 메모리        |
| `used`  | 사용 중인 메모리     |
| `free`  | 사용 가능한 메모리    |
| `usage` | 메모리 사용률 (%)   |

### 개별 값

```java
ServerMonitor.getMemoryTotal();
ServerMonitor.getMemoryUsed();
ServerMonitor.getMemoryFree();
ServerMonitor.getMemoryUsage();
```

---

## Disk

특정 경로가 속한 파일 시스템의 Disk 정보를 조회합니다.

```java
try {
    DiskStatus disk = ServerMonitor.getDisk(Path.of("."));
} catch (IOException e) {
    // 대상 경로에 접근할 수 없는 경우 (마운트 해제, 권한 문제 등)
}
```

| Field   | Description   |
| ------- | ------------- |
| `path`  | 조회 대상 경로      |
| `total` | 전체 용량         |
| `used`  | 사용 용량         |
| `free`  | 사용 가능한 용량     |
| `usage` | 사용률 (%)       |

### 개별 값

```java
ServerMonitor.getDiskTotal(path);   // throws IOException
ServerMonitor.getDiskUsed(path);    // throws IOException
ServerMonitor.getDiskFree(path);    // throws IOException
ServerMonitor.getDiskUsage(path);   // throws IOException
```

모든 FileStore를 조회할 수도 있습니다.

```java
List<FileStore> disks = ServerMonitor.getDisks();
```

> 모든 `get*` Disk 메서드는 `IOException`을 던집니다(unchecked가 아님) — 호출부에서 반드시 `try/catch` 또는 `throws` 선언이 필요합니다.

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

`getUptime()`은 milliseconds를 반환합니다. 사람이 읽을 수 있는 형태로 변환하려면:

```java
String uptime = FormatUtils.formatDuration(ServerMonitor.getUptime());
```

예:

```text
3d 12h 42m 18s
```

### Java / JVM 정보

```java
String javaVersion = ServerMonitor.getJavaVersion();
String jvmName = ServerMonitor.getJvmName();
String jvmVersion = ServerMonitor.getJvmVersion();
```

---

## System

전체 시스템 정보를 한 번에 조회할 수 있습니다.

```java
SystemStatus system = ServerMonitor.getSystem();
```

| Field          | Description                |
| -------------- | --------------------------- |
| `osName`       | Operating System name       |
| `osVersion`    | Operating System version    |
| `architecture` | System architecture         |
| `hostname`     | Hostname                    |
| `javaVersion`  | Java version                 |
| `jvmName`      | JVM name                    |
| `jvmVersion`   | JVM version                 |
| `uptime`       | JVM uptime (milliseconds)   |
| `threadCount`  | Current thread count        |

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

지원 단위: `B`, `KB`, `MB`, `GB`, `TB`, `PB` (1024 기준 변환)

### Format Duration

```java
FormatUtils.formatDuration(3661000);
// 1h 1m 1s
```

필요한 단위만 표시합니다.

```text
0          → 0s
1000       → 1s
61000      → 1m 1s
3661000    → 1h 1m 1s
90061000   → 1d 1h 1m 1s
```

---

## 전체 예제

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

예상 출력:

```text
CPU: 23.42%
Memory: 61.37%
Uptime: 2d 14h 31m 09s
Java: 17.0.12
OS: macOS
```

---

## 설계

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

`ServerMonitor`는 **데이터 조회**에, `FormatUtils`는 **데이터 표현**에 집중합니다.

다음과 같은 코드는 지양합니다.

```java
ServerMonitor.getCPUUsageString();
ServerMonitor.getFormattedMemory();
ServerMonitor.getReadableUptime();
```

대신 원본 데이터와 표현 방식을 분리합니다.

```java
ServerMonitor.getCPUUsage();
FormatUtils.formatPercentage(...);

ServerMonitor.getMemoryUsed();
FormatUtils.formatBytes(...);

ServerMonitor.getUptime();
FormatUtils.formatDuration(...);
```

---

## 요구사항

- Java 17+ (`record`, `Duration`, Management API 사용)

---

## 알려진 제약사항

- **`com.sun.management.OperatingSystemMXBean` 사용**: HotSpot(OpenJDK/Oracle JDK) 내부 API입니다. README의 "외부 런타임 의존성 없음"은 별도 라이브러리를 안 쓴다는 의미이며, 모든 JVM 구현체(예: 일부 임베디드/커스텀 JVM)에서 100% 보장되는 표준 API는 아닙니다.
- **측정 불가 값은 `NaN`으로 통일**: `getCPUUsage()`와 `getSystemLoad()`는 OS가 값을 제공하지 못하는 경우(최초 호출 직후, Windows에서의 `load` 등) `Double.NaN`을 반환합니다. 과거에는 이를 각각 `0.0`, `-1.0`으로 흘려보내 "실제로 유휴/로드 없음"과 구분이 안 됐지만, 지금은 `Double.isNaN(...)`으로 명시적으로 걸러내야 합니다.

## 범위(Scope)

현재 버전은 **조회 및 데이터 표현**에 집중합니다. 포함하지 않는 기능:

- 자동 알림
- Webhook
- 서버 제어 / 프로세스 종료 / 자동 재시작
- 외부 모니터링 서버 연동
- 주기적인 polling
- Threshold 관리

이러한 기능은 이후 별도의 Monitoring/Alerting 계층에서 처리하는 것을 전제로 합니다.
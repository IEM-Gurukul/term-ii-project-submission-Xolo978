# PCCCS495 – Term II Project

## Project Title

**SwiftLoad - Multithreaded File Downloader**

---

## Problem Statement

Standard file downloaders utilize only a single thread for fetching data, hence underutilizing network bandwidth. This
leads to increased latency for large assets. Additionally, utilizing a single thread could cause UI freezes during heavy
I/O operations. The proposed file downloader addresses these limitations by segmenting files into logical byte-wise
partitions and downloading them concurrently via multiple threads. It utilizes the observer pattern to notify background
workers about UI changes such as speed and progress, ensuring a responsive and informative user experience.

---

## Target User

People downloading large files at once who need faster download speeds and real-time progress visibility across multiple
concurrent threads.

---

## Core Features

**Automatic splitting of files into chunks and parallel downloading**: Files are intelligently divided into byte chunks,
and downloading is done in parallel by a thread pool, thus maximizing network bandwidth utilization.

- **Live tracking of download metrics**: Real-time display of thread-wise download speeds, overall download progress,
  and ETA is provided, keeping you updated on the status of your downloads.
- **Support for HTTP Range Requests Detection**: Automatically detects whether the server supports partial downloading
  of files by sending HTTP/1.1 206 Partial Content. If it does not, it automatically reverts to single-threaded mode.
- **Intuitive GUI with thread-wise monitoring**: JavaFX is used to provide a user interface that displays thread-wise
  information about the downloaded files.

---

## OOP Concepts Used

- **Abstraction**: `DownloadListener` interface abstracts callback logic; `DownloadManager` and `DownloadWorker`
  abstract the internal working and execution of downloads.
- **Inheritance**: `DownloadUiListener` implements `DownloadListener` to provide GUI-specific event handling and state
  management.
- **Polymorphism**: Multiple listener implementations override callback methods with different behaviors;
  `Callable<Boolean>` pattern for thread execution.
- **Exception Handling**: Handling of `IOException`, `InterruptedException`, `CancellationException`, and network
  errors in `MetaDataFetcher`, `DownloadManager`, and `DownloadWorker`.
- **Collections / Threads**: `ExecutorService` with fixed thread pool, `List<DownloadPart>` for partitioning,
  `ConcurrentHashMap` for thread-safe per-thread state, `AtomicLong` for synchronized counters, and
  `Platform.runLater()` for JavaFX thread-safe UI updates.

---

## Proposed Architecture Description

An event-driven architecture is used, based on the Observer pattern. A `DownloadManager` is used as a central Subject,
calculating the file segments by a `DownloadPartitioner` and then distributing them to several `DownloadWorker` threads.
All of these threads simply use `RandomAccessFile` to write bytes to calculated offsets in a file.

When receiving data, all of these threads notify a registered observer for a GUI, `DownloadUiListener`, by calling
`DownloadListener`, without blocking the main execution thread. The implementation of `DownloadUiListener` uses
`Platform.runLater()` to ensure thread safety for JavaFX.

The implementation automatically recognizes whether a server supports HTTP range requests by using `MetaDataFetcher`,
and it will automatically switch to single-threaded operation when a server does not support partial content
downloading (HTTP status code 205).

---

## How to Run

### Prerequisites

- Java 20 or later
- Gradle 8.x
- JavaFX 26

### Running the Application

```bash
# Run the downloader GUI
./gradlew run
```

The GUI will launch with:

- URL input field (default: example 100MB file)
- Dynamic thread count slider (auto-detects CPU core count)
- Overall progress bar and speed/ETA display
- Per-thread progress tracking with individual speeds

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run a specific test
./gradlew test --tests com.xolo.swiftload.util.DownloadPartitionerTest
```

### Build

```bash
# Clean rebuild
./gradlew clean build

# Compile only (no tests)
./gradlew compileJava
```

---

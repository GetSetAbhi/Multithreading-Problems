# 📂 Async File Reader & Writer in Java

## There are two projects under File Reader

If you want to check a case where we limit our BufferedReader to a certain size of memory 
then check out [MemoryLimitedChunk](memorylimited.md) project.

I have added a maven project which can be directly imported for use.

This project demonstrates a simple asynchronous file reader and writer system built in **pure Java**.

## 🧠 Overview

The system consists of two primary services:
- `FileReaderService`: Reads files asynchronously from the classpath.
- `FileWriterService`: Writes the contents to disk as separate text files.

These services use **concurrent queues and thread pools** to efficiently process files in parallel.

---

## 🔧 Components

### 📘 FileReaderService

- Uses a `LinkedBlockingQueue<String>` to accept file paths.
- Runs a **dedicated reader thread** that picks file paths from the queue and submits them to a thread pool (`ExecutorService`) for parallel reading.
- Reads the contents line by line and sends the final aggregated content to `FileWriterService`.

### 📙 FileWriterService

- Uses a `LinkedBlockingQueue<String>` to accept file contents.
- Runs a **writer thread** that continuously pulls from the queue and submits writing tasks to a thread pool.
- Creates a file with a random UUID as the name and writes the content line by line.

---

## ⚙️ Design Highlights

- ✅ **Producer-Consumer Pattern**: Decouples file reading from writing.
- ✅ **Thread-safe** with `LinkedBlockingQueue`.
- ✅ **Graceful shutdown** using `shutdown()` method and `ExecutorService` termination.
- ✅ **Scalable**: Uses `Runtime.getRuntime().availableProcessors()` for thread pool sizing.
- ✅ **Resource-safe**: Uses `try-with-resources` for proper stream closure.

---

## Purpose of thread pool

```java
this.service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
```

This line initializes a thread pool with a fixed number of threads equal to the number of available CPU cores on the machine.

# Example 

If your machine has 8 cores:

This means:

At most 8 file reading or writing tasks will run in parallel.
Any extra tasks will wait in a queue.


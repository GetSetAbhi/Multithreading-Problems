# The purpose and Application of this concept

In an interview, I was asked this question:

> "You have a system with 2GB memory, and thousands of files each several MBs in size.  
> How would you read these files without overwhelming the system?"

I didn’t know the full answer then, but I learned and documented my solution in this project.

---

## Reading a file chunk asynchronously

The **MemoryLimitedChunk** project demonstrates a simple asynchronous file reader where we set a configurable chunk size limit on the buffered reader.

To understand the code here, it helps to know what a **BufferedReader** is and how it works.

## Purpose of BufferedReader

Instead of reading one character at a time from a file or stream (which is slow),  
**BufferedReader** reads a chunk (buffer) at once and then serves characters or lines from that buffer.

By default, Java's BufferedReader reads **8,192 characters (16 KB)** at a time.

If I want to read **32 KB** of data at once, I can create it like this:

| Value       | Meaning                                     |
| ----------- | ------------------------------------------- |
| `1024`      | 1 kilobyte (KB) = 1024 bytes (or characters) |
| `32 * 1024` | 32 KB buffer                               |

Example code:

```java
BufferedReader reader = new BufferedReader(new FileReader("file.txt"), 32 * 1024); // 32 KB buffer
```

## Concepts used

### Problem Recap

You have many large files arriving concurrently, and want to process them asynchronously **without exhausting memory**.

### Solutions

#### 1. Backpressure

**What is it?**  
A mechanism to slow down or pause the input when the system is overwhelmed.

**How to apply:**  
- Use **bounded queues** (e.g., `LinkedBlockingQueue` with fixed capacity) to hold files or file data chunks.  
- When the queue is full, block or reject new submissions until space is freed.  
- This naturally slows down the producer, preventing unbounded memory growth.

**Example:**  
You have 3 consumer threads processing files concurrently, each taking files from a queue of capacity 5.  
If more than 5 files arrive while the consumers are busy, producers will block until space is available — this is backpressure in action.

At any moment in below example:

- 3 files are actively being processed by the 3 consumer threads (thread pool size = 3).
- 5 files can wait in the bounded queue (capacity = 5).

So, the system can hold up to 8 files simultaneously — either processing or waiting in queue.


```java
import java.util.concurrent.*;

public class FileProcessingWithBackpressure {

    private final BlockingQueue<String> fileQueue = new LinkedBlockingQueue<>(5);
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public void submitFile(String filePath) throws InterruptedException {
        System.out.println("Trying to submit: " + filePath);
        fileQueue.put(filePath);  // blocks if queue is full (backpressure)
        System.out.println("Submitted: " + filePath);
    }

    public void startProcessing() {
        Runnable consumerTask = () -> {
            try {
                while (true) {
                    String file = fileQueue.take();  // blocks if queue empty
                    processFile(file);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        for (int i = 0; i < 3; i++) {
            executor.submit(consumerTask);
        }
    }

    private void processFile(String file) {
        System.out.println("Processing " + file);
        try {
            Thread.sleep(2000); // simulate processing delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Finished processing " + file);
    }

    public static void main(String[] args) throws InterruptedException {
        FileProcessingWithBackpressure service = new FileProcessingWithBackpressure();
        service.startProcessing();

        for (int i = 1; i <= 10; i++) {
            service.submitFile("file_" + i + ".txt");
            System.out.println("Submitted file_" + i);
        }

        service.executor.shutdown();
        service.executor.awaitTermination(1, TimeUnit.MINUTES);
    }
}
```
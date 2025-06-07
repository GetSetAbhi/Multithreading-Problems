# 🧵 Producer-Consumer Problem in Java (Using `wait()`/`notifyAll()`)

## ✅ Problem Statement

The **Producer-Consumer** problem is a classic concurrency scenario where:

- One or more threads (Producers) **produce data**.
- One or more threads (Consumers) **consume data**.
- A **shared resource (buffer)** is accessed concurrently.
- Synchronization ensures **safe access** and prevents **race conditions**.

---

## 💡 Concepts Used

- Java Multi-threading
- Synchronization (`synchronized`)
- Inter-thread communication (`wait()` / `notifyAll()`)
- Shared resource coordination using a flag (`canProduce`)

---

## 🧪 Code Demo

### 👨‍🏭 ProducerConsumerDriver.java

```java
public class ProducerConsumerDriver {

	public static void main(String[] args) {
		
		SharedResource resource = new SharedResource();
		
		Thread producer = new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				try {
					resource.produce(i);
				} catch (InterruptedException e) {
					System.err.print(e);
				}
			}
		});
		
		Thread consumer = new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				try {
					resource.consume();
				} catch (InterruptedException e) {
					System.err.print(e);
				}
			}
		});
		
		producer.start();
		consumer.start();
	}

}

class SharedResource {
	private int data;
	private Object lock;
	private boolean canProduce;
	
	
	public SharedResource() {
		this.lock = new Object();
		this.canProduce = true;
	}

	public void produce(int a) throws InterruptedException {
		synchronized (lock) {
			while (this.canProduce == false) {
				lock.wait();
			}
			this.data = a;
			System.out.println("Producing Data : " + this.data);
			this.canProduce = false;
			lock.notify();
		}
	}
	
	public void consume() throws InterruptedException {
		synchronized (lock) {
			while (this.canProduce == true) {
				lock.wait();
			}
			System.out.println("Consuming Data : " + this.data);
			this.canProduce = true;
			lock.notify();
		}
	}
}
```

## 📝 Note: Solving with `Semaphore`

Java's `Semaphore` from `java.util.concurrent` offers a structured and clean alternative to solving the Producer-Consumer problem, 
especially when dealing with **bounded buffers** or **multiple producers and consumers**.

### 🔁 Concept Overview

- Use **two semaphores**:
  - `produce`: Controls when the producer can produce (initialized to **1** so producer can start).
  - `consume`: Controls when the consumer can consume (initialized to **0** so consumer initially waits).

- **Producer behavior**:
  - Acquires a permit from the `produce` semaphore before producing.
  - Releases a permit to the `consume` semaphore after producing.

- **Consumer behavior**:
  - Acquires a permit from the `consume` semaphore before consuming.
  - Releases a permit to the `produce` semaphore after consuming.

### ✅ Benefits of Using `Semaphore`

- Avoids manual use of `wait()` and `notifyAll()`.
- Simplifies synchronization and coordination logic.
- Makes the solution easily extendable to **multiple producers and consumers**.

> Semaphores make concurrent logic cleaner, more readable, and easier to scale compared to traditional locking mechanisms.


```
import java.util.concurrent.Semaphore;

class SharedResource {
    private int data;
    private final Semaphore produce = new Semaphore(1);
    private final Semaphore consume = new Semaphore(0);

    public void produce(int value) throws InterruptedException {
        produce.acquire();
        data = value;
        System.out.println("Producing Data: " + data);
        consume.release();
    }

    public void consume() throws InterruptedException {
        consume.acquire();
        System.out.println("Consuming Data: " + data);
        produce.release();
    }
}

```
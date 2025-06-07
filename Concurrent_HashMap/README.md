# Custom ConcurrentMap in Java

I have added a maven project which can be directly imported for use.

## Overview

This is a custom implementation of a thread-safe `ConcurrentMap` in Java, designed to mimic the behavior of `ConcurrentHashMap` 
without using any of Java's built-in concurrent map classes. The core idea is to ensure:

- **Thread safety per key** using per-key locking via `ReentrantLock`.
- **Non-blocking behavior** when multiple threads operate on different keys.
- **Serialized access** only when multiple threads operate on the same key.

## Implementation Highlights

- Each key has its own lock stored in a `Map<K, Lock>`.
- Lock creation is synchronized using a shared mutex (`locksMutex`).
- `put`, `get`, and `remove` methods ensure safe concurrent access.
- Unused locks are cleaned up during `remove` to prevent memory leaks.

## Why Per-Key Locking?

Using a separate lock for each key allows:
- **Concurrent access to different keys**, improving throughput.
- **Serialized access to the same key**, ensuring thread safety without unnecessary blocking.

### How lock-per-key works?

- You maintain a map of locks, one lock per key.
- When a thread wants to operate on a key, it:
	1. Gets the lock for that key.
	2. Acquires that lock (locks it).
	3. Performs the operation (e.g., put, remove).
	4. Releases the lock (unlocks it).
- Since each key has its own lock, threads working on different keys don’t block each other.
- But threads working on the same key must wait for each other — ensuring serialized access per key.

## Potential Bottleneck

While per-key locking improves concurrency, creating/retrieving a lock still requires synchronization on `locksMutex`.

### What’s happening with locksMutex?

- The locksMutex is a single global lock used to safely access and modify the locks map which stores per-key locks.
- So, whenever any thread wants to get or create a lock for a key, it has to acquire this global locksMutex first.
- This means all threads serially access the locks map itself, even if they want locks for different keys.

### Why *computeIfAbsent* ?

The purpose of computeIfAbsent in Java is to compute and insert a value for a key if it is not already present in a Map. 
It's a thread-safe and atomic way to avoid race conditions during "check-then-act" logic.

## Example Usage

```java
public class Main {
    public static void main(String[] args) {
        ConcurrentMap<String, Integer> map = new ConcurrentMap<>();

        Thread t1 = new Thread(() -> {
            map.put("foo", 1);
        });

        Thread t2 = new Thread(() -> {
            map.put("bar", 2);
        });

        Thread t3 = new Thread(() -> {
            System.out.println(map.get("foo"));
        });

        t1.start();
        t2.start();
        t3.start();
    }
}

public class ConcurrentMap<K, V> {

	private Map<K, V> map;
	private Map<K, Lock> locks;

	private Object locksMutex;

	public ConcurrentMap() {
		this.map = new HashMap<>();
		this.locks = new HashMap<>();
		this.locksMutex = new Object();
	}
	
	private Lock getLock(K key) {
		synchronized (locksMutex) {
			return this.locks.computeIfAbsent(key,k -> new ReentrantLock());
		}
	}

	public void put(K key, V value) {
		Lock lock = getLock(key);
		lock.lock();
		try {
			this.map.put(key, value);
		} finally {
			lock.unlock();
		}
	}

	public V get(K key) {
		return this.map.get(key);
	}
	
	public void remove(K key) {
		Lock lock;
		synchronized (locksMutex) {
			lock = locks.get(key);
			if (lock == null) {
				return; // no such key
			}
		}

		lock.lock();
		try {
			map.remove(key);
		} finally {
			lock.unlock();
		}

		// Optional: cleanup locks map to avoid leak
		synchronized (locksMutex) {
			if (!map.containsKey(key)) { 
				// only remove lock if key no longer in map
				locks.remove(key);
			}
		}
	}
}
```

## Limitations

- `get` is not synchronized: it's eventually consistent and may return stale data if not combined with external synchronization.
- Lock cleanup in `remove` is basic and assumes key removal happens less frequently.

---
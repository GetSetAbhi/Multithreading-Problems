package com.demo.concurrent.map;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
			lock = getLock(key);
			if (lock == null) {
				return;
			}
		}
		
		try {
			this.map.remove(key);
		} finally {
			lock.unlock();
			synchronized (locksMutex) {
				Lock currentLock = getLock(key);
				if (lock == currentLock) {
					this.locks.remove(key);
				}
			}
		}
	}
}

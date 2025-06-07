package com.demo.main;

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

package com.demo.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class FileReaderService {

	private LinkedBlockingQueue<String> fileQueue;
	private ExecutorService executorService;
	private Thread fileReaderService;
	private boolean readingStatus;
	
	public FileReaderService() {
		readingStatus = true;
		this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		this.fileQueue = new LinkedBlockingQueue<>();
		startReading();
	}
	
	public void readFile(String filePath) {
		try {
			this.fileQueue.put(filePath);
		} catch (InterruptedException e) {
			System.out.println(e);
		}
	}
	
	private void startReading() {
		fileReaderService = new Thread(() -> {
			while (readingStatus || !this.fileQueue.isEmpty()) {
				try {
					String filePath = this.fileQueue.take();
					this.executorService.execute(() -> startFileReadingAsync(filePath));
				} catch (InterruptedException e) {
					System.out.println(e.getMessage());
				}
			}
		});
		fileReaderService.start();
	}
	
	private void startFileReadingAsync(String filePath) {
		// Reading 32kb worth in input at once.
		// 32 x 1024 Bytes
		try (BufferedReader br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(filePath)), 32*1024)) {
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}

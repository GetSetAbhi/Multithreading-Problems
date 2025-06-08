package com.reader.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class FileReaderService {

	private LinkedBlockingQueue<String> fileQueue;
	private ExecutorService service;
	private volatile boolean running = true;
	private FileWriterService writerService;
	private Thread readerThread;
	
	public FileReaderService() {
		this.fileQueue = new LinkedBlockingQueue<String>();
		this.service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		startReading();
	}
	
	public void fileRead(String filePath) throws InterruptedException {
		this.fileQueue.put(filePath);
	}
	
	private void startReading() {
		readerThread = new Thread(() -> {
			while (this.running || !fileQueue.isEmpty()) { 
				try {
					String filePath = this.fileQueue.take();
					this.service.execute(() -> readFileContents(filePath));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		readerThread.start();
	}
	
	private void readFileContents(String filePath) {
		try (BufferedReader bf = new BufferedReader(
					new InputStreamReader(ClassLoader.getSystemResourceAsStream(filePath)))){
			String line;
			StringBuilder builder = new StringBuilder();
			while ((line = bf.readLine()) != null) {
				builder.append(line).append("\n");
				System.out.println(line);
			}
			if (!builder.isEmpty()) {
				writerService.assembleFile(builder.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		
		if (this.service != null) {
			try {
				this.running = false;
				this.service.shutdown();
				if (!this.service.awaitTermination(10, TimeUnit.SECONDS)) {
					this.service.shutdownNow();
				}
				this.readerThread.interrupt();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	public void setWriterService(FileWriterService writerService) {
		this.writerService = writerService;
	}
}

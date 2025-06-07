package com.reader.demo;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class FileWriterService {
	
	private LinkedBlockingQueue<String> fileQueue;
	private ExecutorService service;
	private volatile boolean running = true;
	private Thread writerThread;
	
	public FileWriterService() {
		this.fileQueue = new LinkedBlockingQueue<>();
		this.service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		this.startWriting();
	}
	
	public void assembleFile(String contents) {
		this.fileQueue.add(contents);
	}
	
	private void startWriting() {
		this.writerThread = new Thread(() -> {
			while (this.running || !this.fileQueue.isEmpty()) {
				try {
					String content = this.fileQueue.take();
					service.submit(() -> createFile(content));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		this.writerThread.start();
	}
	
	private void createFile(String fileContents) {
		BufferedWriter bf = null;
		try {
			if (fileContents != "" && !fileContents.equals("\n")) {
				String[] contents = fileContents.split("\n");
				if (contents != null && contents.length > 0) {
					String fileName = UUID.randomUUID().toString() + ".txt";
					bf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
					for (String fileLine : contents) {
						if (fileLine != "" && !fileLine.equals("\n")) {
							bf.write((fileLine + "\n").toCharArray());
						}
					}
					System.out.println("Contents have been written inside file :" + fileName);
				}
			}
			if (bf != null) {
				bf.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}

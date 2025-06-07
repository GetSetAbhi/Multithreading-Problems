package com.reader.demo;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ReaderTest {

	public static void main(String[] args) throws IOException {
		List<String> files = Arrays.asList("file1.txt", "file2.txt","file3.txt");
		FileWriterService writerService = new FileWriterService();
		FileReaderService service = new FileReaderService();
		service.setWriterService(writerService);
		
		for(String file : files) {
			try {
				service.fileRead(file);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}


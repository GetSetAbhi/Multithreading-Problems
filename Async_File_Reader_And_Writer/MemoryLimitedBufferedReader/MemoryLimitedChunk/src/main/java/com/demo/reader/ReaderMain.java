package com.demo.reader;

import java.util.Arrays;
import java.util.List;

public class ReaderMain {

	public static void main(String[] args) {
		List<String> files = Arrays.asList("file1.txt", "file2.txt","file3.txt");
		FileReaderService service = new FileReaderService();
		
		for(String file : files) {
			service.readFile(file);
		}

	}

}

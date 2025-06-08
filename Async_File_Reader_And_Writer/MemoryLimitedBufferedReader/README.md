# Reading a file chunk asynchronously

To understand the code in this project we first need have an understanding of BufferedReader.

## Purpose of BufferedReader

Instead of reading one character at a time from the file or stream (which is slow), 
BufferedReader reads a chunk (buffer) at once, and then serves characters or lines from that buffer.

By default, a Java BufferedReader reads 8,192 characters (or 16 KB) at a time.

Suppose if I want to read 32kb of data at once then

| Value       | Meaning                                      |
| ----------- | -------------------------------------------- |
| `1024`      | 1 kilobyte (KB) = 1024 bytes (or characters) |
| `32 * 1024` | 32 KB buffer                                 |

Now we can define our buffered reader like this

```
BufferedReader reader = new BufferedReader(new FileReader("file.txt"), 32 * 1024); // 32 KB buffer
```

This is the whole basis of this project, in which I have defined my ReaderService to read 32kb of data at once from a file

package main;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class FileFragmentation {
	RandomAccessFile file;
	long numFrags; 
	long max_buffer_size;
	HashMap<Integer, byte[]> fileMap;

	private void makeFileMap(RandomAccessFile file, long numBytes, int id) throws IOException {
		long offset = id * numBytes;
		byte[] buf = new byte[(int) numBytes];
		file.seek(offset);
		file.read(buf, 0, (int) numBytes);
		fileMap.put(id, buf);
	}

	public void printFileMap() {
		System.out.println(fileMap);
	}

	public long getNumFrags() {
		return numFrags;
	}

	public void setNumFrags(long numFrags) {
		this.numFrags = numFrags;
	}

	public HashMap<Integer, byte[]> getFileMap() {
		return fileMap;
	}

	public void setFileMap(HashMap<Integer, byte[]> fileMap) {
		this.fileMap = fileMap;
	}

	public FileFragmentation(String fileName) throws IOException {
		fileMap = new HashMap<Integer, byte[]>();
		file = new RandomAccessFile(fileName, "r");
		max_buffer_size = 64000;
		numFrags = file.length() / max_buffer_size;
		if(file.length() % max_buffer_size != 0)
			numFrags++;
		
		if (numFrags < 1) numFrags = 1;

		for (int dest = 0; dest < numFrags; dest++) {
			makeFileMap(file, max_buffer_size, dest);
		}
		file.close();
	}


}

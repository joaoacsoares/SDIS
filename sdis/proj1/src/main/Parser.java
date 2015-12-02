package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Parser {

	public enum Headers {PUTCHUNK, GETCHUNK, STORED, DELETE, CHUNK, REMOVED, TRASH};

	private Headers protocol;
	private String message;

	public Parser(String message) {
		this.message = message;
		System.out.println(message);

		protocol = getProtocol(message);
	}

	public Headers getProtocol(String message) {
		Headers ret;
		if (message.startsWith("PUTCHUNK"))
			ret = Headers.valueOf("PUTCHUNK");
		else if (message.startsWith("GETCHUNK"))
			ret = Headers.valueOf("GETCHUNK");
		else if (message.startsWith("STORED"))
			ret = Headers.valueOf("STORED");
		else if (message.startsWith("DELETE"))
			ret = Headers.valueOf("DELETE");
		else if (message.startsWith("CHUNK"))
			ret = Headers.valueOf("CHUNK");
		else if (message.startsWith("REMOVED"))
			ret = Headers.valueOf("REMOVED");
		else
			ret = Headers.valueOf("TRASH");

		return ret;
	}

	public String assignChunk() {
		String msg = "";
		switch (protocol) {
		case PUTCHUNK:
			System.out.println("Received PUTCHUNK");
			if(saveChunk())
				msg = confirmBackup();
			break;
		case GETCHUNK:
			System.out.println("Received GETCHUNK");
			msg = confirmRestore();
			break;
		case STORED:
			System.out.println("Received STORED");
			//handled by confirm listener
			break;
		case DELETE:
			System.out.println("Received DELETE");
			boolean deleted = deleteChunks();
			if(deleted) System.out.println("DELETED Chunks");
			break;
		case CHUNK:
			System.out.println("Received CHUNK");
			break;
		case REMOVED:
			break;
		case TRASH:
			System.out.println("Invalid Message");
			break;
		default:
			System.out.println("bad message");
			break;
		}
		return msg;
	}

	private boolean deleteChunks() {
		String[] filename = message.split(" ");
		Path path = Paths.get(filename[2]);
		boolean ret = false;
		try {
			ret = Files.deleteIfExists(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		 return ret; 
	}

	private boolean saveChunk() {
		String[] filename = message.split(" ");

		byte[] flagValue = { 0xD, 0xA };
		String crlf = new String(flagValue);

		String[] data = message.split(crlf + crlf);

		String fileIDChunkNo = filename[2] + "." + filename[3];

		File f = new File(fileIDChunkNo);
		if(f.exists()) {
			System.out.println("ERROR: Chunk repetition");
			return true; 
		}

		try {
			FileOutputStream chunkData = new FileOutputStream(fileIDChunkNo);

			for (int i = 1; i < data.length; i++) {
				if (i != 1) {

					chunkData.write(crlf.getBytes());
					chunkData.write(crlf.getBytes());
				}
				chunkData.write(data[i].getBytes());
			}
			chunkData.close();
			
		} catch (IOException e) {

			e.printStackTrace();
		}
		return true;
	}
	
	public boolean validChunk(String fileName, Integer chunkNo)
	{
		String[] fileNameReceive = message.split(" ");

		if((fileNameReceive[1].equals("1.0") 
				&& fileName.equals(fileNameReceive[2]) 
				&& fileNameReceive[3].equals(chunkNo.toString()))
				)
			return true;
		else
			return false;
		
	}


	public String readChunk()
	{
		String msg = "";
		
		byte[] ascii = { 0xD, 0xA };
		String crlf = new String(ascii);

		String[] data = message.split(crlf + crlf);

		for(int j = 0; j < data.length;j++)
			msg = data[j];

		return msg;
	}


	public boolean confirmStored() {
		String[] tokens = message.split(" ");
		byte ascii[] = { 0xD, 0xA };

		byte[] crlfToken = tokens[4].getBytes();

		if (tokens[2].equals(Main.storedFiles.get("filename"))
				&& tokens[3].equals(Main.storedFiles.get("chunkNo"))
				&& (crlfToken[0] == ascii[0] && crlfToken[1] == ascii[1])) {
			// notify();
			return true;

		} else {
			if (!(tokens[2].equals(Main.storedFiles.get("filename")))) {
				System.out.println("FAILED FILENAME: "
						+ Main.storedFiles.get("filename"));
				System.out.println("TOKENS2: " + tokens[2]);
			}
			if (!(tokens[3].equals(Main.storedFiles.get("chunkNo")))) {
				System.out.println("FALIED CHUNKNO: "
						+ Main.storedFiles.get("chunkNo"));
				System.out.println("TOKENS 3: " + tokens[3]);

			}

			if (!(crlfToken[0] == ascii[0] && crlfToken[1] == ascii[1])) {
				System.out.println("FAILED CRLF: " + ascii[0] + " + "
						+ ascii[1]);
				System.out.println("TOKENS 4: " + crlfToken[0] + " + "
						+ crlfToken[1]);
			}
			return false;
		}

	}

	private String confirmBackup() {
		String stored = "STORED";
		String[] tokens = message.split(" ");

		if (tokens.length < 6)
		{
			System.out.println("bad message " + message);
			return "";
		}

		stored += " " + tokens[1] + " " + tokens[2] + " " + tokens[3] + " " + tokens[5];
		System.out.println(stored);
		return stored;
	}

	private String confirmRestore() {
		String stored = "CHUNK";
		String[] tokens = message.split(" ");
		if (tokens.length < 5)
		{
			System.out.println("bad message " + message);
			return "";
		}
		stored += " " + tokens[1] + " " + tokens[2] + " " + tokens[3] + " "
				+ tokens[4];
		

		byte[] buffer= new byte[64000];
		try {
			RandomAccessFile chunk = new RandomAccessFile(tokens[2] +"."+tokens[3], "r");
			
			chunk.read(buffer);
			
			stored += buffer;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return stored;
		
	}
	
	

}

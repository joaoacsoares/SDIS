package main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;



public class FileManager {

	private InetAddress mcAddr;
	private InetAddress mdbAddr;
	private InetAddress mdrAddr;
	private int mcPort;
	private int mdbPort;
	private int mdrPort;

	public FileManager(int mcPort, String mcAddress, int mdbPort, String mdbAddress, int mdrPort, String mdrAddress) throws UnknownHostException {

		mcAddr = InetAddress.getByName(mcAddress);
		this.mcPort = mcPort;
		mdbAddr = InetAddress.getByName(mdbAddress);
		this.mdbPort = mdbPort;
		mdrAddr = InetAddress.getByName(mdrAddress);
		this.mdrPort = mdrPort;

	}
	private void backup() throws InterruptedException {
		System.out.println("***    BACKUP FILE    ***");
		String fileName;
		Integer repDegree;
		String header;
		HashMap<Integer, byte[]> map;


		System.out.println("Filename?");
		System.out.println("->");
		fileName = System.console().readLine();
		if (fileName.equals(""))
			fileName = System.console().readLine();

		try {
			FileFragmentation split = new FileFragmentation(fileName);
			map = split.getFileMap();
		} catch (IOException e) {
			System.out.println("fail on file split");
			map = new HashMap<Integer, byte[]>();
			e.printStackTrace();
		}
		System.out.println("Replication Degree?");
		System.out.println("->");
		repDegree = Integer.parseInt(System.console().readLine());

		byte[] flagValue = { 0xD, 0xA };
		String crlf = new String(flagValue);


		for (Integer j = 0; j < map.size(); j++) {

			header = "PUTCHUNK 1.0 " + fileName + " " + j.toString() + " "
					+ repDegree.toString() + " " + crlf + crlf + map.get(j);
			for (int i = 0; i < repDegree; i++)							
			{
				Main.storedFiles.replace("filename", fileName);
				Main.storedFiles.replace("chunkNo", j.toString());

				try (DatagramSocket multicastSocket = new DatagramSocket()) {
					DatagramPacket msgPacket = new DatagramPacket(header.getBytes(), header.getBytes().length, mdbAddr, mdbPort);
					multicastSocket.send(msgPacket);

					System.out.println("FILEMANAGER sent packet with msg: " + header);
					Thread.sleep(500);
					if (receiveStoredChunk(repDegree, fileName, j)) {
						System.out.println("FILEMANAGER: obtained the desired rep degree");
						Main.storedFiles.replace("confirmationCount", "0");
						break;
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}

			}
		}
	}

	private void restore() {
		System.out.println("***    RESTORE FILE    ***");
		String fileName;
		String header;
		HashMap<Integer, byte[]> chunkMap = new HashMap<Integer, byte[]>();

		byte[] flagValue = { 0xD, 0xA };
		String crlf = new String(flagValue);

		System.out.println("filename?");
		System.out.println("->");
		fileName = System.console().readLine();
		if (fileName.equals(""))
			fileName = System.console().readLine();

		Integer j = 0;

		while (true) {

			header = "GETCHUNK 1.0 " + fileName + " " + j.toString() + " "
					+ crlf + crlf;

			try (DatagramSocket multicastSocket = new DatagramSocket()) {
				DatagramPacket msgPacket = new DatagramPacket(header.getBytes(), header.getBytes().length, mcAddr,mcPort);
				multicastSocket.send(msgPacket);

				System.out.println("FILEMANAGER sent packet with msg: " + header);
				// Thread.sleep(500);

			} catch (IOException ex) {
				ex.printStackTrace();
			}

		

			RestoreChunk chunk = new RestoreChunk(mdrPort, mdrAddr, fileName, j);
			byte[] data = chunk.assignChunkToFile();
			chunkMap.put(j, data);

			if (data.length < 64000)
				break;

			j++;
		}
		chunksToFile(fileName,chunkMap);

	}

	private void delete() {
		System.out.println("***    DELETE FILE    ***");
		String fileName;
		String version;
		String header;
		System.out.println("Filename?");
		System.out.println("->");
		fileName = System.console().readLine();
		if (fileName.equals(""))
			fileName = System.console().readLine();

		System.out.println("Version (default = version 1.0)?");
		System.out.println("->");
		version = System.console().readLine();
		if (version.equals(""))
			version = "1.0";

		byte[] flagValue = { 0xD, 0xA };
		String crlf = new String(flagValue);
		header = "DELETE" + " " + version + " " + fileName + " " + crlf + crlf;
		
			try (DatagramSocket multicastSocket = new DatagramSocket()) {
				DatagramPacket msgPacket = new DatagramPacket(header.getBytes(), header.getBytes().length, mcAddr,mcPort);
				multicastSocket.send(msgPacket);

				System.out.println("FILEMANAGER sent packet with msg: " + header);
			

			} catch (IOException ex) {
				ex.printStackTrace();
			
		}
		
		
		
	}






public void mainMenu() throws InterruptedException {
	int choice;

	System.out.println("***    MAINMENU    ***");
	System.out.println("choose an option:");
	System.out.println("1- backup file");
	System.out.println("2- restore file");
	System.out.println("3- delete file");
	System.out.println("4- space reclaiming");
	System.out.println("5- exit");
	System.out.println("->");
	choice = Integer.parseInt(System.console().readLine());

	switch(choice){
	case 1:
		backup();
		break;
	case 2:
		restore();
		break;
	case 3:
		delete();
		break;
	case 4:
		//spaceReclaim();
		break;
	case 5:
		return;
	}
}




private void chunksToFile(String filename ,HashMap<Integer, byte[]> chunkMap){

	try {
		FileOutputStream data = new FileOutputStream(filename);

		for (int i = 0;i<chunkMap.size(); i++) {
			data.write(chunkMap.get(i));
		}
		data.close();
	} catch (IOException e) {
		e.printStackTrace();
	}

}



public void sendConfirmation(String msg) {

	if (msg.startsWith("STORED"))
		sendToMc(msg);
	else if (msg.startsWith("CHUNK"))
		sendToMdr(msg);

}

public void sendToMc(String msg) {

	try (DatagramSocket multicastSocket = new DatagramSocket()) {
		DatagramPacket msgPacket = new DatagramPacket(
				msg.getBytes(),
				msg.getBytes().length, mcAddr, mcPort);
		multicastSocket.send(msgPacket);
		System.out.println("Server sent packet with msg: " + msg);

	} catch (IOException ex) {
		ex.printStackTrace();
	}

}

public void sendToMdb(String msg) {
	try (DatagramSocket multicastSocket = new DatagramSocket()) {
		DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),msg.getBytes().length, mdbAddr, mdbPort);
		multicastSocket.send(msgPacket);
		System.out.println("Server sent packet with msg: " + msg);

	} catch (IOException ex) {
		ex.printStackTrace();
	}

}

public void sendToMdr(String msg) {
	try (DatagramSocket multicastSocket = new DatagramSocket()) {
		DatagramPacket msgPacket = new DatagramPacket(
				msg.getBytes(),
				msg.getBytes().length, mdrAddr, mdrPort);
		multicastSocket.send(msgPacket);
		System.out.println("Server sent packet with msg: " + msg);

	} catch (IOException ex) {
		ex.printStackTrace();
	}

}

public boolean receiveStoredChunk(int repDegree, String filename,int chunkNo) {
	int confirmations = 0;

	try {
		ConfirmListener confirmationListener = new ConfirmListener(mcPort,mcAddr, filename, chunkNo);
		confirmationListener.start();
		synchronized (confirmationListener) {
			confirmationListener.wait(500);
		}
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
	confirmations = Integer.parseInt(Main.storedFiles.get("receivedCount"));
	if (confirmations < repDegree)
		return false;
	else {
		return true;
	}
}
}



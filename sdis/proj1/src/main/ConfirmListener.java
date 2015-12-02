package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ConfirmListener extends Thread {

	private InetAddress addr;
	private int port;
	private byte[] dataBuffer = new byte[256];
	private String file;
	private Integer nChunk;

	public ConfirmListener(int port, InetAddress INET_ADDR, String file, int nChunk) {

		addr = INET_ADDR;
		this.port = port;
		this.file = file;
		this.nChunk = nChunk;

	}

	public void run() {
		Parser parsed;

		boolean ok = true;
		boolean repeating;
		do
		{

			try (MulticastSocket socketMC = new MulticastSocket(port)) {
				socketMC.joinGroup(addr);
				socketMC.setLoopbackMode(true);

				DatagramPacket messagePacket = new DatagramPacket(dataBuffer, dataBuffer.length);
				socketMC.receive(messagePacket);

				String messageToParse = new String(dataBuffer, 0, dataBuffer.length);

				parsed = new Parser(messageToParse);
				if(!  (Main.storedFiles.get("chunkNo").equals(nChunk.toString()) && Main.storedFiles.get("filename").equals(file)))
				{
		
					ok = false;
					break;
				}
				repeating = !(parsed.confirmStored());

				

				dataBuffer = new byte[256];
			} catch (IOException ex) {
				System.out.println("Error in Confirmation");
				ex.printStackTrace();

				repeating = false;
			}

		}while(repeating);

		if(ok)
		{
			Integer counter = Integer.parseInt(Main.storedFiles.get("receivedCount"));
			counter++;
			Main.storedFiles.replace("receivedCount", counter.toString());
		}
	}

}

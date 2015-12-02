package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class RestoreChunk {
	
	private InetAddress addr;
	private int port;
	private byte[] buffer = new byte[64000];
	private String fileName;
	private Integer chunkNo;


	public RestoreChunk(int Port, InetAddress add, String filen, int chunk) {

		addr = add;
		this.port = Port;
		this.fileName = filen;
		this.chunkNo = chunk;
	}
	
	public byte[] assignChunkToFile(){
			Parser parsed;

			boolean rep = true;
			do
			{
				buffer = new byte[64000];
				try (MulticastSocket socket = new MulticastSocket(port)) {
					socket.joinGroup(addr);
					socket.setLoopbackMode(true);
					DatagramPacket msg = new DatagramPacket(buffer, buffer.length);
					socket.receive(msg);

					String message = new String(buffer);

					parsed = new Parser(message);
					
					if(parsed.validChunk(fileName,chunkNo))
					{
						buffer = parsed.readChunk().getBytes();
						rep = false;
					}
					
				} catch (IOException ex) {
					System.out.println("Restore Chunk failed");
					ex.printStackTrace();

					rep = false;
				}

			}while(rep);
		
		
		return buffer;
		
	}

}


package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Random;

public class ThreadListener extends Thread {


	private InetAddress addr;
	private int port;
	private byte[] dataBuffer = new byte[256];
	private FileManager confirmSocket;

	public ThreadListener(int port,String adr, FileManager confirmSocket) throws UnknownHostException {

		addr = InetAddress.getByName(adr);
		this.port = port;
		this.confirmSocket = confirmSocket;

	}

	public void run()
	{		
		Parser parsed;
		
		try (MulticastSocket socketMC = new MulticastSocket(port)) {

			socketMC.joinGroup(addr);
			socketMC.setLoopbackMode(true);
			
			System.out.println(addr + " "+ port);

			while (true) {

				DatagramPacket msgPacket = new DatagramPacket(dataBuffer, dataBuffer.length);
				socketMC.receive(msgPacket);

				String messageToParse = new String(dataBuffer, 0, dataBuffer.length);
				
				parsed = new Parser(messageToParse);
				
				String confirmMessage = parsed.assignChunk();
				
				Random r = new Random();
				sleep(r.nextInt(400));
				
				ConfirmMessage reply = new ConfirmMessage(confirmMessage, confirmSocket);
				reply.start();				
				
				
				dataBuffer = new byte[256];
			}
		} catch (IOException ex) {
			System.out.println("Error in Listener:");
			ex.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
}






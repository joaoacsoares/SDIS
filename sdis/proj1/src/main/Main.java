package main;

import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;


public class Main {

	static public  byte[] flagValue = {0xD, 0xA};
	static public String crlf = new String(flagValue);
	static public ConcurrentHashMap<String, String> storedFiles;
	
	
	public static void main(String Args[]) throws UnknownHostException, InterruptedException {

		if(Args.length != 6)
			System.out.println("Function args:  <MCAddr> <MCPort> <MDBAddr> <MDBPort> <MDRAddr> <MDRPort>");
		else
		{
			storedFiles = new ConcurrentHashMap<>();
			storedFiles.put("filename", "");
			storedFiles.put("chunkNo", "0");
			storedFiles.put("receivedCount","0");
			
			String MCaddress = Args[0];
			int MCPort = Integer.parseInt(Args[1]);
			String MDBaddress = Args[2];
			int MDBPort = Integer.parseInt(Args[3]);
			String MDRaddress = Args[4];
			int MDRPort = Integer.parseInt(Args[5]);

			FileManager fm = new FileManager(MCPort, MCaddress,MDBPort,MDBaddress,MDRPort,MDRaddress);

			ThreadListener mcThread = new ThreadListener(MCPort,MCaddress, fm);
			ThreadListener mdbThread = new ThreadListener(MDBPort,MDBaddress, fm);
			//ThreadListener mdrThread = new ThreadListener(MDRPort,MDRaddress, client);
			
			mcThread.start();
			mdbThread.start();
			//mdrThread.start();
			
			fm.mainMenu();
			
		}

		

	}

}


package main;

public class ConfirmMessage extends Thread {

	private String checking;
	private FileManager confirmSocket;

	public ConfirmMessage(String checking, FileManager confirmSocket) {
		this.confirmSocket = confirmSocket;
		this.checking = checking;
	}

	public void run() {
		if (!checking.equals(""))
			confirmSocket.sendConfirmation(checking);
		
	}

}

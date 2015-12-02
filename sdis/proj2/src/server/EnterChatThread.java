package server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class EnterChatThread extends Thread{

	HttpExchange t;
	Map<String, String> requestParams;
	Headers header;
	Charset CHARSET;
	ArrayList<Pair<String,String>> loggedClients;

	EnterChatThread(HttpExchange t, Map<String, String> requestParams, 
			Headers header, Charset CHARSET, ArrayList<Pair<String,String>> loggedClients)
	{
		this.t = t;
		this.requestParams = requestParams;
		this.header = header;
		this.CHARSET = CHARSET;
		this.loggedClients = loggedClients;
	}

	public void run()
	{
		try {
			String ips = "";
			for (int i= 0; i < loggedClients.size(); i++ )
			{
				if (i != loggedClients.size()-1)
					ips = ips + "'" + loggedClients.get(i).getX() + "," + loggedClients.get(i).getY() + "'" + ",";
				else
					ips = ips + "'" + loggedClients.get(i).getX() + "," + loggedClients.get(i).getY() + "'" ;

			}
			String responseBody = "{ \"ips\": [" + ips + "]}";
			System.out.println("{ \"ips\": [" + ips + "]}");

			header.set("Content-Type", "application/json");
			byte[] rawResponseBody = responseBody.getBytes(CHARSET);
			t.sendResponseHeaders(200, rawResponseBody.length);
			OutputStream os = t.getResponseBody();
			os.write(rawResponseBody);
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

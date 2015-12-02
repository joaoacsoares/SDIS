package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicNameValuePair;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class RemoveClientThread extends Thread{

	HttpExchange t;
	Headers header;
	Charset CHARSET;
	ArrayList<Pair<String,String>> loggedClients;
	CloseableHttpAsyncClient futureClient;

	RemoveClientThread(HttpExchange t, CloseableHttpAsyncClient futureClient, Headers header, 
			Charset CHARSET, ArrayList<Pair<String,String>> loggedClients)
	{
		this.t = t;
		this.futureClient = futureClient;
		this.header = header;
		this.CHARSET = CHARSET;
		this.loggedClients = loggedClients;
	}

	public void run()
	{
		try {
			InputStream in = t.getRequestBody();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder out = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				out.append(line);
			}

			String request = out.toString();
			request = decodeUrlComponent(request);
			System.out.println(request);
			String[] ip = request.split("=");

			String responseBody = "{ \"resposta\":'yes'}";
			System.out.println("Disconnected " + ip[1]);

			header.set("Content-Type", "application/json");
			byte[] rawResponseBody = responseBody.getBytes(CHARSET);
			t.sendResponseHeaders(200, rawResponseBody.length);
			OutputStream os = t.getResponseBody();
			os.write(rawResponseBody);
			os.close();

			// mandar remove a todos os clientes
			for (int i = 0; i < loggedClients.size(); i++)
			{
				String url= "http://" + loggedClients.get(i).getY() +":8080/sdisdocs/chat/remove";
				HttpPost method=new HttpPost(url);

				try {
					List <NameValuePair> nvps = new ArrayList <NameValuePair>();
					nvps.add(new BasicNameValuePair("remove", ip[1]));
					method.setEntity(new UrlEncodedFormEntity(nvps));
					Future<HttpResponse> future = futureClient.execute(method, null);
					System.out.println("Remove done " + loggedClients.get(i).getY() + " " + ip[1]);
				}
				catch (Exception e) {
					System.out.println("Post to " + url + " failed with: "+ e );
				}
			}
			for (int j = 0; j < loggedClients.size(); j++)
			{
				if (loggedClients.get(j).getY().equals(ip[1]))
					loggedClients.remove(j);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	String decodeUrlComponent(final String urlComponent) {
		try {
			return URLDecoder.decode(urlComponent, CHARSET.name());
		} catch (final UnsupportedEncodingException ex) {
			throw new InternalError(ex);
		}
	}
}

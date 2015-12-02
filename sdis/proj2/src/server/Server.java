package server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Server {

	private static final Charset CHARSET = StandardCharsets.UTF_8;

	static ArrayList<Pair<String,String>> loggedClients;
	static ArrayList<Pair<String,String>> clientsInfo;
	static CloseableHttpClient client;
	static CloseableHttpAsyncClient futureClient;

	public static void main(String[] args) throws Exception {

		initServer();

		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		server.createContext("/sdisdocs", new MyHandler());
		server.setExecutor(null); // creates a default executor
		server.start();


		futureClient = HttpAsyncClients.createDefault();
		futureClient.start();
	}

	private static void initServer() throws IOException
	{
		client = HttpClients.createDefault();
		clientsInfo = new ArrayList<Pair<String,String>>();
		loggedClients = new ArrayList<Pair<String,String>>();


		String dirPath = "users";
		File dir = new File(dirPath);
		if(!dir.isDirectory())
			dir.mkdirs();

		File file = new File("users\\registredUsers.txt");

		if(file.exists() && !file.isDirectory())
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				String[] tmp = line.split(",");
				Pair<String,String> pair = new Pair<String, String>(tmp[0],tmp[1]);
				clientsInfo.add(pair);
			}
			br.close();
		}
	}

	static class MyHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			Headers header = t.getResponseHeaders();
			System.out.println(t.getRequestURI());
			String[] path = t.getRequestURI().getPath().split("/");
			switch(path[2])
			{
			case "get":
			{
				Map<String, String> requestParams = getRequestParameters(t.getRequestURI());
				if(path[3].equals("file")) {
					new FileRequestThread(t, requestParams, header, CHARSET).start();
				}
				else if (path[3].equals("changes"))
				{
					new GetChangesThread(t, requestParams, header, CHARSET).start();
				}
				else if(path[3].equals("enterChat"))
				{
					new EnterChatThread(t, requestParams, header, CHARSET, loggedClients).start();
				}
				break;
			}
			case "post":
			{
				if(path[3].equals("change"))
				{
					System.out.println("aqui");
					new PostChangesThread(t, header, CHARSET).start();
				}
				else if(path[3].equals("doRegister"))
				{
					String[] username = new String[2];
					String[] password = new String[2];
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
					String[] tmp = request.split("&");
					String responseBody;

					if(tmp.length == 2)
					{
						username = tmp[0].split("=");
						password = tmp[1].split("=");
					}
					else
						System.out.println("Invalid number of parameters in register request");
					boolean isOK = true;
					for (int i=0; i < clientsInfo.size(); i++)
						if (username[1].equals(clientsInfo.get(i).getX()))
						{
							isOK = false;
							break;
						}

					header.set("Content-Type", "application/json");
					byte[] rawResponseBody;
					if(isOK)
					{
						responseBody = "{ \"register\": 'yes'}";
						rawResponseBody = responseBody.getBytes(CHARSET);
						t.sendResponseHeaders(200, rawResponseBody.length);
					}
					else
					{
						responseBody = "{ \"register\": 'no'}";
						rawResponseBody = responseBody.getBytes(CHARSET);
						t.sendResponseHeaders(400, rawResponseBody.length);
					}
					OutputStream os = t.getResponseBody();
					os.write(rawResponseBody);
					os.close();

					if(isOK)
					{
						MessageDigest digest = null;
						try {
							digest = MessageDigest.getInstance("SHA-256");
						} catch (NoSuchAlgorithmException e) {
							e.printStackTrace();
						}
						byte[] hash = digest.digest(password[1].getBytes("UTF-8"));

						StringBuilder hexString = new StringBuilder();

						for (int i = 0; i < hash.length; i++) {
							String hex = Integer.toHexString(0xff & hash[i]);
							if(hex.length() == 1) hexString.append('0');
							hexString.append(hex);
						}

						String encryptedPassword = hexString.toString();

						Pair<String,String> pair = new Pair<String, String>(username[1],encryptedPassword);
						clientsInfo.add(pair);
						File file = new File("users\\registredUsers.txt");

						FileWriter fw = new FileWriter(file, true);
						fw.write(pair.getX() + "," + pair.getY() + "\n");
						fw.close();
					}
				}
				else if(path[3].equals("login"))
				{
					String[] username = new String[2];
					String[] password = new String[2];
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
					String[] tmp = request.split("&");
					String responseBody;

					if(tmp.length == 2)
					{
						username = tmp[0].split("=");
						password = tmp[1].split("=");
					}
					else
						System.out.println("Invalid number of parameters in login request");
					boolean isOK = false;
					for (int i=0; i < clientsInfo.size(); i++)
						if (username[1].equals(clientsInfo.get(i).getX()))
						{
							MessageDigest digest = null;
							try {
								digest = MessageDigest.getInstance("SHA-256");
							} catch (NoSuchAlgorithmException e) {
								e.printStackTrace();
							}
							byte[] hash = digest.digest(password[1].getBytes("UTF-8"));

							StringBuilder hexString = new StringBuilder();

							for (int j = 0; j < hash.length; j++) {
								String hex = Integer.toHexString(0xff & hash[j]);
								if(hex.length() == 1) hexString.append('0');
								hexString.append(hex);
							}

							String encryptedPassword = hexString.toString();

							if(encryptedPassword.equals(clientsInfo.get(i).getY()))
								isOK = true;
							break;
						}

					header.set("Content-Type", "application/json");
					byte[] rawResponseBody;
					if(isOK)
					{
						responseBody = "{ \"login\": 'yes'}";
						rawResponseBody = responseBody.getBytes(CHARSET);
						t.sendResponseHeaders(200, rawResponseBody.length);

						String ip = t.getRemoteAddress().getHostString();
						System.out.println("Enviar para os outros");
						addClient(username[1], ip);
						System.out.println("Enviei");
						Pair<String, String> p = new Pair<String,String>(username[1], ip);
						loggedClients.add(p);
					}
					else
					{
						responseBody = "{ \"login\": 'no'}";
						rawResponseBody = responseBody.getBytes(CHARSET);
						t.sendResponseHeaders(400, rawResponseBody.length);
					}
					OutputStream os = t.getResponseBody();
					os.write(rawResponseBody);
					os.close();
				}
				else if(path[3].equals("remove"))
				{
					new RemoveClientThread(t, futureClient, header, CHARSET, loggedClients).start();
				}
				break;
			}
			}
		}
	}


	static Map<String, String> getRequestParameters(URI requestUri) {
		final Map<String, String> requestParameters = new HashMap<String, String>();
		final String requestQuery = requestUri.getRawQuery();
		if (requestQuery != null)
		{
			String[] rawRequestParameters = requestQuery.split("&");
			for (String rawRequestParameter : rawRequestParameters)
			{
				String[] requestParameter = rawRequestParameter.split("=", 2);
				if(requestParameter.length == 2)
					requestParameters.put(decodeUrlComponent(requestParameter[0]), decodeUrlComponent(requestParameter[1]));
			}
		}
		return requestParameters;
	}

	static String decodeUrlComponent(final String urlComponent) {
		try {
			return URLDecoder.decode(urlComponent, CHARSET.name());
		} catch (final UnsupportedEncodingException ex) {
			throw new InternalError(ex);
		}
	}


	/*
	 * Pedido post ao cliente para informar de novo utilizador no chat,
	 * 		nao espera resposta;
	 *
	 */
	synchronized private static void addClient(String username, String ip) throws IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		System.out.println("size: "+ loggedClients.size());
		for (int i= 0; i< loggedClients.size(); i++)
		{
			System.out.println("for" + i);
			String url = "http://" + loggedClients.get(i).getY() + ":8080/sdisdocs/chat/add";
			HttpPost method=new HttpPost(url);

			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair(username, ip));
			method.setEntity(new UrlEncodedFormEntity(nvps));
			client.execute(method);
		}
	}

}
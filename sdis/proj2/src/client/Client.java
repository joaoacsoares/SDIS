package client;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import server.Pair;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.*;

public class Client {
	static Chat chatroom;
	EditorGUI editor;
	static String chatname;
	private static final Charset CHARSET = StandardCharsets.UTF_8;
	private static String lastChange;
	
	static final Scanner inputReader = new Scanner(System.in);

    static CloseableHttpClient client;
    static CloseableHttpAsyncClient clientFuture;
    static ArrayList<Pair<String,String>> chatIp;

    static boolean sendClient;
    static boolean sendServer;

    static String serverIP;
    static String serverPort;

    public Client(EditorGUI editor, Chat chat) {
    }

	public static void main(String[] args) throws IOException {

		HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/sdisdocs/chat", new MyHandler());
		server.setExecutor(null); // creates a default executor
		server.start();

		client = HttpClients.createDefault();
        clientFuture = HttpAsyncClients.createDefault();
        clientFuture.start();
		chatIp = new ArrayList<Pair<String,String>>();
		serverIP = "192.168.0.101";
	    serverPort = "8000";


		System.out.println("Welcome to NotGoogleDocs!");
		boolean exit = false;
		do
		{
			System.out.println("Choose one option:");
			System.out.println("1 - Register");
			System.out.println("2 - Login");
			System.out.println("3 - Exit");
			if(inputReader.hasNextLine())
			{
				try{
					int input = inputReader.nextInt();
					inputReader.nextLine();
					switch(input)
					{
					case 1:
						doRegister();
						break;
					case 2:
						login();
						String url = "http://"+serverIP+":"+serverPort+"/sdisdocs";
						File file = requestFile(url,"teste.txt");
						EditorGUI editor = new EditorGUI(file);
						chatroom = new Chat(chatname,file.getName());
						chatroom.sendMessage.addActionListener(new sendMessageButtonListener());
						enterChat();
						
						lastChange = "start";
				        Vector<String> serverSideChanges = getChanges(url,file.getName(),lastChange);
				        if(serverSideChanges.size() != 0) {
				            for(String c : serverSideChanges)
				            {
				                editor.applyChange(c);
				            }
				        }
				        serverSideChanges.clear();

				        while(editor.getTextArea().isEnabled()) //application life cycle
				        {
				            serverSideChanges.clear();
				            serverSideChanges = getChanges(url,file.getName(),lastChange);
				            if(serverSideChanges.size() != 0) {
				                for(String c : serverSideChanges)
				                {
				                    editor.applyChange(c);
				                }
				            }

				            if(editor.getLoggedChanges().size() !=0) {
				                for(int i =0; i < editor.getLoggedChanges().size() ; i++ ) {
				                    System.out.println(editor.getLoggedChanges().elementAt(i));
				                    postChange(url, file.getName(), editor.getLoggedChanges().elementAt(i));
				                }
				                editor.clearLog();
				            }

				            try {
				                Thread.sleep(500);
				            } catch(InterruptedException ex) {
				                Thread.currentThread().interrupt();
				            }
				        }
						
						break;
					case 3:
						exit=true;
						break;
					}
				}catch(NoSuchElementException exception)
				{
					System.out.println("The input was not correct please input a number from 1 to 3");
				}
			}
		}while(!exit);
	}

	private static Vector<String> getChanges(String serverIP, String filename, String date) throws IOException {
		Vector<String> serverSideChanges = new Vector<>();
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String aux = date.replace(" ", "+");
		serverIP += "/get/changes/" + "?filename=" + filename + "&timestamp="+ aux;
		HttpGet myGet = new HttpGet(serverIP);
		CloseableHttpResponse response = httpclient.execute(myGet);
		try {
			HttpEntity entity = response.getEntity();
			String tmp = parseResponse(response);
			JSONObject obj = new JSONObject(tmp);
			JSONArray arr = obj.getJSONArray("changes");
			for(int i=0; i < arr.length() ;i++)
			{
				serverSideChanges.add(arr.getString(i));
			}
			if(!serverSideChanges.isEmpty()) {
				String[] elements = serverSideChanges.elementAt(serverSideChanges.size() - 1).split("/");
				lastChange = elements[elements.length - 1];
			}
			EntityUtils.consume(entity);
		} finally {
			response.close();
		}
		return serverSideChanges;
	}


	private static File requestFile(String serverIP, String filename) throws IOException
	{
		CloseableHttpClient httpclient = HttpClients.createDefault();
		serverIP += "/get/file/" + "?filename=" + filename;
		HttpGet myGet = new HttpGet(serverIP);
		File file;
		CloseableHttpResponse response = httpclient.execute(myGet);
		try {
			HttpEntity entity = response.getEntity();
			String tmp = parseResponse(response);
			JSONObject obj = new JSONObject(tmp);
			JSONArray arr = obj.getJSONArray("file");
			file = new File(arr.getString(0));
			FileOutputStream fop = new FileOutputStream(file);
			if (!file.exists()) {
				file.createNewFile();
			}
			byte[] contentInBytes = arr.getString(1).getBytes();
			fop.write(contentInBytes);
			fop.flush();
			fop.close();

			EntityUtils.consume(entity);
		} finally {
			response.close();
		}
		return file;
	}

	private static void postChange(String serverIP,String filename, String change) throws IOException
	{
		CloseableHttpClient httpclient = HttpClients.createDefault();
		serverIP += "/post/change";
		HttpPost httpPost = new HttpPost(serverIP);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair(filename, "filename"));
		nvps.add(new BasicNameValuePair(change, "change"));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = httpclient.execute(httpPost);
		try {
			HttpEntity entity = response.getEntity();
			String tmp = parseResponse(response);
			JSONObject obj = new JSONObject(tmp);
			JSONArray arr = obj.getJSONArray("response");
			lastChange = arr.getString(1);

			EntityUtils.consume(entity);
		} finally {
			response.close();
		}
	}




	private static String parseResponse(CloseableHttpResponse response) throws UnsupportedOperationException, IOException
	{
		String content = "";
		BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			content += line;
		}

		return content;
	}

	/*
	 * Pedido post ao servidor para criar um novo utilizador com username e password,
	 * 		espera resposta "yes" ou "no"
	 * Se "yes" register concluido
	 * Se "no" criacao de novo username
	 *
	 */
	static synchronized private void doRegister()
	{
		String url= "http://"+serverIP+":"+serverPort+"/sdisdocs/post/doRegister";
		String username;
		String password;
		String repeatpassword;

		try {
			String string="no";
			HttpPost method=new HttpPost(url);
			while (string.equals("no"))
			{
				System.out.println("Username:");
				username = inputReader.nextLine();
				System.out.println("Password:");
				password = inputReader.nextLine();
				System.out.println("Repeat Password:");
				repeatpassword = inputReader.nextLine();
				while(!password.equals(repeatpassword))
				{
					System.out.println("Password missmatch");
					System.out.println("Password:");
					password = inputReader.nextLine();
					System.out.println("Repeat Password:");
					repeatpassword = inputReader.nextLine();
				}

				List <NameValuePair> nvps = new ArrayList <NameValuePair>();
				nvps.add(new BasicNameValuePair("username", username));
				nvps.add(new BasicNameValuePair("password", password));
				method.setEntity(new UrlEncodedFormEntity(nvps));

				//Resposta do servidor a confirmar se utilizador ja existe registado
				HttpResponse r = client.execute(method);
				BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));
				String line = "";
				String content = "";
				while ((line = rd.readLine()) != null)
				{
					content += line;
				}
				JSONObject o = new JSONObject(content);
				string = o.getString("register");
				if(string.equals("no")) {
					System.out.println("Username in use, please select another");
				}
				else
					System.out.println("Register successful!!\n\n");
			}

		}
		catch (Exception e) {
			System.out.println("Post to " + url + " failed with: "+ e );
		}
	}

	/*
	 * Pedido post ao servidor para criar um novo utilizador com username e password,
	 * 		espera resposta "yes" ou "no"
	 * Se "yes" register concluido
	 * Se "no" criacao de novo username
	 *
	 */
	static synchronized private void login()
	{
		String url= "http://"+serverIP+":"+serverPort+"/sdisdocs/post/login";
		String username;
		String password;
		Scanner reader = new Scanner(System.in);

		try {
			String string="no";
			HttpPost method=new HttpPost(url);
			while (string.equals("no"))
			{
				System.out.println("Username:");
				username = reader.nextLine();
				System.out.println("Password:");
				password = reader.nextLine();

				List <NameValuePair> nvps = new ArrayList <NameValuePair>();
				nvps.add(new BasicNameValuePair("username", username));
				nvps.add(new BasicNameValuePair("password", password));
				method.setEntity(new UrlEncodedFormEntity(nvps));

				//Resposta do servidor a confirmar se utilizador ja existe registado
				HttpResponse r = client.execute(method);
				BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));
				String line = "";
				String content = "";
				while ((line = rd.readLine()) != null)
				{
					content += line;
				}
				JSONObject o = new JSONObject(content);
				string = o.getString("login");
				if(string.equals("no")) {
					System.out.println("Invalid username or password");
				}
				else
				{
					System.out.println("Login successful");
					chatname = username;
				}
			}
		}
		catch (Exception e) {
			System.out.println("Post to " + url + " failed with: "+ e );
		}
		reader.close();
	}

	/*
	 * Pedido get ao servidor para receber os ips todos de quem esta presente no chat,
	 * 		espera resposta com array de strings com os ips
	 *
	 */
	static synchronized private void enterChat()
	{
		String url= "http://"+serverIP+":"+serverPort+"/sdisdocs/get/enterChat";
		try
		{
			HttpGet p = new HttpGet(url);
			HttpResponse r = client.execute(p);

			BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));
			String line = "";
			String content = "";
			while ((line = rd.readLine()) != null)
			{
				content += line;
			}
			JSONObject o = new JSONObject(content);
			JSONArray array = o.getJSONArray("ips");
			for (int i = 0; i < array.length(); i++)
			{
				String[] tmp = array.getString(i).split(",");
				Pair<String,String> pair = new Pair<String,String>(tmp[0], tmp[1]);
				chatIp.add(pair);
				System.out.println("Add ip" + array.getString(i));
			}
		}
		catch(ParseException | IOException e) {
			System.out.println(e);
		}
	}

	/*
     * Pedidos do tipo post para todos os clientes com a nova mensagem de chat,
     * 		nao espera resposta
     *
     */
    public static void sendMessage(String message) throws Exception
    {
        sendClient = false;
        sendServer = false;
        HttpPost[] requests = null;
        final ArrayList<Pair<String, String>> copychatIp = new ArrayList<Pair<String,String>>(chatIp);
        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair(chatname, message));

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(3000)
                .setConnectTimeout(3000).build();
        final CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        try {
            httpclient.start();
            requests = new HttpPost[chatIp.size()];

            // Ciclo para criar array de requests com os ips dados pelo servidor
            for (int i = 0; i < chatIp.size(); i++)
            {
                String url = "http://" + chatIp.get(i).getY() + ":8080/sdisdocs/chat/message";
                requests[i] = new HttpPost(url);
                requests[i].setEntity(new UrlEncodedFormEntity(nvps));
            }

            System.out.println(chatIp.size());

            final CountDownLatch latch = new CountDownLatch(requests.length);
            int i = -1;

            for (final HttpPost request: requests)
            {

                i++;
                final int ip = i;
                System.out.println("request");
                if(request == null)
                    System.out.println("i'm null");
                httpclient.execute(request, new FutureCallback<HttpResponse>()
                {
                    @Override
                    public void completed(final HttpResponse response)
                    {
                        sendClient = true;
                        latch.countDown();
                        System.out.println("Completed:" + request.getRequestLine() + "->" + response.getStatusLine());
                    }

                    @Override
                    public void failed(final Exception ex)
                    {
                        int j = 0;
                        boolean done = false;
                        while(j < 3 && !done)
                        {
                            Future<HttpResponse> future = httpclient.execute(request, null);
                            try
                            {
                                future.get(500, TimeUnit.MILLISECONDS);
                                done = true;
                            }
                            catch (InterruptedException | ExecutionException | TimeoutException e)
                            {
                                System.out.println(request.getRequestLine() + " Excepcao no reenvio "+ ex);
                                j++;

                            }
                        }
                        try
                        {
                            String url= "http://"+serverIP+":"+serverPort+"/sdisdocs/post/remove/";
                            HttpPost method=new HttpPost(url);

                            List <NameValuePair> nvps = new ArrayList <NameValuePair>();

                            nvps.add(new BasicNameValuePair("ip", copychatIp.get(ip).getY()));
                            method.setEntity(new UrlEncodedFormEntity(nvps));
                            Future<HttpResponse> response = clientFuture.execute(method,null);
                            sendServer = true;

                        }
                        catch (IOException | UnsupportedOperationException e1)
                        {
                            System.out.println("Excepcao no envio para o servidor "+ e1);
                        }
                        System.out.println(request.getRequestLine() + " Excepcao failed "+ ex);
                        latch.countDown();
                    }

                    @Override
                    public void cancelled()
                    {
                        latch.countDown();
                        System.out.println(request.getRequestLine() + " cancelled");
                    }

                });
            }
            latch.await();
            System.out.println("All sent");
        } finally {
            httpclient.close();
        }
        System.out.println("Done");
        if (!sendClient && !sendServer)
            chatroom.receivedMessage("disconnected", "System");
    }

    /*
     * Remover um dado ip do array chatIp
     *
     */
    static void removeIp(String ip)
    {
        int i;
        boolean removed = false;
        for (i = 0; i< chatIp.size(); i++)
            if (chatIp.get(i).getY().equals(ip))
            {
                removed = true;
                break;
            }

        if (removed)
        {
            chatroom.receivedMessage(chatIp.get(i).getX() + " disconnected", "System");
            chatIp.remove(i);
        }
        else
            System.out.println("Ip nao existente na lista de ips");
    }



	/*
	 * Server part of Client
	 */


	/*
	 * Handler de pedidos post
	 * 		add(ip) : Recebe do servidor para adicionar um novo ip do chatIp
	 *		remove(ip) : Recebe do servidor para remover um dado ip do chatIp
	 *		message(string) : Recebe de um cliente para processar a mensagem e representa-la na gui
	 *
	 */
	static class MyHandler implements HttpHandler
	{
		public void handle(HttpExchange t) throws IOException {
			
			String[] path = t.getRequestURI().getPath().split("/");

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

			String[] tmp = request.split("=");
			switch(path[3])
			{
			case "add":
				chatIp.add(new Pair<String, String>(tmp[0], tmp[1]));
				break;
			case "remove":
				removeIp(tmp[1]);
				break;
			case "message":
				System.out.println("message receive");
				chatroom.receivedMessage(tmp[1],tmp[0]);
				break;
			}

			Headers header = t.getResponseHeaders();
			header.set("Content-Type", "application/json");
			String responseBody = "{\"response\": \"OK\"}";
			byte[] rawResponseBody = responseBody.getBytes(CHARSET);
			t.sendResponseHeaders(200, rawResponseBody.length);
			OutputStream os = t.getResponseBody();
			os.write(rawResponseBody);
			os.close();
		}
	}

	static String decodeUrlComponent(final String urlComponent) {
		try {
			return URLDecoder.decode(urlComponent, CHARSET.name());
		} catch (final UnsupportedEncodingException ex) {
			throw new InternalError(ex);
		}
	}

    static class sendMessageButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (chatroom.messageBox.getText().length() < 1) {
                // do nothing
            } else if (chatroom.messageBox.getText().equals(".clear")) {
                chatroom.chatBox.setText("Cleared all messages\n");
                chatroom.messageBox.setText("");
            } else {
                try {
                    sendMessage(chatroom.messageBox.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                chatroom.messageBox.setText("");
            }
            chatroom.messageBox.requestFocusInWindow();
        }
    }
}




package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.sql.Timestamp;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class PostChangesThread extends Thread{

	HttpExchange t;
	Headers header;
	Charset CHARSET;

	PostChangesThread(HttpExchange t, Headers header, Charset CHARSET)
	{
		this.t = t;
		this.header = header;
		this.CHARSET = CHARSET;
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
			String[] tmp = request.split("&");
			String responseBody;

			if (tmp.length == 2) {
				String[] name = tmp[0].split("=");
				String[] change = tmp[1].split("=");
				java.util.Date date = new java.util.Date();
				String tsp = new Timestamp(date.getTime()).toString();
				String logChange = change[0] + "/" + tsp + "\n";
				File logs = new File(name[0] + "log");
				FileWriter fw = new FileWriter(logs.getAbsoluteFile(), true);
				fw.write(logChange);
				fw.close();

				responseBody = "{\"response\": [\"OK\", \"" + tsp + "\"]}";
			} else
				responseBody = "{\"response\": [\"NO\"]}";
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
	
	String decodeUrlComponent(final String urlComponent) {
		try {
			return URLDecoder.decode(urlComponent, CHARSET.name());
		} catch (final UnsupportedEncodingException ex) {
			throw new InternalError(ex);
		}
	}
}

package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class FileRequestThread extends Thread{

	HttpExchange t;
	Map<String, String> requestParams;
	Headers header;
	Charset CHARSET;

	FileRequestThread(HttpExchange t, Map<String, String> requestParams, Headers header, Charset CHARSET)
	{
		this.t = t;
		this.requestParams = requestParams;
		this.header = header;
		this.CHARSET = CHARSET;
	}

	public void run()
	{
		String filename = requestParams.get("filename");
		File file = new File("src/"+filename);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			StringBuilder out = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				out.append(line);
			}
			reader.close();
			String fileContents = out.toString();
			String responseBody = "{ \"file\": ['"+filename+"' , '" + fileContents + "']}";
			System.out.println(fileContents);
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

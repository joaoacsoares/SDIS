package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class GetChangesThread extends Thread{

	HttpExchange t;
	Map<String, String> requestParams;
	Headers header;
	Charset CHARSET;

	GetChangesThread(HttpExchange t, Map<String, String> requestParams, Headers header, Charset CHARSET)
	{
		this.t = t;
		this.requestParams = requestParams;
		this.header = header;
		this.CHARSET = CHARSET;
	}

	public void run()
	{
		String filename = requestParams.get("filename");
		String tsp = requestParams.get("timestamp").replace('+', ' ');
		Timestamp requestDate = null;
		if(!tsp.equals("start"))
			requestDate = Timestamp.valueOf(tsp);

		String responseBody = "{\"changes\": [";

		String line;
		File f = new File(filename + "log");
		try {
			if(f.exists() && !f.isDirectory())
			{
				BufferedReader br;
				br = new BufferedReader(new FileReader(f));
				boolean addChanges = false;
				if(requestDate == null)
					addChanges = true;
				if(br.ready())
					while ((line = br.readLine()) != null) {
						String[] aux = line.split("/");
						Timestamp changeDate = Timestamp.valueOf(aux[aux.length-1]);
						if(!addChanges)
						{
							if(changeDate.after(requestDate))
								addChanges = true;
						}
						if(addChanges)
						{
							responseBody += "'";
							responseBody += line;
							responseBody += "'";
							responseBody += ",";
						}
					}
				br.close();
			}
			responseBody += "]}";

			System.out.println(responseBody);

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

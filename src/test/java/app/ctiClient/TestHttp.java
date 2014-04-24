package app.ctiClient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.Test;

import com.google.gson.Gson;

@ActiveProfiles("test")
public class TestHttp {


	public static final String POST_URL = "http://127.0.0.1:8123/action/HttpConnectorAction.dealRequest?method=batchQueryAgentState";

    @Test	
	public  void readContentFromPost() throws IOException {
		// Post请求的url，与get不同的是不需要带参数
		URL postUrl = new URL(POST_URL);
		int port = postUrl.getPort();
		System.out.println(port);
		// 打开连接
		long old = System.currentTimeMillis();
		HttpURLConnection connection = (HttpURLConnection) postUrl
				.openConnection();
		
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		connection.setUseCaches(false);
		connection.setInstanceFollowRedirects(true);
		connection.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		connection.connect();
		DataOutputStream out = new DataOutputStream(connection
				.getOutputStream());
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < 100; i++) {
			list.add(""+i);
		}
		Gson gson = new Gson();
		String content = "list="+gson.toJson(list);
		out.writeBytes(content);
		out.flush();
		out.close();// flush and close
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream(), "utf-8"));
		String line = "";
		System.out.println("=============================");
		System.out.println("Contents of post request");
		System.out.println("=============================");
		StringBuilder result = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			// line = new String(line.getBytes(), "utf-8");
			result.append(line);
 		}
		String[] results = result.toString().split("}");
		for (int i = 0; i < results.length; i++) {
			System.out.print(results[i]);
			if(i != results.length) System.out.println("}");
		}
		System.out.println("=============================");
		System.out.println("Contents of post request ends");
		System.out.println("=============================");
		reader.close();
		connection.disconnect();
		System.out.println("查询记录数："+(results.length-1));
		System.out.println("耗时时间："+ (System.currentTimeMillis()-old)+"ms");
	}
	
}

package me.Flibio.JobsLite.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import me.Flibio.JobsLite.Main;

import org.slf4j.Logger;

public class HttpUtils {
	
	public static String requestData(String url) {
		Logger logger = Main.access.logger;
		String USER_AGENT = "Mozilla/5.0";
		URL obj;
		try {
			obj = new URL(url);
		} catch (MalformedURLException e) {
			logger.error("Error requesting page via HTTP");
			logger.error(e.getMessage());
			return "";
		}
		HttpURLConnection con;
		try {
			con = (HttpURLConnection) obj.openConnection();
		} catch (IOException e) {
			logger.error("Error requesting page via HTTP");
			logger.error(e.getMessage());
			return "";
		}
 
		// optional default is GET
		try {
			con.setRequestMethod("GET");
		} catch (ProtocolException e) {
			logger.error("Error requesting page via HTTP");
			logger.error(e.getMessage());
			return "";
		}
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
 
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		} catch (IOException e) {
			logger.error("Error requesting page via HTTP");
			logger.error(e.getMessage());
			return "";
		}
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		try {
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
		} catch (IOException e) {
			logger.error("Error requesting page via HTTP");
			logger.error(e.getMessage());
			return "";
		}
		try {
			in.close();
		} catch (IOException e) {
			logger.error("Error requesting page via HTTP");
			logger.error(e.getMessage());
			return "";
		}
		con.disconnect();
		//return result
		return response.toString();
	}
}

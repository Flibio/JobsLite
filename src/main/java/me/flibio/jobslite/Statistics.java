/*
 * This file is part of JobsLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2016 Flibio
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.flibio.jobslite;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Statistics {

	private final String USER_AGENT = "JobsLite/1.0";
	private String ip;
	private boolean working = false;
	
	private String accessKey = "";
	
	public Statistics() {
		Optional<InetSocketAddress> addrOptional = JobsLite.access.game.getServer().getBoundAddress();
		JobsLite.access.game.getScheduler().createTaskBuilder().execute(r -> {
			String ipAddrResponse = "";
			try {
				ipAddrResponse = post("http://checkip.amazonaws.com","");
			} catch(Exception e) {
				JobsLite.access.logger.error(e.getMessage());
			}
			if(addrOptional.isPresent()&&!ipAddrResponse.isEmpty()) {
				InetSocketAddress addr = addrOptional.get();
				this.ip = ipAddrResponse+":"+addr.getPort();
				//Register the server as started
				this.working = true;
				try {
					String response = post("http://api.flibio.net/jobslite/serverStarted.php","ip="+ip);
					if(!response.contains("error")&&response.length()>10) {
						accessKey = response;
						JobsLite.access.game.getScheduler().createTaskBuilder().execute(t -> {
							try {
								post("http://api.flibio.net/jobslite/pinger.php","key="+accessKey+"&ip="+this.ip+"&pl="+
										JobsLite.access.game.getServer().getOnlinePlayers().size());
							} catch(Exception e) {
								JobsLite.access.logger.error(e.getMessage());
							}
						}).async().interval(1, TimeUnit.MINUTES).delay(1, TimeUnit.MINUTES).submit(JobsLite.access);
					}
				} catch(Exception e) {
					JobsLite.access.logger.error(e.getMessage());
				}
			} else {
				this.ip = "";
			}
		}).async().submit(JobsLite.access);
	}
	
	@Listener
	public void onServerStop(GameStoppingServerEvent event) {
		if(!working) return;
		try {
			post("http://api.flibio.net/jobslite/stopped.php","key="+accessKey+"&ip="+this.ip);
		} catch (Exception e) {
			JobsLite.access.logger.error(e.getMessage());
		}
	}
	
	private String post(String urlString, String urlParameters) throws Exception {
		// Send data
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(urlParameters);
		wr.flush();
		
		// Get the response
		BufferedReader rd = new BufferedReader(
		new InputStreamReader(conn.getInputStream()));
		
		String line;
		while ((line = rd.readLine()) != null) {
			return line;
		}
		wr.close();
		rd.close();
		return "";
	}
	
}

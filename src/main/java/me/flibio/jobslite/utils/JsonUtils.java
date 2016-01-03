/*
 * This file is part of JobsLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2016 Flibio
 * Copyright (c) contributors
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
package me.flibio.jobslite.utils;

import me.flibio.jobslite.gson.JsonGitHubData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtils {
	
	public JsonUtils() {
		
	}
	
	public String getVersion(String json) {
		Gson gson = new GsonBuilder().create();
    	JsonGitHubData data = gson.fromJson(json, JsonGitHubData.class);
    	if(data.getName()==null) return "";
    	String version = data.getName();
    	return version;
	}
	
	public String getUrl(String json) {
		Gson gson = new GsonBuilder().create();
    	JsonGitHubData data = gson.fromJson(json, JsonGitHubData.class);
    	if(data.getUrl()==null) return "";
    	String url = data.getUrl();
    	return url;
	}
	
	public boolean isPreRelease(String json) {
		Gson gson = new GsonBuilder().create();
    	JsonGitHubData data = gson.fromJson(json, JsonGitHubData.class);
		return data.isPreRelease();
	}
	
	public String getDownloadUrl(String jsonRelease) {
		return jsonRelease.split("browser_download_url")[1].split("}",2)[0].replaceAll("\"", "").replaceFirst(":", "").trim();
	}
	
	public Integer versionCompare(String str1, String str2) {
		String[] vals1 = str1.split("\\.");
		String[] vals2 = str2.split("\\.");
		int i = 0;
		
		while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
			i++;
		}
		
		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		} else {
			return Integer.signum(vals1.length - vals2.length);
		}
	}
}

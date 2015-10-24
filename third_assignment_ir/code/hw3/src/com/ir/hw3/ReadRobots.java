package com.ir.hw3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ReadRobots {

	List<String> disallowedLinks = new ArrayList<String>();

	public List<String> getRobotsList(String domain) {

		String host = domain;
		String robot = host + "/robots.txt";
		System.out.println(robot);
		String response;
		int flag = 0;
		String temp;

		try {
			URL obj = new URL(robot);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			//sleep();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			//con.setConnectTimeout(10000);

			BufferedReader inFromServer = new BufferedReader(
					new InputStreamReader(con.getInputStream()));

			while ((response = inFromServer.readLine()) != null) {
				if (response.contains("User-agent: *")) {
					flag = 1;
					continue;
				}

				if (flag == 1) {
					if (response.contains("Disallow")) {
						temp = host + response.substring(10);
						disallowedLinks.add(temp);
					}

				}

			}

		} catch (Exception e) {
			System.out.println("Error:-----");
			return disallowedLinks;
		} 
		
		return disallowedLinks;

	}

	public void sleep() // In milliseconds
	{
		long a = System.currentTimeMillis();
		// System.out.println(a);
		long b = System.currentTimeMillis();
		// System.out.println(b);
		while ((b - a) <= 1000) {
			try {
				Thread.sleep(1000 - (b - a));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				continue;
			}
		}
	}

}

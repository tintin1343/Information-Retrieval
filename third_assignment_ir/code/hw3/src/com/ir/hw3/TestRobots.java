package com.ir.hw3;

import java.util.List;

public class TestRobots {

	public static void main(String[] args) {
		ReadRobots rr = new ReadRobots();
		List<String> a = rr.getRobotsList("https://www.barackobama.com/");
		
		System.out.println(a.size());
		
		for(String l:a){
			System.out.println("Disallowed:: "+ l);
		}

	}

}

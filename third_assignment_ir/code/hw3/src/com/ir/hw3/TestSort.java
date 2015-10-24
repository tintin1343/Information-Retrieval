package com.ir.hw3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TestSort {

	public static void main(String[] args) {
		List<String> inLink1 = new ArrayList<String>();
		List<String> inLink2 = new ArrayList<String>();
		List<String> inLink3 = new ArrayList<String>();
		
		inLink1.add("");
		inLink1.add("");
		inLink1.add("");
		
		inLink2.add("");
		inLink2.add("");
		
		
		inLink3.add("");
		
		List<WebPageBean> list = new ArrayList<WebPageBean>();
		WebPageBean w1 = new WebPageBean();
		WebPageBean w2 = new WebPageBean();
		WebPageBean w3 = new WebPageBean();
		
		w1.setDocno("1");
		w1.setInLinks(inLink1);
		
		w2.setDocno("1");
		w2.setInLinks(inLink2);
		
		w3.setDocno("1");
		w3.setInLinks(inLink3);
		
		list.add(w2);
		list.add(w1);
		list.add(w3);
		
		
		
		Comparator comparator = new Comparator<WebPageBean>() {

			@Override
			public int compare(WebPageBean w1, WebPageBean w2) {
				// TODO Auto-generated method stub
				return  w1.getInLinks().size() > w2.getInLinks().size() ? -1
						: w1.getInLinks().size() == w2.getInLinks().size() ? 0
								: 1;
			}
		};
		
		list.sort(comparator);
		
		for(WebPageBean w:list){
			System.out.println(w.getInLinks().size());
		}
		
	}

}

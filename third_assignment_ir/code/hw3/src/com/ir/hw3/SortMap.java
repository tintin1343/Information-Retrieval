package com.ir.hw3;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

public class SortMap {

	public <K,V extends Comparable<? super V>> Map<String, List<String>> getSortedMap(
			Map<String, List<String>> outLinksList) {
		// System.out.println("Started Sorting..." + "@ " + new Date());

		List<Map.Entry<String, List<String>>> list = new LinkedList<Map.Entry<String, List<String>>>(
				outLinksList.entrySet());
		Collections.sort(list,
				new Comparator<Map.Entry<String, List<String>>>() {
					public int compare(Map.Entry<String, List<String>> o1,
							Map.Entry<String, List<String>> o2) {
						// return (o1.getValue()).compareTo(o2.getValue());
						return o1.getValue().size() > o2.getValue().size() ? -1
								: o1.getValue().size() == o2.getValue().size() ? 0
										: 1;

					}
				});

		Map<String, List<String>> result = new LinkedHashMap<String, List<String>>();
		for (Map.Entry<String, List<String>> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		// System.out.println("Stopped Sorting..." + "@ " + new Date());
		return result;
	}
	
	//Comparator anonymous class implementation
    public Comparator<? super WebPageBean> idComparator = new Comparator<WebPageBean>(){
         
        @Override
        public int compare(WebPageBean w1, WebPageBean w2) {
            return  w1.getInLinks().size() > w2.getInLinks().size() ? -1
					: w1.getInLinks().size() == w2.getInLinks().size() ? 0
							: 1;
        }
    };

}

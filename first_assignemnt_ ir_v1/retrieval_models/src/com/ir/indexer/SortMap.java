package com.ir.indexer;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SortMap {

	public <K, V extends Comparable<? super V>> Map<K, V> getSortedMap(
			Map<K, V> rankTerm) {
		System.out.println("Started Sorting..." + "@ " + new Date());

		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				rankTerm.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				// return (o1.getValue()).compareTo(o2.getValue());
				return Double.parseDouble(o1.getValue().toString()) > Double
						.parseDouble(o2.getValue().toString()) ? -1 : Double
						.parseDouble(o1.getValue().toString()) == Double
						.parseDouble(o2.getValue().toString()) ? 0 : 1;

			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		System.out.println("Stopped Sorting..." + "@ " + new Date());
		return result;
	}
}

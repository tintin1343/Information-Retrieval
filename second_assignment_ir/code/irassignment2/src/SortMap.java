
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SortMap {

	public <K, V extends Comparable<? super V>> Map<K, V> getSortedMap(
			Map<K, V> rankTerm) {
		// System.out.println("Started Sorting..." + "@ " + new Date());

		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				rankTerm.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				// return (o1.getValue()).compareTo(o2.getValue());
				return Integer.parseInt(o1.getValue().toString()) < Integer
						.parseInt(o2.getValue().toString()) ? -1 : Integer
						.parseInt(o1.getValue().toString()) == Integer
						.parseInt(o2.getValue().toString()) ? 0 : 1;

			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		// System.out.println("Stopped Sorting..." + "@ " + new Date());
		return result;
	}

	public <K, V extends Comparable<? super V>> Map<K, V> getSortedRankMap(
			Map<K, V> rankTerm) {
		// System.out.println("Started Sorting..." + "@ " + new Date());

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
		// System.out.println("Stopped Sorting..." + "@ " + new Date());
		return result;
	}

	public <K, V extends Comparable<? super V>> Map<Integer, Map<Integer, List<Integer>>> getSortedHashMap(
			Map<Integer, Map<Integer, List<Integer>>> rankTerm) {
		// System.out.println("Started Sorting..." + "@ " + new Date());

		List<Map.Entry<Integer, Map<Integer, List<Integer>>>> list = new LinkedList<Map.Entry<Integer, Map<Integer, List<Integer>>>>(
				rankTerm.entrySet());
		Collections
				.sort(list,
						new Comparator<Map.Entry<Integer, Map<Integer, List<Integer>>>>() {
							public int compare(
									Map.Entry<Integer, Map<Integer, List<Integer>>> o1,
									Map.Entry<Integer, Map<Integer, List<Integer>>> o2) {
								// return
								// (o1.getValue()).compareTo(o2.getValue());
								return Integer.parseInt(o1.getKey().toString()) < Integer
										.parseInt(o2.getKey().toString()) ? -1
										: Integer.parseInt(o1.getKey()
												.toString()) == Integer
												.parseInt(o2.getKey()
														.toString()) ? 0 : 1;

							}
						});

		Map<Integer, Map<Integer, List<Integer>>> result = new LinkedHashMap<Integer, Map<Integer, List<Integer>>>();
		for (Map.Entry<Integer, Map<Integer, List<Integer>>> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		// System.out.println("Stopped Sorting..." + "@ " + new Date());
		return result;
	}

	public <K, V extends Comparable<? super V>> Map<String, TokenCatalogBean> getSortedTermCat(
			Map<String, TokenCatalogBean> rankTerm) {
		// System.out.println("Started Sorting..." + "@ " + new Date());

		List<Map.Entry<String, TokenCatalogBean>> list = new LinkedList<Map.Entry<String, TokenCatalogBean>>(
				rankTerm.entrySet());
		Collections.sort(list,
				new Comparator<Map.Entry<String, TokenCatalogBean>>() {
					public int compare(Map.Entry<String, TokenCatalogBean> o1,
							Map.Entry<String, TokenCatalogBean> o2) {
						// return (o1.getValue()).compareTo(o2.getValue());
						return o1.getValue().getTermId() < o2.getValue()
								.getTermId() ? -1
								: o1.getValue().getTermId() == o2.getValue()
										.getTermId() ? 0 : 1;

					}
				});

		Map<String, TokenCatalogBean> result = new LinkedHashMap<String, TokenCatalogBean>();
		for (Map.Entry<String, TokenCatalogBean> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		// System.out.println("Stopped Sorting..." + "@ " + new Date());
		return result;
	}

	public <K, V extends Comparable<? super V>> Map<String, DocBean> getSortedDocCat(
			Map<String, DocBean> rankTerm) {
		// System.out.println("Started Sorting..." + "@ " + new Date());

		List<Map.Entry<String, DocBean>> list = new LinkedList<Map.Entry<String, DocBean>>(
				rankTerm.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, DocBean>>() {
			public int compare(Map.Entry<String, DocBean> o1,
					Map.Entry<String, DocBean> o2) {
				// return (o1.getValue()).compareTo(o2.getValue());
				return o1.getValue().getDocId() < o2.getValue().getDocId() ? -1
						: o1.getValue().getDocId() == o2.getValue().getDocId() ? 0
								: 1;

			}
		});

		Map<String, DocBean> result = new LinkedHashMap<String, DocBean>();
		for (Map.Entry<String, DocBean> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		// System.out.println("Stopped Sorting..." + "@ " + new Date());
		return result;
	}

}

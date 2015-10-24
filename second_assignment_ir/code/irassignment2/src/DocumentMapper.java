import java.util.HashMap;
import java.util.Map;


public class DocumentMapper {
	private Map<String, Integer> DocToId = new HashMap<String, Integer>();
	private Map<Integer,Long> IdtoLength= new HashMap<Integer, Long>();
	
	public void addValues(String key, int value, long docLength){
		DocToId.put(key, value);
		IdtoLength.put(value, docLength);
	}
	
	public int getId(String key){
		return DocToId.get(key);
	}
	
	public int getDocLength(int value){
		return DocToId.get(value);
	}
	
	public Map<String, Integer> getDocMap(){
		return DocToId;
	}

}

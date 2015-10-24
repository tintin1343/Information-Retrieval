

import java.util.HashMap;
import java.util.Map;

public class TokenMapper {
	
	private Map<String, Integer> TermToId = new HashMap<String, Integer>();
	private Map<Integer,String> IdtoTerm= new HashMap<Integer, String>();
	
	public void addValues(String key, int value){
		TermToId.put(key, value);
		IdtoTerm.put(value, key);
	}
	
	public int getId(String key){
		return TermToId.get(key);
	}
	
	public String getId(int key){
		return IdtoTerm.get(key);
	}
	
	public boolean containsKey(String key){
		
		return TermToId.containsKey(key);
		
	}
	
	public Map<String, Integer> getTermsMap(){
		return TermToId;
		
	}
	

}

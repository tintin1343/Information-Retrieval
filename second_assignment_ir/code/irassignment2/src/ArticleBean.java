

public class ArticleBean {
	
	private String DocNo;
	private String Text;
	private String DocLen;
	private int tf;
	private String word;
	
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getTf() {
		return tf;
	}
	public void setTf(int tf) {
		this.tf = tf;
	}
	public String getDocNo() {
		return DocNo;
	}
	public void setDocNo(String docNo) {
		DocNo = docNo;
	}
	public String getText() {
		return Text;
	}
	public void setText(String text) {
		Text = text;
	}
	public String getDocLen() {
		return DocLen;
	}
	public void setDocLen(String docLen) {
		DocLen = docLen;
	}

}

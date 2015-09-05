package xusheng.webquestion.qapair;

public class NounPhrase {
	public int qid;
	public String nounphrase;
	public int start;
	public int end;
	
	public NounPhrase(int qid, String nounphrase, int start, int end){
		this.qid=qid;
		this.nounphrase=nounphrase;
		this.start=start;
		this.end=end;
	}
}

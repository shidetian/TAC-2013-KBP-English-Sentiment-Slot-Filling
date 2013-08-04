public class Response implements Comparable<Response>{
	
	public String qId;
	public Sentiment sent;
	public String teamId;
	public String justId; //can be NIL
	//The entries below can be empty
	public String entity;
	public String fillerEntityOffsets;
	public String queryEntityOffsets;
	public String justificationOffsets;
	public double confidence = 1;
	
	Response(String qid, Sentiment s, String tid, String jid, String e, String feo, String qeo, String jo, double c){
		qId = qid;
		sent = s;
		teamId = tid;
		jid = justId;
		entity = e;
		fillerEntityOffsets = feo;
		queryEnittyOffsets = qeo;
		justificationOffsets = jo;
		confidence = c;
	}
	
	@Override
	public int compareTo(Response a) {
		return ((Double) confidence).compareTo(a.confidence);
	}
	
}
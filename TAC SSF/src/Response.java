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
		if (!(validateOffset(feo)&&validateOffset(qeo)&&validateOffset(jo))){
			System.err.println("One or more of the offsets are malformed");
		}
		qId = SolrInterface.getOriginalId(qid);
		sent = s;
		teamId = tid;
		justId = jid;
		entity = e;
		fillerEntityOffsets = feo;
		queryEntityOffsets = qeo;
		justificationOffsets = jo;
		confidence = c;
	}
	
	//Returns whether an offset is properly formated
	public static boolean validateOffset(String s){
		String[] parts = s.split(",");
		for (String part : parts){
			if (part.split("-").length!=2){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int compareTo(Response a) {
		return ((Double) confidence).compareTo(a.confidence);
	}
	
}
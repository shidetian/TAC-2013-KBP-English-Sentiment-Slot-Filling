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
	@Override
	public int compareTo(Response a) {
		return ((Double) confidence).compareTo(a.confidence);
	}
	
}

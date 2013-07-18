
public class Response implements Comparable<Response>{
	public static enum Sentiment{
		pos_toward, pos_from, neg_toward, neg_from;
		
		@Override
		public String toString(){
			switch(this){
			case pos_toward: return "pos-toward"; 
			case pos_from: return "pos-from"; 
			case neg_toward: return "neg-toward"; 
			case neg_from: return "neg-from";
			default: throw new IllegalArgumentException();
			}
		}
	}
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

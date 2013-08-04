public enum Sentiment{
	pos_towards, pos_from, neg_towards, neg_from;
	
	@Override
	public String toString(){
		switch(this){
		case pos_towards: return "pos-towards"; 
		case pos_from: return "pos-from"; 
		case neg_towards: return "neg-towards"; 
		case neg_from: return "neg-from";
		default: throw new IllegalArgumentException();
		}
	}
	
	public Sentiment fromString(String s){
		return Sentiment.valueOf(s.replaceAll("-", "_"));
	}
}
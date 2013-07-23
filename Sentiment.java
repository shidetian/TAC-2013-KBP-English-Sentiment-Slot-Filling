public enum Sentiment{
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
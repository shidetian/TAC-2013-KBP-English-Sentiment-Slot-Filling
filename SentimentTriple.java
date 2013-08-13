
public class SentimentTriple {
	private String holder;
	private String target;
	private double polarity;
	
	public SentimentTriple(double polarity, String holder, String target){
		this.polarity = polarity;
		this.holder = holder;
		this.target = target;
	}
	
	public double getPolarity(){
		return polarity;
	}
	
	public String getHolder(){
		return holder;
	}
	
	public String getTarget(){
		return target;
	}
}

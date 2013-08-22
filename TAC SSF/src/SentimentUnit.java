// each opinion extracted from any sentiment analysis system 
// is represented by this class

public class SentimentUnit{
	
	// we can add more public attributes, if needed
	public String docID;  // document ID
	public String sentenceSpan;  // span of the sentence containing this opinion
	public String opinSpan;  // span of the opinion expression
	public String holderSpan;  // span of the holder
	public String targetSpan;  // span of the target
	public String polarity;    // polarity, pos or neg
	public double confidenceScore;  // confidence score of the extracted opinion
	
	public SentimentUnit(String docID, String sentenceSpan, String opinSpan, String holderSpan, 
			String targetSpan, String polarity, double confidenceScore){
		this.docID = docID;
		this.sentenceSpan = sentenceSpan;
		this.opinSpan = opinSpan;
		this.holderSpan = holderSpan;
		this.targetSpan = targetSpan;
		this.polarity = polarity;
		this.confidenceScore = confidenceScore; 
	}
	
}

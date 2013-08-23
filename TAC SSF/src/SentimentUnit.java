// each opinion extracted from any sentiment analysis system 
// is represented by this class

public class SentimentUnit{
	
	// we can add more public attributes, if needed
	public String docID;  // document ID
	public String sentenceSpan;  // span of the sentence containing this opinion
	public String sentenceOffsets;
	public String opinSpan;  // span of the opinion expression
	public String opinOffsets;
	public String holderSpan;  // span of the holder
	public String holderOffsets
	public String targetSpan;  // span of the target
	public String targetOffsets;
	public String polarity;    // polarity, pos or neg
	public double confidenceScore;  // confidence score of the extracted opinion
	
	public SentimentUnit(String docID, String sentenceSpan, String sentenceOffsets, 
	                String opinSpan, String opinOffsets,
	                String holderSpan, String holderOffsets,
			String targetSpan, String, targetOffsets,
			String polarity, double confidenceScore){
		this.docID = docID;
		this.sentenceSpan = sentenceSpan;
		this.sentenceOffsets = sentenceOffsets;
		this.opinSpan = opinSpan;
		this.opinOffsets = opinOffsets;
		this.holderSpan = holderSpan;
		this.holderOffsets = holderOffsets;
		this.targetSpan = targetSpan;
		this.targetOffsets = targetOffsets;
		this.polarity = polarity;
		this.confidenceScore = confidenceScore; 
	}
	
}

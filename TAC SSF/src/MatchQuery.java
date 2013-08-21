// analyze the sentiment unit, whether it is consistent with the query
// the particular attribute of query is not determined?

public class MatchQuery{
	String filler;
	
	public MatchQuery(SentimentUnit su, Query query){
		
		// the following two cases are cases
		// which the sentiment unit is in accordance with query
		// that means 
		// 1) when the query type is "pos/neg-from", the query is the target and the filler is the holder
		if (query.sent.toString.contains("from") && query.entity.equals(su.targetSpan)){
			filler = su.holderSpan;
		}
		// 2) when the query type is "pos/neg-towards", the query is the holder and the filler is the target
		else if (query.sent.toString.contains("towards") && query.entity.equals(su.holderSpan)){
			filler = su.targetSpan;
		}
		
	}
}

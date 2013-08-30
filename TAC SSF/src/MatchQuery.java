import java.util.ArrayList;
import java.util.List;

import opinUnit.SentimentUnit;

// analyze the sentiment unit, whether it is consistent with the query
// the particular attribute of query is not determined?

public class MatchQuery{
	public Response response;
	
	public MatchQuery(ArrayList<SentimentUnit> suList, Query query){
		
		public String path = "/home/carmen/KBP-annotations";
		
		for (SentimentUnit su: suList){
			int[] holderOffsets = {Integer.parseInt(su.holderOffsets.split("-")[0]), Integer.parseInt(su.holderOffsets.split("-")[1])};
			int[] targetOffsets = {Integer.parseInt(su.targetOffsets.split("-")[0]), Integer.parseInt(su.targetOffsets.split("-")[1])};
			
			NEReader NE1 = new NEReader(path);
			NE1.parseNEs(su.docID);
			List<NamedEntity> holders = NE1.getNEs(holderOffsets[0], holderOffsets[1]);
			List<NamedEntity> targets = NE1.getNEs(targetOffsets[0], targetOffsets[1]);
			
			Boolean holderFlag = false;
			Boolean targetFlag = false;
			
			// holder
			for (NamedEntity holder: holders){
				if (query.nodeId.charAt(0)=='E'){
					if (holder.resolutions.containsKey(query.nodeId))
						holderFlag = true;
				} // if in KB
				else{
					if (query.entity.equals(holder.entity))
						holderFlag = true;
				}
			}
			
			// target
			for (NamedEntity target: targets){
				if (query.nodeId.charAt(0)=='E'){
					if (target.resolutions.containsKey(query.nodeId))
						targetFlag = true;
				} // if in KB
				else{
					if (query.entity.equals(target.entity))
						targetFlag = true;
				}
			}
			
			// the following two cases are cases
			// which the sentiment unit is in accordance with query
			// that means 
			// 1) when the query type is "pos/neg-from", the query is the target and the filler is the holder
			if (query.sent.toString().contains("from") && targetFlag){
				response = new Response(query.qId, su.polarity, "pitt", "", su.holderSpan, su.holderOffsets, su.targetOffsets, "", su.confidenceScore);
			}
			// 2) when the query type is "pos/neg-towards", the query is the holder and the filler is the target
			else if (query.sent.toString().contains("towards") && holderFlag){
				response = new Response(query.qId, su.polarity, "pitt", "", su.targetSpan, su.targetOffsets, su.holderOffsets, "", su.confidenceScore);
			}
		}
		
	}
}

import java.util.ArrayList;

import org.apache.solr.client.solrj.SolrServerException;


public class QueryBundle {
	public Query query;
	//todo standford parse tree
	public ArrayList<String> docIds;
	
	public QueryBundle(Query q){
		query = q;
		docIds = new ArrayList<String>();
		try {
			docIds.addAll(SolrInterface.getByTexualSearch(query.entity));
			if (query.sent==Sentiment.neg_towards || query.sent==Sentiment.pos_towards){
				docIds.addAll(SolrInterface.getByAuthorSearch(query.entity));
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

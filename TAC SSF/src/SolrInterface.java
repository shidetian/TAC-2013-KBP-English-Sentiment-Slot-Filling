import java.util.ArrayList;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;


public class SolrInterface {
	public static String getRawDocument(String id) throws SolrServerException{
		HttpSolrServer server = new HttpSolrServer("http://ec2-23-20-151-240.compute-1.amazonaws.com:8983/solr/");
		SolrQuery query = new SolrQuery();
		query.setQuery("id:"+id);
		query.setStart(0);
		query.setFields("whole_text");
		
		QueryResponse response = server.query(query);
		SolrDocumentList results = response.getResults();
	    for (int i = 0; i < results.size(); ++i) {
	    	@SuppressWarnings("unchecked")
			ArrayList<Object> temp = (ArrayList<Object>) (results.get(i).getFieldValue("whole_text"));
	    	if (temp!=null){
	    		return (String) temp.get(0);
	    	}
	    }
	    return null;
	}
	
	public static void main(String[] args) throws SolrServerException{
		getRawDocument("eng-NG-31-100124-10777395");
	}
}

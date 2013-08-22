import java.io.IOException;
import java.util.ArrayList;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import edu.stanford.nlp.trees.Tree;

public class SolrInterface {
	static HttpSolrServer server = new HttpSolrServer("http://ec2-23-20-151-240.compute-1.amazonaws.com:8983/solr/");
	
	public static String getRawDocument(String id) throws SolrServerException{
		SolrQuery query = new SolrQuery();
		if (id.contains(".")){
			id = id.substring(0, id.indexOf('.'));
		}
		query.setQuery("id:"+id);
		query.setStart(0);
		query.setFields("whole_text");
		
		QueryResponse response = server.query(query);
		SolrDocumentList results = response.getResults();
	    for (int i = 0; i < results.size(); ++i) {
	    	return (String) results.get(i).getFieldValue("whole_text");
	    }
	    return null;
	}
	
	@SuppressWarnings("unchecked")
	public static ProcessedDocument getProcessedDocument(String id) throws SolrServerException, ClassNotFoundException, IOException{
		SolrQuery query = new SolrQuery();
		if (id.contains(".")){
			id = id.substring(0, id.indexOf('.'));
		}
		query.setQuery("id:"+id);
		query.setStart(0);
		query.setFields("offsets", "tokens", "tree");
		
		QueryResponse response = server.query(query);
		SolrDocumentList results = response.getResults();
	    for (int i = 0; i < results.size(); i++) {
	    	return new ProcessedDocument((String) results.get(i).getFieldValue("offsets"),
	    						(String) results.get(i).getFieldValue("tokens"),
	    						(ArrayList<Tree>) Preprocessor.fromBase64(((byte[]) results.get(i).getFieldValue("tree")))
	    	);
	    }
	    return null;
	}
	
	public static ArrayList<String> getByTexualSearch(String s) throws SolrServerException{
		SolrQuery query = new SolrQuery();
		query.setQuery("text:"+s);
		query.setStart(0);
		query.setRows(1000);
		query.setFields("id");
		
		QueryResponse response = server.query(query);
		SolrDocumentList results = response.getResults();
		ArrayList<String> ids = new ArrayList<String>(results.size());
	    for (int i = 0; i < results.size(); ++i) {
	    	ids.add((String) results.get(i).getFieldValue("id"));
	    	System.out.println(ids.get(ids.size()-1));
	    }
	    return null;
	}
	
	public static ArrayList<String> getByAuthorSearch(String s) throws SolrServerException{
		SolrQuery query = new SolrQuery();
		query.setQuery("author:"+s);
		query.setStart(0);
		query.setRows(1000);
		query.setFields("id");
		
		QueryResponse response = server.query(query);
		SolrDocumentList results = response.getResults();
		ArrayList<String> ids = new ArrayList<String>(results.size());
	    for (int i = 0; i < results.size(); ++i) {
	    	ids.add((String) results.get(i).getFieldValue("id"));
	    }
	    return null;
	}
	
	/*public static ArrayList<String> getByMentionsSearch(String s) throws SolrServerException{
		SolrQuery query = new SolrQuery();
		query.setQuery("mentions:"+s);
		query.setStart(0);
		query.setFields("id");
		
		QueryResponse response = server.query(query);
		SolrDocumentList results = response.getResults();
		ArrayList<String> ids = new ArrayList<String>(results.size());
	    for (int i = 0; i < results.size(); ++i) {
	    	ids.add((String) results.get(i).getFieldValue("id"));
	    }
	    return null;
	}*/
	
	public static void main(String[] args) throws SolrServerException, ClassNotFoundException, IOException{
		//System.out.println(getProcessedDocument("eng-NG-31-100177-10778010"));
		Object temp = getProcessedDocument("eng-NG-31-100124-10777395");
		getByTexualSearch("CIA");
	}
}

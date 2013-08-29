import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import edu.stanford.nlp.trees.Tree;

public class SolrInterface {
	static HttpSolrServer server = new HttpSolrServer("http://54.221.246.163:8983/solr/");
	
	public static String getOriginalId(String id){
		if (id.contains(".")){
			if (id.charAt(id.indexOf('.')+1)=='0' || (id.substring(id.indexOf('.')+1).length()==4 && id.charAt(id.indexOf('.')+1)!='p')){
				return id;
			}else return id.substring(0, id.indexOf('.'));
		}else{
			return id;
		}
	}
	
	public static String getRawDocument(String id) throws SolrServerException{
		SolrQuery query = new SolrQuery();
		query.setQuery("id:"+getOriginalId(id));
		query.setStart(0);
		query.setFields("whole_text");
		
		QueryResponse response = server.query(query);
		SolrDocumentList results = response.getResults();
		if (results.size()==0){
			//Hack to get news
			query.setQuery("id:"+id);
			response = server.query(query);
			results = response.getResults();
			return results.size()==0?null:(String) results.get(0).getFieldValue("whole_text");
		}else{
			return (String) results.get(0).getFieldValue("whole_text");
		}
	}
	
	@SuppressWarnings("unchecked")
	public static ProcessedDocument getProcessedDocument(String id) throws SolrServerException, ClassNotFoundException, IOException{
		SolrQuery query = new SolrQuery();
		query.setQuery("id:"+getOriginalId(id));
		query.setStart(0);
		query.setFields("offsets", "tokens", "tree");
		
		QueryResponse response = server.query(query);
		SolrDocumentList results = response.getResults();
	    /*if (results.size()>0 && results.get(0).getFieldValue("offsets")!=null) {
	    	return new ProcessedDocument((String) results.get(0).getFieldValue("offsets"),
	    						(String) results.get(0).getFieldValue("tokens"),
	    						(ArrayList<Tree>) Preprocessor.fromBase64(((byte[]) results.get(0).getFieldValue("tree")))
	    	);
	    }else{*/
	    	String rawText = SolrInterface.getRawDocument(getOriginalId(id));
	    	if (rawText==null){
	    		return null;
	    	}
			Object[] processed = Preprocessor.Tokenize(StripXMLTags.strip(rawText).toString());
			
			String offsets = (String) processed[0];
			String tokens = (String) processed[1];
			ArrayList<Tree> trees = (ArrayList<Tree>) Preprocessor.fromBase64((byte[]) processed[2]);
			
			//Cache in Solr
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id", getOriginalId(id));
			HashMap<String, String> offsetUpdate = new HashMap<String, String>();
			offsetUpdate.put("set", offsets);
			HashMap<String, String> tokensUpdate = new HashMap<String, String>();
			tokensUpdate.put("set", tokens);
			HashMap<String, Object> treesUpdate = new HashMap<String, Object>();
			treesUpdate.put("set", processed[2]);
			
			doc.addField("offsets", offsetUpdate);
			doc.addField("tokens", tokensUpdate);
			doc.addField("tree", treesUpdate);
			
			server.add(doc, 10000); //commit within 10 seconds
			
	    	return new ProcessedDocument(offsets,tokens, trees);
	    //}
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
	    	//System.out.println(ids.get(ids.size()-1));
	    }
	    return ids;
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
	    return ids;
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
		String s = (getRawDocument("APW_ENG_20090623.1102"));
		Object temp = getProcessedDocument("bolt-eng-DF-170-181125-9140399");
		//getByTexualSearch("CIA");
	}
}

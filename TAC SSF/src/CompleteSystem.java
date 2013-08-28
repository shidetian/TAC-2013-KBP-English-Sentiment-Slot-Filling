import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.xml.sax.SAXException;

import edu.stanford.nlp.trees.Tree;


public class CompleteSystem {
	
	
	//Arguments: <query file>
	@SuppressWarnings("unused")
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, ClassNotFoundException, SolrServerException {
		ArrayList<Query> qs = QueryReader.parseFile(args[0]);
		for(Query q : qs){
			QueryBundle b = new QueryBundle(q);
			//Call systems with QueryBundle
			//This is what you would do if the tree was in Solr Already
			for (String id : b.docIds){
				ProcessedDocument processed = SolrInterface.getProcessedDocument(id);
			//int i =0;
				System.out.println(id);
			}
		}
	}

}

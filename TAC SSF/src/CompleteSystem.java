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
			ProcessedDocument processed = SolrInterface.getProcessedDocument(b.query.docId);
			int i =0;
			//This is how you do it manually
			/*String rawText = SolrInterface.getRawDocument(b.query.docId);
			Object[] processed = Preprocessor.Tokenize(rawText);
			String offsets = (String) processed[0];
			String tokens = (String) processed[1];
			ArrayList<Tree> trees = (ArrayList<Tree>) Preprocessor.fromBase64((byte[]) processed[2]);*/
		}
	}

}

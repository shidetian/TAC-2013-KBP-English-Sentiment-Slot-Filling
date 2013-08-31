import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.xml.sax.SAXException;

import edu.stanford.nlp.trees.Tree;


public class CompleteSystem {
	
	
	//Arguments: <query file>
	//@SuppressWarnings("unused")
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, ClassNotFoundException, SolrServerException {
		System.out.println("IN MAIN BEGIN~!!!");
		
		//ArrayList<Query> qs = QueryReader.parseFile(args[0]);
		ArrayList<Query> qs = new ArrayList<Query>();
		Query query  = new Query("SSF13_ENG_TRAINING_038", "Malta", "APW_ENG_20090608.0585",
				"90".concat("-").concat("94"), "GPE", "E0426219", Sentiment.pos_from);
		qs.add(query);
		
		OutputWriter output = new OutputWriter();
		
		for(Query q : qs){
			/*
			System.out.println("checking Query Bundle...");
			QueryBundle b = new QueryBundle(q);
			//Call systems with QueryBundle
			//This is what you would do if the tree was in Solr Already
			String[] testIds = {b.docIds.get(0)};
			System.out.println("testIds..."+testIds.toString());
			
			for (String id : testIds){
				ProcessedDocument processed = SolrInterface.getProcessedDocument(id);
			//int i =0;
				System.out.println("Processed ids:..."+id);
			}
			*/
			String[] docIDs = new String[1];
			docIDs[0] = "eng-NG-31-105369-11947957";
			System.out.println("Begin Preprocessed...");
			//ProcessedDocument processed = SolrInterface.getProcessedDocument(docIDs[0]);
			
			
			System.out.println("Begin PittSystem...");
			PittSystem pitt = new PittSystem();
			pitt.run(docIDs);
			
			System.out.println("Begin Ensemble...");
			SentimentEnsemble ensemble = new SentimentEnsemble("pitt_output.txt", "pitt_output.txt");
			ensemble.ensemble();
			
			System.out.println("Begin MatchQuery...");
			MatchQuery match = new MatchQuery(ensemble.sentimentListEnsembled, q);
			ArrayList<Response> responseList = match.responseList;
			if (!responseList.isEmpty()){
				for (Response response: responseList)
					output.addResponse(response);
			}
		}
		output.write("test_pitt.txt", true);
	}

}

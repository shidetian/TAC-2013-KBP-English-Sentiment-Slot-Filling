import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class QueryReader {
	public static ArrayList<Query> parseFile(String path) throws SAXException, IOException, ParserConfigurationException{
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(path));
		doc.normalizeDocument();
		
		if (!doc.getDocumentElement().getNodeName().equals("kbpsentslotfill")){
			System.err.println("Root element not kbpsentslotfill. Wrong file?");
			return null;
		}
		
		NodeList queries = doc.getElementsByTagName("query");
		ArrayList<Query> out = new ArrayList<Query>(queries.getLength());
		
		for (int i = 0; i < queries.getLength(); i++){
			Node query = queries.item(i);
			if (query.getNodeType()==Node.ELEMENT_NODE){
				Element temp = (Element) query;
				String qId = temp.getAttribute("id");
				String entity = temp.getElementsByTagName("name").item(0).getTextContent();
				String docId = temp.getElementsByTagName("docid").item(0).getTextContent();
				String entityOffsets = temp.getElementsByTagName("beg").item(0).getTextContent()+"-"+
										temp.getElementsByTagName("end").item(0).getTextContent();
				String entityType = temp.getElementsByTagName("enttype").item(0).getTextContent();
				String nodeId = temp.getElementsByTagName("nodeid").item(0).getTextContent();
				Sentiment sent = Sentiment.fromString(temp.getElementsByTagName("slot").item(0).getTextContent());
				out.add(new Query(qId, entity, docId, entityOffsets, entityType, nodeId, sent));
			}else{
				System.err.println("Extraneous node. Should not happen");
			}
		}
		return out;
	}
	
	//this is an example
	public static void main(String[] args){
		try {
			ArrayList<Query> qs = QueryReader.parseFile("tac_2013_kbp_sentiment_slot_filling_sample_queries.xml");
			qs.size();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

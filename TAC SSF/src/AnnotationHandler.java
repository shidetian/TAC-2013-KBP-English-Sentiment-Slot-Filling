import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class AnnotationHandler extends DefaultHandler {
	private HttpSolrServer server;
	
	SolrInputDocument currentDoc;
	StringBuilder contentBuffer;
	String docId;
	boolean inRes = false;
	AnnotationHandler(HttpSolrServer server){
		this.server = server;
		currentDoc = null;
		contentBuffer = new StringBuilder();
	}
	
	@Override
	public void startElement(String uri, String localName,String qName, 
            Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("entity")){
			String type = attributes.getValue("type");
		}else if (qName.equalsIgnoreCase("document")){
			docId = attributes.getValue("DOCID");
		}else if (qName.equalsIgnoreCase("entity_resolution")){
			//do nothing
		}else if (qName.equalsIgnoreCase("resolution")){
			double prob = Double.parseDouble(attributes.getValue("PROBABILITY"));
			inRes = true;
		}
	}
	
	@Override
	public void characters(char ch[], int start, int length){
		if (inRes){
			currentDoc.addField("resolution", new String(ch, start, length));
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName){
		if (qName.equalsIgnoreCase("entity")){
			//System.out.println("\n========");
			if (!skip){
				KBImporter.kb.put(current.getId(), current);
			}
			current = null;
			skip = true;
		}
		
	}
}

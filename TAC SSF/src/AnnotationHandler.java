import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class AnnotationHandler extends DefaultHandler {
	private HttpSolrServer server;
	
	SolrInputDocument currentDoc;
	StringBuilder contentBuffer;
	String docId, wholeDoc;
	boolean inRes = false;
	AnnotationHandler(HttpSolrServer server, String wholeDoc){
		this.server = server;
		currentDoc = null;
		this.wholeDoc = wholeDoc;
		contentBuffer = new StringBuilder();
	}
	
	@Override
	public void startElement(String uri, String localName,String qName, 
            Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("entity")){
			//String type = attributes.getValue("type");
		}else if (qName.equalsIgnoreCase("document")){
			currentDoc = new SolrInputDocument();
			docId = attributes.getValue("DOCID");
			currentDoc.addField("id", docId);
			currentDoc.addField("whole_text", wholeDoc);
		}else if (qName.equalsIgnoreCase("entity_resolution")){
			//do nothing
		}else if (qName.equalsIgnoreCase("resolution")){
			//double prob = Double.parseDouble(attributes.getValue("PROBABILITY"));
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
		if (qName.equalsIgnoreCase("document")){
			//System.out.println("\n========");
			try {
				server.add(currentDoc, 1800000);
			} catch (SolrServerException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static void main(String[] args) throws IOException, Exception{
		HttpSolrServer server = new HttpSolrServer("http://54.221.246.163:8984/solr/");
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		SAXParser parser = factory.newSAXParser();
		File folder = new File(args[0]);
		File[] files = null;
		if (folder.isDirectory()){
			files = folder.listFiles();
		}else{
			files = new File[]{folder};
		}
		for (File f: files){
			parser.parse(f, new AnnotationHandler(server, StripXMLTags.readFile(f.getAbsolutePath(), StandardCharsets.UTF_8)));
			server.commit();
		}
	}
}

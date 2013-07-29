import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CorpusHandler extends DefaultHandler{
	private HttpSolrServer server;
	
	SolrInputDocument currentDoc;
	
	String category;
	String baseID;
	StringBuilder contentBuffer;
	StringBuilder otherBuffer;
	int count = 0;
	
	boolean inDocId = false;
	boolean inContent = false;
	boolean inPoster = false;
	
	CorpusHandler(HttpSolrServer server){
		this.server = server;
		currentDoc = null;
		category = null;
		baseID = null;
		count = 0;
		contentBuffer = new StringBuilder();
		otherBuffer = new StringBuilder();
	}
	
	private void clear(){
		category = null;
		baseID = null;
		count = 0;
		inDocId = false;
		inContent = false;
		inPoster = false;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
		if (qName.equalsIgnoreCase("DOC")){
			if (currentDoc!=null){
				throw new SAXException("Nested Document taggs. Input file has problems.\n");
			}else{
				currentDoc = new SolrInputDocument();
				switch (attributes.getLength()){
					case 0: category = "web";
							break;
					case 1: category = "discussion";
							baseID = attributes.getValue(0);
							currentDoc.addField("id", attributes.getValue(0));
							break;
					case 2: category = "news";
							currentDoc.addField("id", attributes.getValue(0));
							//TODO: process news type attribute if needed
							break; 
				}
				currentDoc.addField("category", category);
				
			}
		}else if (qName.equalsIgnoreCase("DOCID")){
			//Should happen in web documents only
			inDocId = true;
		}else if (qName.equalsIgnoreCase("DOCTYPE")){
			//Should happen in web documents only
			//TODO: process if this element is needed
			//inDocType = true;
		}else if (qName.equalsIgnoreCase("DATETIME")){
			//inDateTime = true;
		}else if (qName.equalsIgnoreCase("DATELINE")){
			//TODO: process if needed
			//inDateTime = true;
		}else if (qName.equalsIgnoreCase("POSTDATE")){
			//inDateTime = true;
		}else if (qName.equalsIgnoreCase("P")){
			inContent = true;
		}else if (qName.equalsIgnoreCase("headline")){
			inContent = true;
		}else if (qName.equalsIgnoreCase("post")){
			assert(category.equals("discussion")|| category.equals("web"));
			assert(baseID!=null);
			
			if (currentDoc!=null){ //Finish the previous doc
				if (category.equals("web") && currentDoc.getFieldValues("id")==null){
					currentDoc.addField("id", baseID);
				}
				assert(currentDoc.getFieldValue("id")!=null);
				
				try {
					SolrImporter.addDoc(server,currentDoc);
				} catch (SolrServerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				currentDoc = null;
			}
			
			currentDoc = new SolrInputDocument();
			currentDoc.addField("category", category);
			if (attributes.getQName(0).equalsIgnoreCase("author")){
				assert(category.equals("discussion"));
				currentDoc.addField("author", attributes.getValue(0));
				//Append post id to doc id
				currentDoc.addField("id", baseID+"."+attributes.getValue(2));
			}else{
				assert(category.equals("web"));
				//Append the poster number to the doc id
				currentDoc.addField("id", baseID+"."+(count++));
			}
			if (category.equals("discussion")){
				//TODO: datetime for discussions if needed
			
				inContent = true;
			}
		}else if (qName.equalsIgnoreCase("POSTER")){
			inPoster = true;
		}else if (qName.equalsIgnoreCase("BODY")){	
			//ignore since we populate fields inside
		}else if (qName.equalsIgnoreCase("TEXT")){
			//ignore since we populate fields inside
		}else{
			System.out.println("Unexpected node:"+qName);
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException{
		if (inDocId){
			//currentDoc.addField("id", new String(ch, start, length));
			//baseID = new String(ch, start, length);
			otherBuffer.append(ch, start, length);
		}else if (inContent){
			contentBuffer.append(ch, start, length);
		}else if (inPoster){
			//currentDoc.addField("author", new String(ch, start, length));
			//inPoster = false;
			otherBuffer.append(ch, start, length);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName){
		switch (qName.toLowerCase()){
		case "post":
		case "text":
		case "headline":
			currentDoc.addField("content", contentBuffer.toString());
			inContent = false;
			//Depending on memory constraints and document sizes,
			//maybe declare new one instead of virtual clear
			contentBuffer.setLength(0);
			break;
		case "docid":
			inDocId = false;
			baseID = otherBuffer.toString();
			otherBuffer.setLength(0);
			break;
		case "poster":
			currentDoc.addField("author", otherBuffer.toString());
			inPoster = false;
			otherBuffer.setLength(0);
			break;
		case "doc":
			assert(currentDoc!=null);
			try {
				SolrImporter.addDoc(server,currentDoc);
			} catch (SolrServerException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			assert(!inDocId);
			assert(!inContent);
			assert(!inPoster);
			
			clear();
			break;
		}
		
	}
}
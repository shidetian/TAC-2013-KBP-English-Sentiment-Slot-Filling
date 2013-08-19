import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;

public class CorpusHandler{
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
	boolean inDate = false;
	
	//Vital, for your health
	//private boolean avoidDeath = false;
	
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
		currentDoc = null;
		category = null;
		baseID = null;
		count = 0;
		inDocId = false;
		inContent = false;
		inPoster = false;
	}
	
	public void startElement(XmlPullParser parser) throws Exception{
		String qName = parser.getName();
		if (qName.equalsIgnoreCase("DOC")){
			if (currentDoc!=null){
				throw new Exception("Nested Document taggs. Input file has problems.\n");
			}else{
				currentDoc = new SolrInputDocument();
				switch (parser.getAttributeCount()){
					case 0: category = "web";
							break;
					case 1: category = "discussion";
							baseID = parser.getAttributeValue(0);
							currentDoc.addField("id", parser.getAttributeValue(0));
							break;
					case 2: category = "news";
							currentDoc.addField("id", parser.getAttributeValue(0));
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
			inDate = true;
		}else if (qName.equalsIgnoreCase("DATELINE")){
			inDate = true;
		}else if (qName.equalsIgnoreCase("POSTDATE")){
			inDate = true;
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
			if (parser.getAttributeCount()==3 && parser.getAttributeName(0).equalsIgnoreCase("author")){
				assert(category.equals("discussion"));
				currentDoc.addField("author", parser.getAttributeValue(0));
				//Append post id to doc id
				currentDoc.addField("id", baseID+"."+parser.getAttributeValue(2));
			}else{
				assert(category.equals("web"));
				//Append the poster number to the doc id
				currentDoc.addField("id", baseID+"."+(count++));
			}
			//TODO: datetime for discussions if needed
			
			inContent = true;
		}else if (qName.equalsIgnoreCase("POSTER")){
			inPoster = true;
		}else if (qName.equalsIgnoreCase("BODY")){	
			//ignore since we populate fields inside
		}else if (qName.equalsIgnoreCase("TEXT")){
			//ignore since we populate fields inside
		}else if (qName.equalsIgnoreCase("docs")){
			//ignore
		}else if (qName.equalsIgnoreCase("QUOTE")){
			//Note: Hack so that we have the things before the quote tag due to not XML compliant quote tag crashing parser
			if (category.equalsIgnoreCase("web")){
				//currentDoc.addField("content", contentBuffer.toString());
				endElement("post");
			}
		}else if (qName.equals("a")){
			//ignore
		}else if (qName.equals("img")){
			//ignore
		}else{
			System.out.println("Unexpected node:"+qName);
		}
	}
	
	//@Override
	public void characters(String chars){
		if (inDocId){
			//currentDoc.addField("id", new String(ch, start, length));
			//baseID = new String(ch, start, length);
			otherBuffer.append(chars.trim());
		}else if (inPoster){
			//currentDoc.addField("author", new String(ch, start, length));
			//inPoster = false;
			otherBuffer.append(chars.trim());
		}else if (inDate){
			
		}else if (inContent){
			contentBuffer.append(chars.trim());
		}
	}
	
	//@Override
	public void endElement(String qName){
		//String qName = parser.getName();
		switch (qName.toLowerCase()){
		case "post":
		case "text":
		case "headline":
			currentDoc.addField("content", contentBuffer.toString().trim());
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
			currentDoc.addField("author", otherBuffer.toString().trim());
			inPoster = false;
			otherBuffer.setLength(0);
			break;
		case "postdate":
		case "dateline":
		case "datetime":
			otherBuffer.setLength(0);
			inDate = false;
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
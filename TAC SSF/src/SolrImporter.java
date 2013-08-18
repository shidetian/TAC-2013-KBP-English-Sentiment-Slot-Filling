import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class SolrImporter {
	//Buffer documents so that we can batch commit
	//private static ArrayList<SolrInputDocument> serverCommitBuffer = new ArrayList<SolrInputDocument>();
	private static int numNotCommitted = 0;
	
	
	//Reads a (potentially gzip) file from corpus and all the documents within it
	public static void readFileCorpus(HttpSolrServer server, XmlPullParserFactory factory, String file) throws Exception{
		//InputStreamReader full;
		InputStream raw;
		//Simple test if file is gziped (not foolproof!)
		//We must also enclose the input with a root node since the corpus doesn't have any :'(
		if (file.endsWith(".gz")){
			raw = new SequenceInputStream(
					Collections.enumeration(Arrays.asList(
							new InputStream[]{
									new ByteArrayInputStream("<docs>".getBytes()),
									new GZIPInputStream(new FileInputStream(file)),
									new ByteArrayInputStream("</docs>".getBytes())
							})));
			//new InputStream(new GZIPInputStream(new FileInputStream(file)), "UTF-8");
		}else{
			raw = new SequenceInputStream(
					Collections.enumeration(Arrays.asList(
							new InputStream[]{
									new ByteArrayInputStream("<docs>".getBytes()),
									new FileInputStream(file),
									new ByteArrayInputStream("</docs>".getBytes())
							})));
		}
		//full = new InputStreamReader(raw, "UTF-8");
		//InputSource in = new InputSource(full);
		//in.setEncoding("UTF-8");
		
		XmlPullParser pullParser = factory.newPullParser();
		pullParser.setInput(raw, "UTF-8");
		//saxParser.parse(in , new CorpusHandler(server));
		//Use the XmlPullParser as a driver to the SAX compliant CorpusHandler
		//Note that QName is probably not correct
		CorpusHandler handler = new CorpusHandler(server);
		int type = pullParser.getEventType();
		while (type!=XmlPullParser.END_DOCUMENT){
			switch(type){
			case XmlPullParser.START_TAG:
				/*if (pullParser.getName().equalsIgnoreCase("QUOTE")){
					//Resolve issues with corpus being in xhtml and not xml, so the quote is not self closed
					//TODO: process if needed
					//Skip it
					//pullParser.
					int i = 0;
					//break;
				}*/
				handler.startElement(pullParser);
				break;
			case XmlPullParser.END_TAG: handler.endElement(pullParser.getName()); break;
			case XmlPullParser.TEXT: handler.characters(pullParser.getText()); break;
			}
			try{
				type = pullParser.next();
			}catch(XmlPullParserException e){
				if (e.getMessage().contains("</DOC>")){
					handler.endElement("doc");
				}
			}catch(EOFException e){
				System.out.println("Warning: some quote related XML problems were fixed/ignored.\n");
				break;
			}
		}
	}
	
	public static void addDoc(HttpSolrServer server, SolrInputDocument doc) throws SolrServerException, IOException{
		server.add(doc);
		if (numNotCommitted>100){
			forceCommit(server);
		}
	}
	
	public static void forceCommit(HttpSolrServer server) throws SolrServerException, IOException{
		server.commit();
		numNotCommitted = 0;
	}
	/**
	 * @param args 
	 * @throws XmlPullParserException 
	 * @throws IOException 
	 * @throws SolrServerException 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws XmlPullParserException, SolrServerException, IOException{
		// TODO Auto-generated method stub
		HttpSolrServer server = new HttpSolrServer("http://localhost:8983/solr");
		//SAXParserFactory factory = SAXParserFactory.newInstance();
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		try {
			//readFileCorpus(server, factory, "D:\\Dropbox\\NLP Research\\Corpus\\data\\English\\web\\eng-NG-31-1256.gz");
			readFileCorpus(server, factory, "C:\\Users\\Detian\\Documents\\GitHub\\TAC-2013-KBP-English-Sentiment-Slot-Filling\\DataSubset\\discussions.xml");
			//readFileCorpus(server, factory, "D:\\Dropbox\\NLP Research\\Resources\\LDC2013E61_TAC_2013_KBP_English_Sentiment_Slot_Filling_Sample_Queries_and_Annotations\\data\\tac_2013_kpb_ssf_sample_documents.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		forceCommit(server);
	}

}

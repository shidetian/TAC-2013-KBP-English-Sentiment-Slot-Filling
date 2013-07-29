import java.io.ByteArrayInputStream;
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

public class SolrImporter {
	//Buffer documents so that we can batch commit
	//private static ArrayList<SolrInputDocument> serverCommitBuffer = new ArrayList<SolrInputDocument>();
	private static int numNotCommitted = 0;
	
	
	//Reads a (potentially gzip) file from corpus and all the documents within it
	public static void readFileCorpus(HttpSolrServer server, SAXParserFactory factory, String file) throws UnsupportedEncodingException, FileNotFoundException, IOException, ParserConfigurationException, SAXException{
		InputStreamReader full;
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
		full = new InputStreamReader(raw, "UTF-8");
		InputSource in = new InputSource(full);
		in.setEncoding("UFT-8");
		
		SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(in , new CorpusHandler(server));
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
	 * @throws IOException 
	 * @throws SolrServerException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws SolrServerException, IOException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub
		HttpSolrServer server = new HttpSolrServer("http://localhost:8983/solr");
		SAXParserFactory factory = SAXParserFactory.newInstance();
		readFileCorpus(server, factory, "C:\\Users\\Detian\\Documents\\GitHub\\TAC-2013-KBP-English-Sentiment-Slot-Filling\\DataSubset\\newswire.xml");
		forceCommit(server);
	}

}

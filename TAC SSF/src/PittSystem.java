import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.io.*;

import org.apache.solr.client.solrj.SolrServerException;

import opin.main.opinionFinder;


public class PittSystem {
	
	static OpinionFinder of;
	static OpinionLexiconChecker ow;
	static SentenceSplitter ss;
	static HTParser parser;
	static HTDetection ht;
	static HTbb htLingjia;
	static NEReader ner;
	
	// Initialize Pitt System
	public PittSystem(){
		ss = new SentenceSplitter("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
		of = new OpinionFinder();
		parser = new HTParser();
		ow = new OpinionLexiconChecker();
		ht = new HTDetection();
		htLingjia = new HTbb();
		ner = new NEReader("");
	}
	
	public void run(QueryBundle qb){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("PittSystem_output.txt"));
			
			for(String docid : qb.docIds){
				System.out.println("ID: " + docid);
				String doc = SolrInterface.getRawDocument(docid);
				//System.out.println(doc);
				
				String text = "";
				int fromIndex = 0;
				
				if(docid.startsWith("bolt")){
					String[] toks = docid.split(".p");
					if(toks.length < 2)
						continue;
					int nPost = Integer.parseInt(toks[1]);
					//System.out.println(nPost);
					
					while((nPost--) > 0){
						fromIndex = doc.indexOf("<post", fromIndex+1);
						//System.out.println(nPost + " : " + fromIndex);
					}
					int endIndex = doc.indexOf("</post>", fromIndex);
					text = doc.substring(fromIndex, endIndex+7);
				}
				else{
					text = doc;
				}
				
				//System.out.println(docid + " : " + fromIndex + "\n" + text);
				
				StringBuffer newStr = StripXMLTags.strip(text);
				String str = new String(newStr);
				//System.out.println(str);
				
				ner.parseNEs(docid);
				
				List<Sentence> sents = ss.process(str);
				
				for(Sentence sent : sents){
					sent.beg = sent.beg + fromIndex;
					sent.end = sent.end + fromIndex;
					
					String temp = sent.sent.trim();
					if(temp.length() <= 1)
						continue;
						
					// OpinionFinder
					HashMap<String, String> pol = opinionFinder.runOpinionFinder(sent.sent);
					// Opin Word Checker
					pol.putAll(ow.runOpinionWordChecker(sent.sent))；
						
					HashMap<String, String> polarity = new HashMap<String, String>();
					String sSpan = Integer.toString(sent.beg).concat("-").concat(Integer.toString(sent.end));
					
					HashSet<String> polterms = new HashSet<String>();
					Set<String> keyset = pol.keySet();
					Iterator<String> iter = keyset.iterator();
					while(iter.hasNext()){
						String offset = iter.next();
						String[] toks = offset.split("_");
						polterms.add(sent.sent.substring(Integer.parseInt(toks[0]), Integer.parseInt(toks[1])));
						//System.out.println("OWords : " + sent.sent.substring(Integer.parseInt(toks[0]), Integer.parseInt(toks[1])) + " , " + pol.get(offset));
						polarity.put(sent.sent.substring(Integer.parseInt(toks[0]), Integer.parseInt(toks[1])), pol.get(offset));
					}
					
					// Holder and Target Detection
					String dep = HTParser.getDependencyString(sent.sent);
					List<NamedEntity> NEs = ner.getNEs(sent.beg, sent.end);
						
					HashMap<String, String> oht = ht.process(sent, dep, polterms, NEs);
					// Opin Word Checker
					oht.putAll(htLingjia.process(sent.sent, ow.polterms, NEs, sent.beg, sent.end));
					
					keyset = oht.keySet();
					iter = keyset.iterator();
					while(iter.hasNext()){
						String opin = iter.next();
						//System.out.println("Extracted: " + opin);
						String p = polarity.get(opin);
						bw.write(docid + "\t" + sent.sent + "\t" + sSpan + "\t" + oht.get(opin) + "\t" + p + "\t" + "1.0\n");
					}
				}
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

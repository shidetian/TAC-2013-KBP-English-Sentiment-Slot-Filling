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
	static HTLast htLingjia;
	static NEReader ner;
	
	// Initialize Pitt System
	public PittSystem(){
		ss = new SentenceSplitter("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
		of = new OpinionFinder();
		parser = new HTParser();
		ow = new OpinionLexiconChecker();
		ht = new HTDetection();
		htLingjia = new HTLast();
		ner = new NEReader("//home/carmen/KBP-annotations");
	}
	
	public void run(QueryBundle qb){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("pitt_output.txt"));
			
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
				
				String author = "";
				String aidx = "";
				
				if(text.contains("<post author=")){
					int tidx = text.indexOf("<post author");
					int sidx = text.indexOf("\"", tidx)+1;
					int eidx = text.indexOf("\"", sidx);
					author = text.substring(sidx, eidx);
					aidx = Integer.toString(sidx+fromIndex).concat("-").concat(Integer.toString(eidx+fromIndex));
				}
				
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
					
					// get the dependency string
					// where is tree...?
					String dep = parser.getDependencyStringFromSentence(sent.sent);
					// the next line is getting the depdency from tree object
					//String dep = parser.getDependencyStringFromTree(tree);
					
					// Holder and Target Detection
					List<NamedEntity> NEs = ner.getNEs(sent.beg, sent.end);
					HashSet<String> NEsInString = new HashSet<String>();
					for (NamedEntity ne : NEs){
						NEsInString.add(ne.entity);
					}
						
					HashMap<String, String> oht = ht.process(sent, dep, polterms, NEs, author, aidx);
					// Opin Word Checker
					oht.putAll(htLingjia.process(sent.sent, parser, ow.polterms, NEsInString, sent.beg, sent.end));
					
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

import java.util.*;
import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.xml.sax.SAXException;

import opin.main.opinionFinder;


public class PittSystem {
	
	static OpinionFinder of;
	static OpinionLexiconChecker ow;
	static HTParser parser;
	static NEReader ner;
	
	// Initialize Pitt System
	public PittSystem(){
		try {
			parser = new HTParser();
			of = new OpinionFinder();	
			ow = new OpinionLexiconChecker();
			ner = new NEReader();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void run(QueryBundle qb){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("output/pitt_SQ3DAnswer.txt"));
			
			String[] sampleAnnotationDocIds = new String[5];
			sampleAnnotationDocIds[0] = "bolt-eng-DF-170-181109-8867106";
			sampleAnnotationDocIds[1] = "bolt-eng-DF-199-192783-6837220";
			sampleAnnotationDocIds[2] = "bolt-eng-DF-199-192783-6862389";
			sampleAnnotationDocIds[3] = "bolt-eng-DF-199-192958-5884891";
			sampleAnnotationDocIds[4] = "bolt-eng-DF-199-192958-5885304";
			
			for(String docid : qb.docIds){
			//for(String docid : sampleAnnotationDocIds){
				
				//if (qb.docIds.indexOf(docid) > 50){
				//	break;
				//}
				
				Boolean flag = false;
				for (String tmpId : sampleAnnotationDocIds){
					if (docid.contains(tmpId)){
						flag = true;
					}
				}
				if (!flag)
					continue;
				
				System.out.println("****"+Integer.toString(qb.docIds.indexOf(docid))+"***"+"/"+Integer.toString(qb.docIds.size())+"*** ID: " + docid);
				String doc = SolrInterface.getRawDocument(docid);
				//System.out.println(doc);
				List<Sentence> allSents = processDocument(doc, SolrInterface.getProcessedDocument(docid));
				
				String text = "";
				int begIndex = 0;
				int endIndex = 0;
				
				if(docid.startsWith("bolt")){
					String[] toks = docid.split(".p");
					if(toks.length < 2)
						continue;
					int nPost = Integer.parseInt(toks[1]);
					//System.out.println(nPost);
					
					while((nPost--) > 0){
						begIndex = doc.indexOf("<post", begIndex+1);
						//System.out.println(nPost + " : " + fromIndex);
					}
					endIndex = doc.indexOf("</post>", begIndex) + 7;
					text = doc.substring(begIndex, endIndex);
				}
				else{
					text = doc;
					endIndex = doc.length();
				}
				
				//System.out.println(docid + " : " + begIndex + "\n" + text);));
				
				String author = "";
				String aidx = "";
				
				if(text.contains("<post author=")){
					int tidx = text.indexOf("<post author");
					int sidx = text.indexOf("\"", tidx)+1;
					int eidx = text.indexOf("\"", sidx);
					author = text.substring(sidx, eidx);
					aidx = Integer.toString(sidx+begIndex).concat("-").concat(Integer.toString(eidx+begIndex));
				}
				
				//ner.parseNEs(docid);
				ner.getNEAnnotations(docid);
				
				for(Sentence sent : allSents){
					if((sent.end < begIndex) || (sent.beg > endIndex))
						continue;
					
					//System.out.println(sent.beg + ", " + sent.end + " " + sent.sent);
					
					String temp = sent.sent.trim();
					if(temp.length() <= 1)
						continue;
						
					HashMap<String, String> poltermsOF = opinionFinder.runOpinionFinder(sent.sent);
					HashMap<String, String> polterms = new HashMap<String, String>();
					polterms.putAll(poltermsOF);
					if (!(ow.runOpinionWordChecker(sent.sent) == null)){
						polterms.putAll(ow.runOpinionWordChecker(sent.sent));
					}
					
					HashMap<String, String> polarity = new HashMap<String, String>();
					String sSpan = Integer.toString(sent.beg).concat("-").concat(Integer.toString(sent.end));
					
					Set<String> keyset = polterms.keySet();
					Iterator<String> iter = keyset.iterator();
					while(iter.hasNext()){
						String offset = iter.next();
						String[] toks = offset.split("_");
						polarity.put(sent.sent.substring(Integer.parseInt(toks[0]), Integer.parseInt(toks[1])), polterms.get(offset));
					}
					
					// Holder&Target Detection
					String dep = HTParser.getDependencyStringFromTree(sent.tree);
					//System.out.println(dep);
					List<NamedEntity> NEs = ner.getNEs(sent.beg, sent.end);
					System.out.println(Integer.toString(sent.beg)+"-"+Integer.toString(sent.end)+"  "+sent.sent);
					System.out.println("entities: "+Integer.toString(NEs.size()));
					
					HTDetection HTD = new HTDetection(sent, parser, NEs, author, aidx);
					HashMap<String, String> oht = HTD.getHT(poltermsOF, ow.polterms);
						
					keyset = oht.keySet();
					iter = keyset.iterator();
					System.out.println("how many candidates:"+keyset.size());
					while(iter.hasNext()){
						String opin = iter.next();
						String span = oht.get(opin);
						String p = polarity.get(opin);
						if (!(p == null) && !(span.contains("NULL"))){
							bw.write(docid + "\t" + sent.sent + "\t" + sSpan + "\t" + span + "\t" + p + "\t" + "1.0\n");
							System.out.println("...........SUCCESS.........");
							System.out.println("......"+span+"..........");
						}
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
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public List<Sentence> processDocument(String doc, ProcessedDocument pDoc){
		//System.out.println("length of offset: " + pDoc.offsets.length());
		//System.out.println("offset: " + pDoc.offsets);
		List<Sentence> sents = new ArrayList<Sentence>();
		
		if(pDoc == null)
			return sents;
		
		List<String> offsets = new ArrayList<String>();
		String[] offtoks = pDoc.offsets.split("\n");
		boolean start = false;
		String offset = "";
		String last = "";
		for(String offtok : offtoks){
			if(offtok.length() < 1){
				start = false;
				if(offset.length() > 1){
					offset = offset.concat(last);
					offsets.add(offset);	
					offset = "";
					last = "";
				}
			}
			else if(!start){
				offset = offtok.substring(0, offtok.indexOf(":")).concat("-");
				last = offtok.substring(offtok.indexOf(":") + 1);
				start = true;
			}
			else
				last = offtok.substring(offtok.indexOf(":") + 1);
		}
		
		if(offset.length() > 1){
			offset = offset.concat(last);
			offsets.add(offset);
			offset = "";
			last = "";
		}
		
		String[] tokens = pDoc.tokens.split("\n");
		
		for(int i=0; i<offsets.size(); i++){
			String off = offsets.get(i);
			String[] temp = off.split("-");
			Sentence sent = new Sentence(tokens[i], Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), pDoc.trees.get(i));
			sents.add(sent);
		}
		
		return sents;
	}

}

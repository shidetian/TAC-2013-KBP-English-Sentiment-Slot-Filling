import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.io.*;

import opin.main.opinionFinder;


public class PittSystem {
	
	static OpinionFinder of;
	static SentenceSplitter ss;
	static HTDetection ht;
	
	public PittSystem(){
		ss = new SentenceSplitter("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
		of = new OpinionFinder();	
		ht = new HTDetection();
		// initialize OpinionWords Class
	}
	
	public void run(String str, String filename){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("PittSystem_output.txt"));
			List<Sentence> sents = ss.process(str);
			
			for(Sentence sent : sents){
				System.out.println(sent.sent);
				String text = sent.sent.trim();
				if(text.length() <= 1)
					continue;
					
				HashMap<String, String> pol = opinionFinder.runOpinionFinder(sent.sent);
				HashMap<String, String> polarity = new HashMap<String, String>();
				String sSpan = Integer.toString(sent.beg).concat("-").concat(Integer.toString(sent.end));
				
				HashSet<String> polterms = new HashSet<String>();
				Set<String> keyset = pol.keySet();
				Iterator<String> iter = keyset.iterator();
				while(iter.hasNext()){
					String offset = iter.next();
					String[] toks = offset.split("_");
					polterms.add(str.substring(Integer.parseInt(toks[0])+sent.beg, Integer.parseInt(toks[1])+sent.beg));
					polarity.put(str.substring(Integer.parseInt(toks[0])+sent.beg, Integer.parseInt(toks[1])+sent.beg), pol.get(offset));
				}
				
				// call OpinionWords function
				
				HashMap<String, String> oht = ht.process(sent, polterms);
				keyset = oht.keySet();
				iter = keyset.iterator();
				while(iter.hasNext()){
					String opin = iter.next();
					String p = polarity.get(opin);
					bw.write(sSpan + "\t" + oht.get(opin) + "\t" + p + "\t" + "1\n");
				}
			}
			
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

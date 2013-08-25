import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;


public class HTDetection {
	static LexicalizedParser lp;
	
	public HTDetection(){
		lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
	}
	
	public HashMap<String, String> process(Sentence sent, HashSet<String> terms){
		HashMap<String, String> ht = new HashMap<String, String>();
		HashMap<String, String> opinterms = new HashMap<String, String>();
		for(String term : terms)
			opinterms.put(term, term);
		
		List<TypedDependency> tdl = parsing(sent.sent);
		HashMap<String, String> Scandidates = findCandidates(tdl, opinterms, "subj");
		HashMap<String, String> Ocandidates = findCandidates(tdl, opinterms, "obj");
							
		Set<String> Skey = Scandidates.keySet();
		Iterator<String> Siter = Skey.iterator();
		while(Siter.hasNext()){
			String subj = Siter.next();
			String Sopin = Scandidates.get(subj);
			subj = subj.substring(0, subj.indexOf("-"));
			boolean check = false;
			
			if(Ocandidates.size() > 0){
				Set<String> Okey = Ocandidates.keySet();
				Iterator<String> Oiter = Okey.iterator();
				while(Oiter.hasNext()){
					String obj = Oiter.next();
					String Oopin = Ocandidates.get(obj);
					obj = obj.substring(0, obj.indexOf("-"));
					
					if(Sopin.compareTo(Oopin) == 0){
						String opin = Oopin.concat("\t").
								concat(Integer.toString(sent.sent.indexOf(Oopin)+sent.beg).concat("-").
								concat(Integer.toString(sent.sent.indexOf(Oopin)+sent.beg+Oopin.length())));
						String holder = subj.concat("\t").
								concat(Integer.toString(sent.sent.indexOf(subj)+sent.beg).concat("-").
								concat(Integer.toString(sent.sent.indexOf(subj)+sent.beg+subj.length())));;
						String target = obj.concat("\t").
								concat(Integer.toString(sent.sent.indexOf(obj)+sent.beg).concat("-").
								concat(Integer.toString(sent.sent.indexOf(obj)+sent.beg+obj.length())));;
						ht.put(Oopin, opin.concat("\t").concat(holder).concat("\t").concat(target));
						//String p = polarity.get(Oopin); 
						//senti.add(new SentimentUnit(sSpan, oSpan, hSpan, tSpan, p, 0));
						check = true;
					}
					
				}
			}
			
			if(!check){
				String opin = Sopin.concat("\t").
						concat(Integer.toString(sent.sent.indexOf(Sopin)+sent.beg).concat("-").
						concat(Integer.toString(sent.sent.indexOf(Sopin)+sent.beg+Sopin.length())));
				String holder = "\t";
				String target = subj.concat("\t").
						concat(Integer.toString(sent.sent.indexOf(subj)+sent.beg).concat("-").
						concat(Integer.toString(sent.sent.indexOf(subj)+sent.beg+subj.length())));;
				ht.put(Sopin, opin.concat("\t").concat(holder).concat("\t").concat(target));
				//System.out.println("Sopin : " + Sopin);
				//String p = polarity.get(Sopin); 
				//senti.add(new SentimentUnit(sSpan, oSpan, hSpan, tSpan, p, 0));
			}
		}	
		return ht;
	}
	
	public static HashMap<String, String> findCandidates(List<TypedDependency> tdl, HashMap<String, String> terms, String type){
		HashMap<String, String> candidates = new HashMap<String, String>();
		int cnt = 0;
		
		while((cnt++) < 5){
			HashMap<String, String> newterms = new HashMap<String, String>();
			Set<String> keyset = terms.keySet();
			Iterator<String> iter = keyset.iterator();
					
			while(iter.hasNext()){
				String term = iter.next();
				String orig = terms.get(term);
				HashMap<String, String> parents = getParents(term, orig, tdl);
				HashMap<String, String> children = getChildren(term, orig, tdl);
				//System.out.println("Parent: " + parents);
				//System.out.println("Children: " + children);
				candidates.putAll(checkType(type, parents, tdl));
				candidates.putAll(checkType(type, children, tdl));
				newterms.putAll(parents);
				newterms.putAll(children);
			}
			
			//System.out.println("new terms : " + newterms);
		
			if(candidates.size() > 0)
				break;
			
			if(newterms.size() <= 0)
				break;
			else{
				terms = newterms;
			}
			
			//System.out.println("new terms : " + terms);
			
		}
		
		return candidates;
	}
	
	
	public static HashMap<String, String> checkType(String type, HashMap<String, String> list, List<TypedDependency> tdl){
		HashMap<String, String> keyset = new HashMap<String, String>();
		
		Set<String> key = list.keySet();
		Iterator<String> iter = key.iterator();
		while(iter.hasNext()){
			String term = iter.next();
			for(TypedDependency td : tdl){
				String[] toks = td.toString().split("[(), ]+");
				if(toks[2].contains(term) && toks[0].contains(type)){
					keyset.put(term, list.get(term));
				}
			}
		}
		
		//System.out.println("keyset : " + keyset);
		return keyset;
	}
	
	public static HashMap<String, String> getParents(String term, String orig, List<TypedDependency> tdl){
		HashMap<String, String> parents = new HashMap<String, String>();
		
		for(TypedDependency td : tdl){
			String[] toks = td.toString().split("[()-, ]+");
			if(toks[2].contains(term)){
				parents.put(toks[1], orig);
			}
		}
		
		return parents;
	}

	public static HashMap<String, String> getChildren(String term, String orig, List<TypedDependency> tdl){
		HashMap<String, String> children = new HashMap<String, String>();
		
		for(TypedDependency td : tdl){
			String[] toks = td.toString().split("[()-, ]+");
			if(toks[1].contains(term)){
				children.put(toks[2], orig);
			}
		}
		
		return children;
	}

	public static List<TypedDependency> parsing(String sent){
		
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
		List<CoreLabel> rawWords = tokenizerFactory.getTokenizer(new StringReader(sent)).tokenize();
		Tree parse = lp.apply(rawWords);

		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		
		//System.out.println("  " + tdl.toString());
		
		return tdl;
	}
}

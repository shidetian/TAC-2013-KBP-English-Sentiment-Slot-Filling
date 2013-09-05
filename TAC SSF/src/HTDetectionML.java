import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class HTDetectionML {
	
	public HTDetectionML(){;
	}
	
	public HashMap<String, String> process(Sentence sent, String dep, HashSet<String> terms, 
			List<NamedEntity> entities, String author, String aidx){
		try {
			//System.out.println("ML start!");
			//System.out.println("List : " + terms);
			HashMap<String, String> ht = new HashMap<String, String>();
			
			List<NamedEntity> newEntities = new ArrayList<NamedEntity>();
			for(NamedEntity entity : entities){
				//System.out.println("original: " + entity.entity);
				boolean check = false;
				
				for(NamedEntity temp : entities){
					if(entity == temp)
						continue;
					
					if(entity.entity.contains(temp.entity)){
						check = true;
					}
				}
				
				if(!check)
					newEntities.add(entity);
			}
			
			List<DependencyTriple> dtl = getDependencyTripleList(dep);
			List<NamedEntity> targetCands = new ArrayList<NamedEntity>();
			HashMap<NamedEntity, String> tOpinTerm = new HashMap<NamedEntity, String>();
			List<NamedEntity> holderCands = new ArrayList<NamedEntity>();
			HashMap<NamedEntity, String> hOpinTerm = new HashMap<NamedEntity, String>();
			
			
			BufferedWriter writer = new BufferedWriter(new FileWriter("weka_target.csv"));
			writer.write("A1,A2,A3,A4,A5,A6,A7,A8,A9,A10,A11,A12,A13,Class\n");
			
			boolean check = false;
			List<NamedEntity> targetTmp = new ArrayList<NamedEntity>();
			for(NamedEntity entity : newEntities){
				//System.out.println("extracted: " + entity.entity);
				String temp = getTargetFeatures(entity, author, terms, dtl);
				//System.out.println(temp);
				if(temp.length() > 1){
					check = true;
					writer.write(temp);
					String[] toks = temp.split("\n");
					for(int i=0; i<toks.length; i++){
						targetTmp.add(entity);
						tOpinTerm.put(entity, toks[i].substring(0, toks[i].indexOf(",")));
					}
				}
			}
			
			writer.close();
			
			if(check){
				DataSource source = new DataSource("weka_target.csv");
				Instances testdata = source.getDataSet();
				testdata.setClassIndex(testdata.numAttributes()-1);
				
				Classifier models = (Classifier) weka.core.SerializationHelper.read("target_smoreg.model");
				
				if(testdata.numInstances() != targetTmp.size())
					System.out.println("wrong number of instances");
				
				for(int i=0; i<testdata.numInstances(); i++){
					double pred = models.classifyInstance(testdata.instance(i));
					if(pred >= 1.0){
						//System.out.println(pred + " , " + targetTmp.get(i).entity);
						targetCands.add(targetTmp.get(i));
					}
				}
			}
		
			
			writer = new BufferedWriter(new FileWriter("weka_holder.csv"));
			writer.write("A1,A2,A3,A4,A5,A6,A7,A8,A9,A10,A11,A12,A13,A14,A15,Class\n");
			
			check = false;
			List<NamedEntity> holderTmp = new ArrayList<NamedEntity>();
			for(NamedEntity entity : newEntities){
				//System.out.println("extracted: " + entity.entity);
				String temp = getHolderFeatures(entity, author, terms, dtl);
				//System.out.println(temp);
				if(temp.length() > 1){
					check = true;
					writer.write(temp);
					String[] toks = temp.split("\n");
					for(int i=0; i<toks.length; i++){
						holderTmp.add(entity);
						hOpinTerm.put(entity, toks[i].substring(0, toks[i].indexOf(",")));
					}
				}
			}
			
			writer.close();
			
			if(check){
				DataSource source = new DataSource("weka_holder.csv");
				Instances testdata = source.getDataSet();
				testdata.setClassIndex(testdata.numAttributes()-1);
				
				Classifier models = (Classifier) weka.core.SerializationHelper.read("holder_smoreg.model");
				
				if(testdata.numInstances() != holderTmp.size())
					System.out.println("wrong number of instances");
				
				for(int i=0; i<testdata.numInstances(); i++){
					double pred = models.classifyInstance(testdata.instance(i));
					if(pred >= 1.0){
						//System.out.println(pred + " , " + holderTmp.get(i).entity);
						holderCands.add(holderTmp.get(i));
					}
				}
			}
			
			if((targetCands.size() == 0) || (holderCands.size() == 0))
				return ht;
			
			List<NamedEntity> holderCandTmp = new ArrayList<NamedEntity>();
			for(NamedEntity holderCand : holderCands){
				boolean hasLonger = false;
				for(NamedEntity temp : holderCands){
					if(temp.entity.compareTo(holderCand.entity) == 0)
						continue;
					
					if(temp.entity.contains(holderCand.entity)){
						hasLonger = true;
						break;
					}
				}
				
				if(!hasLonger)
					holderCandTmp.add(holderCand);
			}
			
			List<NamedEntity> targetCandTmp = new ArrayList<NamedEntity>();
			for(NamedEntity targetCand : targetCands){
				boolean hasLonger = false;
				for(NamedEntity temp : targetCands){
					if(temp.entity.compareTo(targetCand.entity) == 0)
						continue;
					
					if(temp.entity.contains(targetCand.entity)){
						hasLonger = true;
						break;
					}
				}
				
				if(!hasLonger)
					targetCandTmp.add(targetCand);
			}
			
			for(NamedEntity targetCand : targetCandTmp){
				if(targetCand.entity.compareTo(author) == 0)
					continue;
				
				for(NamedEntity holderCand : holderCandTmp){
					if(targetCand.entity.compareTo(holderCand.entity) == 0)
						continue;
					
					String targetOpin = tOpinTerm.get(targetCand);
					String holderOpin = hOpinTerm.get(holderCand);
					
					//System.out.println(targetOpin + ", " + holderOpin);
					if(targetOpin.compareTo(holderOpin) != 0)
						continue;
					
					String opin = targetOpin.concat("\t").
							concat(Integer.toString(sent.sent.indexOf(targetOpin)+sent.beg).concat("-").
							concat(Integer.toString(sent.sent.indexOf(targetOpin)+sent.beg+targetOpin.length())));
					
					String holder = holderCand.entity.concat("\t").
							concat(Integer.toString(holderCand.beg).concat("-").
							concat(Integer.toString(holderCand.end)));
					String target = targetCand.entity.concat("\t").
							concat(Integer.toString(targetCand.beg).concat("-").
							concat(Integer.toString(targetCand.end)));
					ht.put(targetOpin, opin.concat("\t").concat(holder).concat("\t").concat(target));
				}
			}
			
			System.out.println(ht);
			
			return ht;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}		
	}

	public static String getHolderFeatures(NamedEntity entity, String author, HashSet<String> terms, List<DependencyTriple> dtl){
		String feaSet = "";
		
		String phrase = entity.entity;
		
		int distSubj = getDistance(phrase, "subj", dtl);
		int distObj = getDistance(phrase, "obj", dtl);
		
		for(String term : terms){
			HashMap<String, String> opinterms = new HashMap<String, String>();
			opinterms.put(term, term);
			String fea = "";
			// F1 : opinion word
			fea = fea.concat(term).concat(",");
			//System.out.println("f1");
			// F2 : pos of opinion word
			fea = fea.concat(getPOS(term, dtl)).concat(",");
			//System.out.println("f2");
			// F3 & F4: pos of target and distance between a target and an opin word
			fea = fea.concat(getRel(phrase, opinterms, dtl)).concat(",");
			//System.out.println("f3&4");
			// F5: distance from target to subj
			fea = fea.concat(Integer.toString(distSubj)).concat(",");
			//System.out.println("f5");
			// F6: distance from target to obj
			fea = fea.concat(Integer.toString(distObj)).concat(",");
			//System.out.println("f6");
			// F7: has author information?
			if(author.length() >= 1)
				fea = fea.concat("true").concat(",");
			else
				fea = fea.concat("false").concat(",");
			//System.out.println("f7");
			// F8: NEs type
			fea = fea.concat(entity.type).concat(",");
			//System.out.println("f8");
			// F9~F13: common path
			fea = fea.concat(findCommonPath(phrase, term, dtl)).concat(",");
			//System.out.println("f9-13");
			// F14: Pronoun?
			fea = fea.concat(checkPronoun(phrase)).concat(",");
			
			// F15: is Author?
			fea = fea.concat(checkAuthor(phrase, author)).concat(",");
			// class
			fea = fea.concat("?");
			
			feaSet = feaSet.concat(fea).concat("\n");
		}
		//System.out.println(feaSet);
		
		return feaSet;
	}
	
	public static String getTargetFeatures(NamedEntity entity, String author, HashSet<String> terms, List<DependencyTriple> dtl){
		String feaSet = "";
		
		String phrase = entity.entity;
		
		int distSubj = getDistance(phrase, "subj", dtl);
		int distObj = getDistance(phrase, "obj", dtl);
		
		for(String term : terms){
			HashMap<String, String> opinterms = new HashMap<String, String>();
			opinterms.put(term, term);
			String fea = "";
			// F1 : opinion word
			fea = fea.concat(term).concat(",");
			//System.out.println("f1");
			// F2 : pos of opinion word
			fea = fea.concat(getPOS(term, dtl)).concat(",");
			//System.out.println("f2");
			// F3 & F4: pos of target and distance between a target and an opin word
			fea = fea.concat(getRel(phrase, opinterms, dtl)).concat(",");
			//System.out.println("f3&4");
			// F5: distance from target to subj
			fea = fea.concat(Integer.toString(distSubj)).concat(",");
			//System.out.println("f5");
			// F6: distance from target to obj
			fea = fea.concat(Integer.toString(distObj)).concat(",");
			//System.out.println("f6");
			// F7: has author information?
			if(author.length() >= 1)
				fea = fea.concat("true").concat(",");
			else
				fea = fea.concat("false").concat(",");
			//System.out.println("f7");
			// F8: NEs type
			fea = fea.concat(entity.type).concat(",");
			//System.out.println("f8");
			// F9~F13: common path
			fea = fea.concat(findCommonPath(phrase, term, dtl)).concat(",");
			//System.out.println("f9-13");
			// class
			fea = fea.concat("?");
			
			feaSet = feaSet.concat(fea).concat("\n");
		}
		//System.out.println(feaSet);
		
		return feaSet;
	}
	
	public static String checkAuthor(String phrase, String author){
		if(author.compareTo(phrase) == 0)
			return "true";
		
		if(phrase.toLowerCase().compareTo("i") != 0)
			return "false";
		
		if(author.length() >= 1)
			return "true";
		else
			return "false";
		
	}
	
	public static String checkPronoun(String phrase){
		String[] toks = phrase.toLowerCase().split(" ");
		if(toks.length > 1)
			return "null";
		
		if((toks[0].compareTo("i") == 0) || (toks[0].compareTo("my") == 0) || (toks[0].compareTo("me") == 0) ||
				(toks[0].compareTo("you") == 0) || (toks[0].compareTo("your") == 0) ||
				(toks[0].compareTo("he") == 0) || (toks[0].compareTo("his") == 0) || (toks[0].compareTo("him") == 0) ||
				(toks[0].compareTo("she") == 0) || (toks[0].compareTo("her") == 0) ||
				(toks[0].compareTo("they") == 0) || (toks[0].compareTo("their") == 0) || (toks[0].compareTo("them") == 0)){
			return toks[0];
		}
		
		return "null";
	}
	
	public static String findCommonPath(String phrase, String opin, List<DependencyTriple> dtl){
		String[] toks = phrase.split(" ");
		String opinPath = getPath(opin, dtl);
		if(opinPath == "")
			return "null,0.0,0.0,-1,-1";
		
		String tokPath = "";
		for(String tok : toks){
			String temp = getPath(tok, dtl);
			if(temp == "")
				continue;
			
			if(tokPath == "")
				tokPath = temp;
			else{
				String[] toks1 = temp.split("/");
				String[] toks2 = tokPath.split("/");
				
				if(toks1.length < toks2.length)
					tokPath = temp;
			}
		}
		
		//System.out.println("opinPath : " + opin + " ," + opinPath);
		//System.out.println("targetPath : " + phrase + " , " + tokPath);
		String[] oPathToks = opinPath.split("/");
		String[] tPathToks = tokPath.split("/");
		
		int max = (oPathToks.length < tPathToks.length) ? oPathToks.length : tPathToks.length;
		
		int cnt = 1;
		String pos = "";
		
		while(cnt <= max){
			if(oPathToks[oPathToks.length - cnt].compareTo(tPathToks[tPathToks.length - cnt]) == 0){
				pos = oPathToks[oPathToks.length - cnt];
			}
			else
				break;
			
			cnt++;
		}

		cnt -= 1;
		
		String temp = pos.concat(",").concat(Double.toString((double)cnt/(double)oPathToks.length)).concat(",").
				concat(Double.toString((double)cnt/(double)tPathToks.length)).concat(",").
				concat(Integer.toString(oPathToks.length - cnt)).concat(",").
				concat(Integer.toString(tPathToks.length - cnt));
		return temp;
		
	}
	
	public static String getPath(String term, List<DependencyTriple> dtl){
		HashMap<String, String> paths = new HashMap<String, String>();
		paths.put(term, "");
		
		while(true){
			HashMap<String, String> newpaths = new HashMap<String, String>();
			
			for(DependencyTriple dt : dtl){
				if(paths.containsKey(dt.dep)){
					if(dt.relation.compareTo("root") == 0){
						return paths.get(dt.dep).concat(dt.relation);
					}
					
					newpaths.put(dt.gov, paths.get(dt.dep).concat(dt.relation).concat("/"));
					
				}
			}
			
			if(newpaths.size() < 1)
				break;
			
			paths.clear();
			paths.putAll(newpaths);

		}
		
		return "";
	}
	
	
	public static String checkNEs(String phrase, List<NamedEntity> NEs, HashSet<String> checkedNEs){
		String type = "null";
		if(NEs == null)
			return type;
		
		for(NamedEntity ne : NEs){
			if(ne.entity.contains(phrase) || phrase.contains(ne.entity)){
				checkedNEs.add(ne.entity);
				type = ne.type;
			}
		}
		
		return type;
	}
	
	public static int getDistance(String phrase, String rel, List<DependencyTriple> dtl){
		HashMap<String, String> terms = new HashMap<String, String>();
		String[] toks = phrase.split(" ");
		for(String tok : toks)
			terms.put(tok, tok);
		
		int cnt = 0;
		while(cnt < 5){
			if(checkRelation(terms, rel, dtl)){
				return cnt;
			}
			
			HashMap<String, String> newterms = new HashMap<String, String>();
			Set<String> keyset = terms.keySet();
			Iterator<String> iter = keyset.iterator();
			while(iter.hasNext()){
				String term = iter.next();
				String orig = terms.get(term);
				
				HashMap<String, String> parents = getParents(term, orig, dtl);
				HashMap<String, String> children = getChildren(term, orig, dtl);
				
				newterms.putAll(parents);
				newterms.putAll(children);
			}
			
			if(newterms.size() <= 0)
				return -1;
			else{
				terms = newterms;
			}
			
			cnt++;
		}
		
		return -1;
	}
	
	
	public static boolean checkRelation(HashMap<String, String> list, String rel, List<DependencyTriple> dtl){
	//	HashMap<String, String> keyset = new HashMap<String, String>();
		
		Set<String> key = list.keySet();
		Iterator<String> iter = key.iterator();
		while(iter.hasNext()){
			String term = iter.next();
			for(DependencyTriple dt : dtl){
				if(dt.dep.contains(term) && dt.relation.contains(rel)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static String getRel(String phrase, HashMap<String, String> terms, List<DependencyTriple> dtl){
		int cnt = 0;
		
		while(cnt < 5){
			HashMap<String, String> newterms = new HashMap<String, String>();
			
			String mainTerm = findMainTerm(phrase, terms, dtl);
				
			if(mainTerm != null){
				//System.out.println("Main: " + mainTerm + " , " + cnt);
				return getPOS(mainTerm, dtl).concat(",").concat(Integer.toString(cnt));
			}
			
			Set<String> keyset = terms.keySet();
			Iterator<String> iter = keyset.iterator();
			while(iter.hasNext()){
				String term = iter.next();
				String orig = terms.get(term);
				
				HashMap<String, String> parents = getParents(term, orig, dtl);
				HashMap<String, String> children = getChildren(term, orig, dtl);

				newterms.putAll(parents);
				newterms.putAll(children);
			}
			
			if(newterms.size() <= 0)
				break;
			else{
				terms = newterms;
			}
			
			cnt++;
		}
		
		return "null,-1";
	}
	
	public static String getPOS(String term, List<DependencyTriple> dtl){
		for(DependencyTriple dt : dtl){
			if(dt.dep.contains(term)){
				return dt.relation;
			}
		}
		
		return "";
	}
	
	public static String findMainTerm(String phrase, HashMap<String, String> list, List<DependencyTriple> dtl){
		//String main = null;
		String[] toks = phrase.split(" ");
		for(String tok : toks){
			if(list.containsKey(tok)){
				return tok;
			}
		}
		
		return null;
	}
	
	public static HashMap<String, String> getParents(String term, String orig, List<DependencyTriple> dtl){
		HashMap<String, String> parents = new HashMap<String, String>();
		
		for(DependencyTriple dt : dtl){
			if(dt.dep.contains(term)){
				parents.put(dt.gov, orig);
			}
		}
		
		return parents;
	}

	public static HashMap<String, String> getChildren(String term, String orig, List<DependencyTriple> dtl){
		HashMap<String, String> children = new HashMap<String, String>();
		
		for(DependencyTriple dt : dtl){
			if(dt.gov.contains(term)){
				children.put(dt.dep, orig);
			}
		}
		
		return children;
	}
	
	public static List<DependencyTriple> getDependencyTripleList(String dep){
		List<DependencyTriple> dependencyTripleList = new ArrayList<DependencyTriple>();
		
		String tempDep = dep.replace("), ", "),~");
		String[] tripleList = tempDep.split(",~");

		for (int i=0;i<tripleList.length;i++){
			String dependencyTripleString = tripleList[i];

			Pattern triplePattern = Pattern.compile("([a-z_]+)\\((.*?)\\-[0-9]+, (.*?)\\-[0-9]+");
			Matcher tripleMatcher = triplePattern.matcher(dependencyTripleString);
			while (tripleMatcher.find()){
				String dependencyRel = tripleMatcher.group(1);
				String dependencyGov = tripleMatcher.group(2);
				String dependencyDep = tripleMatcher.group(3);

				dependencyTripleList.add(new DependencyTriple(dependencyRel, dependencyGov, dependencyDep));
				
				//System.out.println(dependencyRel + " , " + dependencyGov + " , " + dependencyDep);
			}
		}
		
		//System.out.println(toks);
		return dependencyTripleList;
	}
	
	static void createARFF(BufferedWriter writer) throws IOException{
		
	}

}

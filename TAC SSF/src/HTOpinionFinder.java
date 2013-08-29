import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HTOpinionFinder {
	public HTOpinionFinder(){
	
	}
	
	public static HashMap<String, String> process(Sentence sent, String dep, HashSet<String> terms, 
			List<NamedEntity> entities, String author, String aidx){
		
		HashMap<String, String> ht = new HashMap<String, String>();
		HashMap<String, String> opinterms = new HashMap<String, String>();
		for(String term : terms)
			opinterms.put(term, term);
		
		//System.out.println("DEP: " + dep);
		//System.out.println("Author: " + author);
		//System.out.println("Opin : " + terms);
		//System.out.println("Entities : " + entities);
		
		List<DependencyTriple> dtl = getDependencyTripleList(dep);
		
		HashMap<String, String> subjTmp = findCandidates(dtl, opinterms, "subj");
		subjTmp.putAll(findCandidates(dtl, opinterms, "dep"));
		HashMap<String, String> objTmp = findCandidates(dtl, opinterms, "obj");
		
		//System.out.println("SubjTmp: " + subjTmp);
		//System.out.println("ObjTmp: " + objTmp);
		
		HashMap<NamedEntity, String> subjCands = checkNEs(subjTmp, entities);
		HashMap<NamedEntity, String> objCands = checkNEs(objTmp, entities);
		
		//System.out.println("SubjCands: ");
		//System.out.println("ObjCnads: " + objCands);
			
		Set<NamedEntity> subjKey = subjCands.keySet();
		Iterator<NamedEntity> subjIter = subjKey.iterator();
		while(subjIter.hasNext()){
			NamedEntity subj = subjIter.next();
			//System.out.println("  " + subj.entity);
			String subjopinterm = subjCands.get(subj);
			boolean check = false;
			
			if(objCands.size() > 0){
				Set<NamedEntity> objKey = objCands.keySet();
				Iterator<NamedEntity> objIter = objKey.iterator();
				while(objIter.hasNext()){
					NamedEntity obj = objIter.next();
					String objopinterm = objCands.get(obj);
					
					if(objopinterm.compareTo(subjopinterm) == 0){
						check = true;
						String opin = objopinterm.concat("\t").
								concat(Integer.toString(sent.sent.indexOf(objopinterm)+sent.beg).concat("-").
								concat(Integer.toString(sent.sent.indexOf(objopinterm)+sent.beg+objopinterm.length())));
						String holder = subj.entity.concat("\t").
								concat(Integer.toString(subj.beg).concat("-").
								concat(Integer.toString(subj.end)));
						String target = obj.entity.concat("\t").
								concat(Integer.toString(obj.beg).concat("-").
								concat(Integer.toString(obj.end)));
						ht.put(objopinterm, opin.concat("\t").concat(holder).concat("\t").concat(target));
					}
				}
			}
			
			if((!check) && (author.length() >= 1)){
				String opin = subjopinterm.concat("\t").
						concat(Integer.toString(sent.sent.indexOf(subjopinterm)+sent.beg).concat("-").
						concat(Integer.toString(sent.sent.indexOf(subjopinterm)+sent.beg+subjopinterm.length())));
				String holder = author.concat("\t").concat(aidx);
				String target = subj.entity.concat("\t").
						concat(Integer.toString(subj.beg).concat("-").
						concat(Integer.toString(subj.end)));
				ht.put(subjopinterm, opin.concat("\t").concat(holder).concat("\t").concat(target));
			}
		}

		return ht;
	}
	

	public static HashMap<NamedEntity, String> checkNEs(HashMap<String, String> cands, List<NamedEntity> entities){
		HashMap<NamedEntity, String> candidates = new HashMap<NamedEntity, String>();
		
		Set<String> keyset = cands.keySet();
		Iterator<String> iter = keyset.iterator();
		while(iter.hasNext()){
			String term = iter.next();
			
			for(NamedEntity entity : entities){
				//System.out.println(entity.entity + " , " + term);
				String[] toks = entity.entity.split(" ");
				for(String tok : toks){
					if((tok.compareTo(term) == 0) && (tok.length() > 1)){
						candidates.put(entity, cands.get(term));
						break;
					}
				}
			}
		}
		
		return candidates;
	}
	
	public static HashMap<String, String> findCandidates(List<DependencyTriple> dtl, HashMap<String, String> terms, String rel){
		HashMap<String, String> candidates = new HashMap<String, String>();
		int cnt = 0;
		//System.out.println("terms : " + terms);
		while((cnt++) < 5){
			HashMap<String, String> newterms = new HashMap<String, String>();
			Set<String> keyset = terms.keySet();
			Iterator<String> iter = keyset.iterator();
					
			while(iter.hasNext()){
				String term = iter.next();
				String orig = terms.get(term);
				HashMap<String, String> parents = getParents(term, orig, dtl);
				HashMap<String, String> children = getChildren(term, orig, dtl);
				//System.out.println("Parent: " + parents);
				//System.out.println("Children: " + children);
				candidates.putAll(checkRelation(rel, parents, dtl));
				candidates.putAll(checkRelation(rel, children, dtl));
				newterms.putAll(parents);
				newterms.putAll(children);
			}
			
			
			if(candidates.size() > 0){
				//System.out.println("Candidates : " + candidates);
				break;
			}
			
			if(newterms.size() <= 0)
				break;
			else{
				terms = newterms;
			}
			
			//System.out.println("new terms : " + terms);
			
		}
		
		return candidates;
	}
	
	
	public static HashMap<String, String> checkRelation(String rel, HashMap<String, String> list, List<DependencyTriple> dtl){
		HashMap<String, String> keyset = new HashMap<String, String>();
		
		Set<String> key = list.keySet();
		Iterator<String> iter = key.iterator();
		while(iter.hasNext()){
			String term = iter.next();
			for(DependencyTriple dt : dtl){
				if(dt.dep.contains(term) && dt.relation.contains(rel)){
					keyset.put(term, list.get(term));
					//keyset.putAll(getChildren(term, list.get(term), dtl));
				}
			}
		}
		
		return keyset;
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
}

class DependencyTriple{
	public String gov;
	public String dep;
	public String relation;
	
	public DependencyTriple(String relation, String gov, String dep){
		this.relation = relation;
		this.gov = gov;
		this.dep = dep;
	}
}

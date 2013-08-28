import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class HTParser {
	
	static LexicalizedParser lp;
	
	public String dependencyString;
	public ArrayList<dependencyTriple> dependencyTripleList;
	
	public HTParser(){
		dependencyString = null;
		dependencyTripleList = new ArrayList<dependencyTriple>();
		lp =  LexicalizedParser.loadModel("englishPCFG.ser.gz","-maxLength", "80");
	}
	
	
	
	class dependencyTriple{
		public String gov;
		public String dep;
		public String relation;
	}
	
	
	public String getDependencyStringFromSentence(String sentence){
		
		String[] sent = sentence.split(" ");
		Tree parse = lp.apply(Sentence.toWordList(sent));
		
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		dependencyString = tdl.toString();
		
		return dependencyString;
	
	}
	
	public String getDependencyStringFromTree(Tree tree){
		
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
		Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		dependencyString = tdl.toString();
		
		return dependencyString;
	}
	
	private void getDependencyTripleList(){
		
		if (dependencyString.isEmpty()){
			return;
		}
		
		String tmpString = dependencyString.replace("), ", "),~");
		String[] dependencyTripleListTmp = tmpString.split(",~");
		
		for (int i=0;i<dependencyTripleListTmp.length;i++){
			String dependencyTripleString = dependencyTripleListTmp[i];
			
			Pattern dependencyTriplePattern = Pattern.compile("([a-z_]+)\\((.*?)\\-[0-9]+, (.*?)\\-[0-9]+");
			Matcher dependencyTripleMatcher = dependencyTriplePattern.matcher(dependencyTripleString);
			while (dependencyTripleMatcher.find()){
				
				String dependencyRelationString = dependencyTripleMatcher.group(1);
				String dependencyGovString = dependencyTripleMatcher.group(2);
				String dependencyDepString = dependencyTripleMatcher.group(3);
				
				dependencyTriple dt = new dependencyTriple();
				dt.dep = dependencyDepString;
				dt.gov = dependencyGovString;
				dt.relation = dependencyRelationString;
				dependencyTripleList.add(dt);
			}
		}
		
		return;
	}
	
	public String getTheOtherWord(String word, String relation, String option){
		if (dependencyString.isEmpty()){
			return null;
		}
		else if (dependencyTripleList.isEmpty()){
			getDependencyTripleList();
		}
		
		if (option.contains("gov")){
			for (int i=0;i<dependencyTripleList.size();i++){
				dependencyTriple dtTmp = dependencyTripleList.get(i);
				if (dtTmp.relation.contains(relation) && dtTmp.dep.contentEquals(word) ){
					return dtTmp.gov;
				}
			}
		}
		
		else if (option.contains("dep")){
			for (int i=0;i<dependencyTripleList.size();i++){
				dependencyTriple dtTmp = dependencyTripleList.get(i);
				if (dtTmp.relation.contains(relation) && dtTmp.gov.contentEquals(word) ){
					return dtTmp.dep;
				}
			}
		}
		
		
		return null;
	}
	
	public String getTheRelationBetween(String word1, String word2){
		if (dependencyString.isEmpty()){
			return null;
		}
		else if (dependencyTripleList.isEmpty()){
			getDependencyTripleList();
		}
		
		/*for (int i=0;i<dependencyTripleList.size();i++) {
			System.out.println(dependencyTripleList.get(i).dep+"~~~"+dependencyTripleList.get(i).gov+"~~~"+dependencyTripleList.get(i).relation);
		}*/
		
		ArrayList<String> visited = new ArrayList<String>();
		visited.add(word1);
		String relation = dfs(visited, word1, word2,"");
		
		return relation;
	}
	
	private String dfs(ArrayList<String> visited, String root, String goal, String relation){
		//String relationNew = "";
		//System.out.println(root+"..."+goal+"..."+relation+"...");
		
		for (int i=0;i<dependencyTripleList.size();i++){
			if (dependencyTripleList.get(i).dep.equals(root) && dependencyTripleList.get(i).gov.equals(goal)){
				return relation+"-"+dependencyTripleList.get(i).relation;
			}
			else if (dependencyTripleList.get(i).gov.equals(root) && dependencyTripleList.get(i).dep.equals(goal)){
				return relation+"-"+dependencyTripleList.get(i).relation;
			}
		}
		
		for (int i=0;i<dependencyTripleList.size();i++){
			if (dependencyTripleList.get(i).dep.equals(root) && !visited.contains(dependencyTripleList.get(i).gov) ){
				visited.add(dependencyTripleList.get(i).gov);
				String relationNew = dfs(visited, dependencyTripleList.get(i).gov, goal,relation+"-"+dependencyTripleList.get(i).relation);
				if (!relationNew.contentEquals(relation+"-"+dependencyTripleList.get(i).relation)){
					return relationNew;
				}
			}
			else if (dependencyTripleList.get(i).gov.equals(root) && !visited.contains(dependencyTripleList.get(i).dep) ){
				visited.add(dependencyTripleList.get(i).dep);
				String relationNew = dfs(visited, dependencyTripleList.get(i).dep, goal,relation+"-"+dependencyTripleList.get(i).relation);
				if (!relationNew.contentEquals(relation+"-"+dependencyTripleList.get(i).relation)){
					return relationNew;
				}
			}
		}
		
		return relation;
	}
}

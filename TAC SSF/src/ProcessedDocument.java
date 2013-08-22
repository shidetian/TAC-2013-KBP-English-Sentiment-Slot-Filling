import java.util.ArrayList;

import edu.stanford.nlp.trees.Tree;


public class ProcessedDocument {
	public String offsets;
	public String tokens;
	public ArrayList<Tree> trees;
	
	public ProcessedDocument(String o, String t, ArrayList<Tree> arrayList){
		offsets = o;
		tokens = t;
		trees = arrayList;
	}
}

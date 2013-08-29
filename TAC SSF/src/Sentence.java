import edu.stanford.nlp.trees.Tree;


public class Sentence {
	public int beg;
	public int end;
	public String sent;
	public Tree tree;
	
	public Sentence(String sent, int beg, int end, Tree tree){
		this.sent = sent;
		this.beg = beg;
		this.end = end;
		this.tree = tree;
	}
}

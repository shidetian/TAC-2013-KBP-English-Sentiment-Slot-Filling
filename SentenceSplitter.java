import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;


public class SentenceSplitter {

	private MaxentTagger tagger;
	
	public SentenceSplitter(){
		try {
			this.tagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Can not load stanford model!");
		}
	}
	
	public List<Sentence> process(String text){
		StringReader sReader = new StringReader(text);
		List<Sentence> sents = new ArrayList<Sentence>();
		
		List<List<HasWord>> sentences = MaxentTagger.tokenizeText(sReader);
		for (List<HasWord> sentence : sentences) {
			List<TaggedWord> tSentence = tagger.tagSentence(sentence);
			sents.add(new Sentence(text.substring(tSentence.get(0).beginPosition(), tSentence.get(tSentence.size()-1).endPosition()),
					               tSentence.get(0).beginPosition(), tSentence.get(tSentence.size()-1).endPosition()));
		}

		return sents;
	}
	
}

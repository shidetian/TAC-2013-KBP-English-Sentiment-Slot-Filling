import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;


public class Preprocessor {
	public static void Tokenize(String[] s){
		LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		try{
			for (String arg : s) {
				PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<CoreLabel>(new FileReader(arg), new CoreLabelTokenFactory(), "invertible");
				DocumentPreprocessor dp = new DocumentPreprocessor(arg);
				TokenizerFactory<CoreLabel> ptbtf = PTBTokenizer.factory(new CoreLabelTokenFactory(), "invertible");
				//TokenizerFactory<CoreLabel> ptbtf2 = ptbtf.newPTBTokenizerFactory(false, true);
				dp.setTokenizerFactory(ptbtf);

				FileWriter offsetFile = new FileWriter(arg+".offset");
				FileWriter tokenFile = new FileWriter(arg+".tokenized");
				BufferedWriter offsetOut = new BufferedWriter(offsetFile);
				BufferedWriter tokenOut = new BufferedWriter(tokenFile);

				for (List<HasWord> sentence : dp) {
					/*for (CoreLabel label : sentence) {
	        System.out.println(label);
	        System.out.println(label.get(CharacterOffsetBeginAnnotation.class));
	      }*/
					boolean start = true;
					for (HasWord w : sentence) {
						CoreLabel label = ptbt.next();
						//System.out.println((label+"\t"+label.get(CharacterOffsetBeginAnnotation.class)+":"+label.get(CharacterOffsetEndAnnotation.class)) );
						if (start) {
							tokenOut.write(""+label);
							start = false;
						} else {
							tokenOut.write(" "+label);
						}
						offsetOut.write(label.get(CharacterOffsetBeginAnnotation.class)+":"+label.get(CharacterOffsetEndAnnotation.class)+"\n");
					}
					//System.out.println();
					tokenOut.write("\n");
					offsetOut.write("\n");
					tokenOut.flush();
					offsetOut.flush();
					Tree output = lp.apply(sentence);
					//output.pennPrint();
					TreePrint printer = new TreePrint("wordsAndTags,penn,typedDependencies");
					printer.printTree(output);
				}
			}
			/*
	      for (CoreLabel label; ptbt.hasNext(); ) {
	      	label = ptbt.next();
	        //System.out.println(("\""+label+"\" @@ "+ "\""+label.get(OriginalTextAnnotation.class)+"\""+"||\""+label.get(BeforeAnnotation.class)+"\":\""+label.get(AfterAnnotation.class)+"\""+"||"+label.get(CharacterOffsetBeginAnnotation.class)+"-"+label.get(CharacterOffsetEndAnnotation.class)).replaceAll("\n", "/n").replaceAll("\r","/r"));
	        System.out.println((label+"\t"+label.get(CharacterOffsetBeginAnnotation.class)+":"+label.get(CharacterOffsetEndAnnotation.class)) );
	        //System.out.println("===");
	      }*/
		} catch (IOException e) {
		}
	}
}

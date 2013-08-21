import opin.main.opinionFinder;
import java.util.*;

public class OpinionFinder {
	static opinionFinder of;
	
	public OpinionFinder(){
		of = new opinionFinder();
	}
	
	public HashMap<String, String> process(String str){
		return opinionFinder.runOpinionFinder(str);
	}
}

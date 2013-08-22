import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


public class CompleteSystem {
	
	
	//Arguments: <query file>
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		ArrayList<Query> qs = QueryReader.parseFile(args[0]);
		for(Query q : qs){
			
		}
	}

}

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;


public class KBImporter {
	public static HashMap<String, Entity> kb = new HashMap<String, Entity>();
	
	public static void readKB(String KBPath) throws ParserConfigurationException, SAXException, IOException{
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		File[] files = new File(KBPath).listFiles();
		for (File f : files){
			System.out.println(f.getName());
			parser.parse(f, new KBHandler());
		}
		System.out.println(kb.size());
	}
	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		readKB("D:\\TAC_2009_KBP_Evaluation_Reference_Knowledge_Base\\data\\");
	}

}

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;


public class KBImporter {
	public static HashMap<String, Entity> kb = null;
	
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
	
	public static void initialize() throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException{
		//kb= new HashMap<String, Entity>();
		//readKB("C:\\Users\\user\\Documents\\TAC_2009_KBP_Evaluation_Reference_Knowledge_Base\\data\\");
		ObjectInputStream in = new ObjectInputStream(new FileInputStream("kb.ser"));
		kb = (HashMap<String, Entity>) in.readObject();
		in.close();
	}
	
	public static Entity getEntity(String id) throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException{
		if (kb==null){
			initialize();
		}
		return kb.get(id);
	}
	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, ClassNotFoundException {
		/*initialize();
		OutputStream file = new FileOutputStream("kb.ser");
		ObjectOutputStream os = new ObjectOutputStream(file);
		os.writeObject(kb);
		os.close();*/
		
		initialize();
		Entity temp = getEntity("E0464052");
		kb.keySet();
	}

}

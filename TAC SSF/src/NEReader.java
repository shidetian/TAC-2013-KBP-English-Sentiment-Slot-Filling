import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class NEReader {

	/**
	 * @param args
	 */
	static String path;
	static HashMap<Integer, String> NEset;
	
	public NEReader(String path){
		this.path = path;
		this.NEset = new HashMap<Integer, String>();
	}
	
	public static HashSet<String> getNEs(int beg, int end){
		HashSet<String> nes = new HashSet<String>();
		
		Set<Integer> keyset = NEset.keySet();
		Iterator<Integer> iter = keyset.iterator();
		while(iter.hasNext()){
			int idx = iter.next();
			if((idx >= beg) && (idx <= end))
				nes.add(NEset.get(idx));
		}
		
		return nes;
	}
	
	public static void parseNEs(String docID) throws SAXException, IOException, ParserConfigurationException{
		String filepath = "";
		if(docID.startsWith("bolt-eng-DF")){
			String[] toks = docID.split(".p");
			filepath = path.concat("/discussion_forums/").concat(toks[0]).concat(".sgm.apf.new");
			//System.out.println("File: " + filepath);
		}
		else
			filepath = path.concat("/newswires/").concat(docID);
		
		String XMLpath = filepath.concat(".xml");
		//System.out.println("XML: " + XMLpath);
		
		File temp = new File(XMLpath);
		if(!temp.isFile()){
			createXMLFile(filepath, XMLpath);
		}
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(XMLpath));
		doc.normalizeDocument();
		
		NEset = new HashMap<Integer, String>();
		
		//System.out.println(doc.getDocumentElement().getNodeName());
		if (!doc.getDocumentElement().getNodeName().equals("source_file")){
			System.err.println("Root element not kbpsentslotfill. Wrong file?");
			return ;
		}
		NodeList entities = doc.getElementsByTagName("entity");
		//System.out.println(entities.getLength());
		
		for(int i=0; i<entities.getLength(); i++){
			Element entity = (Element) entities.item(i);
			NodeList mentions = entity.getElementsByTagName("entity_mention");
			//System.out.println("mention: " + mentions.getLength());
			
			for(int j=0; j<mentions.getLength(); j++){
				Element mention = (Element) mentions.item(j);
				NodeList extents = mention.getElementsByTagName("extent");
				
				for(int k=0; k<extents.getLength(); k++){
					Element extent = (Element) extents.item(k);
					Element info = (Element)extent.getElementsByTagName("charseq").item(0);
					int beg = Integer.parseInt(info.getAttribute("START"));
					int end = Integer.parseInt(info.getAttribute("END"));
					String ne = info.getTextContent();
					
					NEset.put(beg, ne);
				}
			}
		}

	}
	
	public static void createXMLFile(String filepath, String XMLpath) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(filepath));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(XMLpath), "UTF-8"));
		String line;
		
		while((line=reader.readLine()) != null){
			if(line.contains("<resolution PROBABILITY")){
				int beg = line.indexOf("=");
				int end = line.indexOf(">");
				writer.write(line.substring(0, beg+1));
				writer.write("\"");
				writer.write(line.substring(beg+1, end));
				writer.write("\"");
				writer.write(line.substring(end));
				writer.newLine();
			}
			else if(line.startsWith("<!DOCTYPE")){
				
			}
			else{
				writer.write(line);
				writer.newLine();
			}
		}
		
		reader.close();
		writer.close();
	}

	/*public static void main(String[] args) throws SAXException {
		// TODO Auto-generated method stub

		try {
			parseFile("bolt-eng-DF-170-181103-8881817.sgm.apf.new", 0, 0);
			//System.out.println("# queries: " + qs.size());
		} catch (IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}

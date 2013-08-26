import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class KBHandler extends DefaultHandler {
	boolean skip = true;
	boolean inAltName = false;
	Entity current = null;
	@Override
	public void startElement(String uri, String localName,String qName, 
            Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("entity")){
			String type = attributes.getValue("type");
			if (type.equalsIgnoreCase("ORG") || type.equalsIgnoreCase("PER") || type.equalsIgnoreCase("GPE") || type.equalsIgnoreCase("UKN")){
				skip = false;
				//System.out.println("Type:"+type+"	Name:"+attributes.getValue("name"));
				current = new Entity();
				current.setId(attributes.getValue("id"));
				current.setType(attributes.getValue("type"));
				current.addAltName(attributes.getValue("name"));
			}
		}else if (qName.equalsIgnoreCase("facts")){
			
		}else if (qName.equalsIgnoreCase("fact")&&!skip){
			String factName = attributes.getValue("name");
			if (factName.contains("name")&&!factName.contains("_")){
				inAltName = true;
			}
		}
	}
	
	@Override
	public void characters(char ch[], int start, int length){
		if (inAltName){
			//System.out.print(new String(ch,start,length)+"; ");
			current.addAltName(new String(ch,start,length));
			inAltName = false;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName){
		if (qName.equalsIgnoreCase("entity")){
			//System.out.println("\n========");
			if (!skip){
				KBImporter.kb.put(current.getId(), current);
			}
			current = null;
			skip = true;
		}
		
	}
}

package thekla;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;


public class OutputCreator {
	
	OutputCreator(){
		//System.out.println("Object OutputCreator created!!");
	}
	
	public String createOutputFile(String input) {
		String[] temp = input.split("\\\\");
		String className = temp[temp.length-1];
		String packName = temp[temp.length-2];
				
		if(className.contains("java")) {
			className = className.replace(".java", ""); 
		}
		
		String output = packName + "_" + className;
		
		return output;
	}

	public void writeOutput(Optional<PackageDeclaration> pack, List<ImportDeclaration> libraries, Set<String> restLibs, HashMap<String, String> fields, HashMap<Integer, String> flowsFrom, HashMap<Integer, String> flowsTo, HashMap<Integer, String> flowsName, String fileName, String s) {		
		try {
			//fileName = "sub" + fileName;
			PrintWriter bw = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
	        //bw.println("The following information is about " + pack.get().getNameAsString());
			bw.println("The following information is about " + s);
			bw.println();	
			bw.println("The external entities are: ");
			bw.println();
			for(ImportDeclaration si : libraries) {
				bw.println(si.getNameAsString());
			}	
			for(String sl : restLibs) {
				bw.println(sl);
			}	
			bw.println();
			bw.println("The fields are: ");
			bw.println();
			if(fields.isEmpty()) {
				bw.println("There are no fields");
			}else {
				for(Entry<String, String> entry : fields.entrySet()) {
					bw.println(entry.getKey() + " with type: " + entry.getValue());
				}
			}			
			bw.println();
			
			bw.println("The flows are: ");
			bw.println();
			//for (Entry<Entry<String,String>, String> entry : flows.entrySet()) {
			for(Entry<Integer, String> entry : flowsName.entrySet()) {
				int i = entry.getKey();
				String from = flowsFrom.get(i);
				String to = flowsTo.get(i);
				bw.println(from + " " + to + " with the name: " + entry.getValue());
			}
			bw.println();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}       		
	}
	
	public void writeDFDAsXML(Set<String> externalEntities, Set<String> flows, List<String> dataStores, String fileName) {
		Element root = new Element("Entire_DFD");
        Document doc = new Document(root);
		Element title = new Element(fileName);
		Element external = new Element("External_Entities");
		for(String s: externalEntities) {
			Element entity = new Element("External_Entity");
			entity.setText(s);
			external.addContent(entity);
		}
		
		Element data = new Element("Data_Stores");
		for(String s: dataStores) {
			Element store = new Element("Data_Store");
			store.setText(s);
			data.addContent(store);
		}
		
		Element flow = new Element("Data_Flows");
		for(String s: flows) {
			String[] fParts = s.split(" ");
			String from = "";
			String to = "";
			String name = "";
			for(int i=0; i<fParts.length-1; i++) {
				if(fParts[i].equals("from")) {
					from = fParts[i+1];
				}
				if(fParts[i].equals("to")) {
					to = fParts[i+1];
				}
				if(fParts[i].equals("name:")) {
					name = fParts[i+1];
				}
			}
			
			Element dFlow = new Element("Data_Flow");
			Element f = new Element("From");
			f.setText(from);
			Element t = new Element("To");
			t.setText(to);
			Element n = new Element("Name");
			n.setText(name);
			dFlow.addContent(f);
			dFlow.addContent(t);
			dFlow.addContent(n);
			flow.addContent(dFlow);
		}
					
		title.addContent(external);
		title.addContent(data);
		title.addContent(flow);

		try {
			doc.getRootElement().addContent(title);
			//Create an XML Outputter
			XMLOutputter outputter = new XMLOutputter();
			
			//Set the format of the outputted XML File
			Format format = Format.getPrettyFormat();
			outputter.setFormat(format);
			
			//Output the XML File to standard output and the desired file
			FileWriter filew = new FileWriter("entire_dfd.xml");
			outputter.output(root, filew);
			
		} catch (IOException e){
			System.out.println(e.getMessage());
		}
	}
	
	public void writeEntireDFD(Set<String> externalEntities, Set<String> flows, List<String> dataStores, String fileName) {
		try {	
			PrintWriter bw = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));	
			bw.println("The external entities are: ");
			bw.println();
			for(String externalEntity : externalEntities) {
				bw.println(externalEntity);
			}			
			bw.println();
			bw.println("The fields are: ");
			bw.println();
			if(dataStores.isEmpty()) {
				bw.println("There are no Data Stores");
			}else {
				for(String dataStore : dataStores) {
					bw.println(dataStore);
				}
			}			
			bw.println();
			
			bw.println("The flows are: ");
			bw.println();
			for (String dataFLow : flows) {
				bw.println(dataFLow);				
			}
			bw.println();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}
}
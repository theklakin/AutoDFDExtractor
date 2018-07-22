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

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;


public class OutputCreator {
	
	OutputCreator(){
		System.out.println("Object OutputCreator created!!");
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

	public void writeOutput(Optional<PackageDeclaration> pack, List<ImportDeclaration> libraries, HashMap<String, String> fields, HashMap<Integer, String> flowsFrom, HashMap<Integer, String> flowsTo, HashMap<Integer, String> flowsName, String fileName, String s) {		
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
				//String[] temp = s.split("\\\\");
				//String[] temp2 = temp[temp.length-1].split("\\.");
				//String specification = temp2[0];
				int i = entry.getKey();
				String from = flowsFrom.get(i);
				String to = flowsTo.get(i);
				//Entry<String,String> inside = entry.getKey();
				//String direction = "";
				//String t = inside.getValue();
				//if(t.contains("from")) {
				//	direction = "to";
				//}else {
				//	direction = "from";
				//}
				bw.println(from + " " + to + " with the name: " + entry.getValue());
				//bw.println("There is a flow " + direction + " " + specification + "_" + inside.getKey() + " " + inside.getValue() + " with the name: " + entry.getValue());				
			}
			bw.println();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
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
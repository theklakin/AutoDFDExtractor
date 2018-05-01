package thekla;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;


public class OutputCreator {
	
	OutputCreator(){
		System.out.println("Object OutputCreator created!!");
	}
	
	public String createOutputFile(String input) {
		String[] temp = input.split("\\\\");
		String output = temp[temp.length-1];
		return output;
	}

	public void writeOutput(Optional<PackageDeclaration> pack, List<ImportDeclaration> libraries, HashMap<String, String> fields, HashMap< Entry<String,String>, String> flows, String fileName, String s) {		
		try {
			PrintWriter bw = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
	        //bw.println("The following information is about " + pack.get().getNameAsString());
			bw.println("The following information is about " + s);
			bw.println();	
			bw.println("The external entities are: ");
			bw.println();
			for(ImportDeclaration si : libraries) {
				bw.println(si.getName());
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
			for (Entry<Entry<String,String>, String> entry : flows.entrySet()) {
				Entry<String,String> inside = entry.getKey();
				String direction = "";
				String t = inside.getValue();
				if(t.contains("from")) {
					direction = "to";
				}else {
					direction = "from";
				}
				bw.println("There is a flow " + direction + " " + inside.getKey() + " " + inside.getValue() + " with the name: " + entry.getValue());				
			}
			bw.println();
			bw.close();
			//System.out.println("I have finished!!");		    
		} catch (IOException e) {
			e.printStackTrace();
		}       		
	}
}
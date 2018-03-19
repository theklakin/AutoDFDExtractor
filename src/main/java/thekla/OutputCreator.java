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
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.Type;


public class OutputCreator {
	
	//this class is used to fix the inputs given to each list
	
	//constructor
	OutputCreator(){
		System.out.println("Output Creator created!!");
	}
	
	public String createOutputFile(String input) {
		String[] temp = input.split("\\\\");
		String output = temp[temp.length-2];
		return output;
	}

	public void writeOutput(Optional<PackageDeclaration> pack, List<ImportDeclaration> libs, List<FieldDeclaration> storages, HashMap<SimpleName,Type> methods, String fileName, String className, HashMap<SimpleName,FieldDeclaration> storageFlows, HashMap<SimpleName, List<Object>> methodCalls) {		
		try {
			PrintWriter bw = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
	        bw.println("The following information is about " + pack.get().getNameAsString());
			bw.println();	
			bw.println("The external entities are: ");
			bw.println();
			for(ImportDeclaration s : libs) {
				bw.println(s.getName());
			}			
			bw.println();
			bw.println("The fields are: ");
			bw.println();
			for(FieldDeclaration s : storages) {
				bw.println(s);
			}			
			bw.println();
			bw.println("The methods are: ");
			bw.println();
			for (Entry<SimpleName, Type> entry : methods.entrySet()) {
				bw.println("Name: " + entry.getKey()+" Type: "+entry.getValue());
			}			
			bw.println();
			
			bw.println("The flows to storages are: ");
			bw.println();
			for (Entry<SimpleName, FieldDeclaration> entry : storageFlows.entrySet()) {
				bw.println("From: " + entry.getKey()+" To: "+entry.getValue());
			}
			bw.println();
			
			bw.println("The flows are: ");
			bw.println();
			//for (Entry<SimpleName, List<Object>> entry : methodCalls.entrySet()) {
			//    SimpleName key = entry.getKey();
			//    List<Object> value = entry.getValue();
			//    for(Object aString : value){
			//    	bw.println("From : " + key + " To : " + aString);
			//    }
			//}
			bw.println();
			bw.close();
			System.out.println("I have finished!!");		    
		} catch (IOException e) {
			e.printStackTrace();
		}       		
	}
}
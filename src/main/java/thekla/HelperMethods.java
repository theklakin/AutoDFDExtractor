package thekla;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;


public class HelperMethods {
	
	//this class is used to fix the inputs given to each list
	
	//constructor
	HelperMethods(){
		System.out.println("I am created!!");
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
		
	public void methodFlows(HashMap<SimpleName, List<Statement>> methodStmnt, List<ImportDeclaration> libraries, HashMap<Statement,String> methods, HashMap<String, String> fields) {
		HashMap<String,String> aliasing = new HashMap<>();
		HashMap<Statement,Boolean> statementExistance = new HashMap<>();
		//System.out.println("New File");
		for (Entry<SimpleName, List<Statement>> entry : methodStmnt.entrySet()) {
			System.out.println("Method: " + entry.getKey()+" Has the following statements: ");
			for(Statement st : entry.getValue()) {				
				boolean exists = checkStatement(st, methods);
				statementExistance.put(st, exists);
				//System.out.println(st + " has value: " + exists);
			}
		}
		
		//aliasing = getAlias(statementExistance, fields);
	}
	
	public HashMap<String,String> getAlias(HashMap<Statement,Boolean> statementExistance, HashMap<String, String> fields){
		HashMap<String,String> aliasing = new HashMap<>();
		for (Entry<Statement,Boolean> entry : statementExistance.entrySet()) {
			if(entry.getValue()==false) {
				String temp = entry.getKey().toString();
				String containedField = getContainedField(temp, fields);
				String containedAlias = getContainedField(temp, aliasing);
				if(containedField=="" && containedAlias=="") {
					continue;
				}else {
					
				}
			}
		}
		
		
		
		return aliasing;
	}
	
	public String getContainedField(String statement, HashMap<String, String> fields) {
		String containedField = "";
		
		for(Entry<String,String> entry : fields.entrySet()) {
			if(statement.contains(entry.getKey())) {
				containedField = entry.getKey();
				break;
			}
		}
		
		return containedField;
	}
	
	public boolean checkStatement(Statement st, HashMap<Statement,String> methods) {
		boolean exists = false;
		for(Entry<Statement,String> entry : methods.entrySet()) {
			Statement temp = entry.getKey();
			if(temp.containsWithin(st)) {
				exists = true;
				break;
			}
		}		
		return exists;
	}

}
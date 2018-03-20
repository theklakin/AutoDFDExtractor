package thekla;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.symbolsolver.javaparser.Navigator;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

public class DataFlowExtractor {

	public void methodFlows(HashMap<SimpleName, List<Statement>> methodStmnt, List<ImportDeclaration> libraries, HashMap<Statement,Entry<SimpleName,String>> methods, HashMap<String, String> fields) {
		HashMap<Statement,Boolean> statementExistance = new HashMap<>();
		HashMap<String, Entry<String,String>> flows = new HashMap<>();
		//= Name of the flow, Between Components

		for (Entry<SimpleName, List<Statement>> entry : methodStmnt.entrySet()) {
			/*per method I want to find all its flows
			 * thus i need to check all its statements for:
			 * 1) possible aliasing
			 * 2) method call
			 * methodName = entry.getKey();
			 */
			
			//we inspect each method individually
			System.out.println("Info for: " + entry.getKey()); 
			HashMap<String,String> localAlias = new HashMap<>();
			List<String> localNonSensitive = new ArrayList<>();
			
			for(Statement st : entry.getValue()) {					
				if(st.isExpressionStmt()) {
					boolean flag = false;
					String state = st.toString();
					if(state.contains("=")) {
						String[] stateParts = state.split("=");
						String alias = stateParts[0];
						String assignment = stateParts[1];
												
						String aliasedField = "";
						
						String containedField = getContainedField(assignment, fields);
						if(containedField!="") {
							aliasedField = containedField;
							flag=true;
						}else {
							String containedAlias = getContainedField(assignment, localAlias);
							if(containedAlias!="") {
								aliasedField = getAliasedField(containedAlias, localAlias);
								flag=true;
							}
						}
						
						if(flag) {
							if(alias.length()==1) {
								localAlias.put(alias, aliasedField);
							} else {
								String[] aliasParts = alias.split(" ");
								localAlias.put(aliasParts[1], aliasedField);
							}
						}else {
							//check for overwrite
							String overwrite = getContainedField(alias, localAlias);
							if(overwrite!="") {
								localNonSensitive.add(overwrite);
								localAlias.remove(overwrite);							
							}
						}
					}
					
					System.out.println("For this iteration we have:");
					for(Entry<String,String> en : localAlias.entrySet()) {
						System.out.println(en.getKey() + " " +en.getValue());
					}
				}
								
				String hasType = checkStatement(entry.getKey(), st, methods);
				if(hasType!="") {
					//it is a method call
					String[] typeParts = hasType.split("\\.");
					String entity = belongsTo(typeParts, libraries);
					//System.out.println("Possible Entity for type: " + hasType + " is: " + entity);
					flows.put(hasType, new SimpleEntry(entry.getKey().toString(), entity));
				}
				//statementExistance.put(st, exists);
				//System.out.println(st + " has value: " + exists);
			}		
		}	
		
		for (Entry<String, Entry<String,String>> entry : flows.entrySet()) {
			Entry<String,String> inside = entry.getValue();
			System.out.println("There is a flow between: " + inside.getKey() + " and: " + inside.getValue() + " with the name: " + entry.getKey());
			
		}
	}
	
	public String belongsTo(String[] type, List<ImportDeclaration> libraries) {
		String entity = "";	
		List<String> possible = new ArrayList<>();
		for(ImportDeclaration id : libraries) {		
			String lib = id.toString();
			if(lib.contains(type[0])) {
				possible.add(lib);
			}
			//System.out.println("Possible Entities: " + id.getNameAsString());
		}
		
		if(possible.size()==1) {
			entity = possible.get(0);
		}else {
			for(String st : possible) {
				if(st.contains(type[2])) {
					entity = st;
				}				
			}
		}		
		return entity;
	}
	
	public String getAliasedField(String containedAlias, HashMap<String,String> localAlias) {
		String aliasedField="";
		
		for(Entry<String,String> entry : localAlias.entrySet()) {
			String temp = entry.getKey();
			if(temp.equals(containedAlias)) {
				aliasedField = entry.getValue();
				break;
			}
		}		
		return aliasedField;
	}
	
	public String getContainedField(String statement, HashMap<String, String> fields) {
		String containedField = "";
		for(Entry<String,String> entry : fields.entrySet()) {
			String field = entry.getKey();
			if(statement.contains(field)) {
				//for now just find the first one
				containedField = entry.getKey();
				break;
			}
		}		
		return containedField;
	}
	
	public String checkStatement(SimpleName methodName, Statement st, HashMap<Statement,Entry<SimpleName,String>> methods) {
		String type = "";
		String mName = methodName.asString();
		for(Entry<Statement,Entry<SimpleName,String>> entry : methods.entrySet()) {
			Entry<SimpleName,String> information = entry.getValue();
			String name = information.getKey().asString();
			if(name.equals(mName)) {
				Statement temp = entry.getKey();
				if(temp.containsWithin(st)) {
					type = information.getValue();
					break;
				}
			}
		}		
		return type;
	}
}

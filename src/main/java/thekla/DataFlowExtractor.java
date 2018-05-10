package thekla;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.Statement;

public class DataFlowExtractor {
	
	private HashMap<String,String> dataStores;
	
	DataFlowExtractor(){
		dataStores = new HashMap<>();
	}
	
	public HashMap<String,String> getDataStores() {
		return dataStores;
	}
	
	public HashMap< Entry<String,String>, String> methodFlows2(HashMap<SimpleName, List<Statement>> methodStmnt, List<ImportDeclaration> libraries, HashMap<Entry<SimpleName,String>,Statement> methods, HashMap<String, String> fields, List<SimpleName> methodNames) {
		HashMap< Entry<String,String>, String> flows = new HashMap<>();
		HashMap< Entry<String,String>, String> finalFlows = new HashMap<>();
		HashMap<SimpleName,HashMap<Statement,String>> orderedMethodCalls = orderMethodCalls(methodNames, methods);
		//printInfo(methods);
		
		for(Entry<SimpleName,List<Statement>> entry : methodStmnt.entrySet()) {
			List<String> inputs = new ArrayList<>();
			if(!fields.isEmpty()) {
				for(Entry<String,String> f : fields.entrySet()) {
					inputs.add(f.getKey());
				}
			}
			SimpleName methodName = entry.getKey();
			HashMap<Statement,String> methodCalls = new HashMap<>();
			for(Entry<SimpleName,HashMap<Statement,String>> temp : orderedMethodCalls.entrySet()) {
				SimpleName name = temp.getKey();
				if(name.equals(methodName)) {
					methodCalls.putAll(temp.getValue());
					break;
				}
			}
			//System.out.println("Inputs");
			//for(String s : inputs) {
			//	System.out.println("input: " +s);
			//}
			
			for(Statement statement : entry.getValue()) {
				String type = checkStatement(statement, methodCalls);
				
				if(!type.equals("")) {
					//System.out.println("Statement " + statement+ " Type: " + type);
					String component = "";
					String entity="";
					boolean flag = false;
					String st = statement.toString();
					
					String id = inspectType(type);
					//System.out.println("Is an " + id);
					
					switch(id) {
					case "Input" :
						String[] inputParts = null;
						if(st.contains("=")) {
							inputParts = st.split("=");
						} else {
							inputParts = st.split("\\.");
						}
						
						if(inputParts.length>1){
							String[] tempS = inputParts[0].split(" ");
							String localVar ="";
							if(tempS.length>1) {
								localVar=tempS[tempS.length-1];
							}else {
								localVar = tempS[0];
							}
							//System.out.println("alias " + localVar);
							inputs.add(localVar);
						} 
						//component = belongsToMethod(type, methodNames);
						//if(component.equals("")) {
							component = belongsTo(type,libraries, methodNames);
						//}
						break;
					case "Output" :
						boolean contain = checkState(statement,inputs,fields);
						String alias = inputAlias(statement, inputs);
						if(!alias.equals("")) {
							inputs.add(alias);
						}
						if(contain) {
							//component = belongsToMethod(type, methodNames);
							//if(component.equals("")) {
								component = belongsTo(type,libraries, methodNames);
							//}
						}
						break;
					default : 
						System.out.println("Default state");
					}
															
					if(st.contains("=") && !type.contains("sql")) {
						flag=true;
					}
					
					//System.out.println("Component: " +component);
					
					if(!component.equals("")) {
						if(flag) {
							entity = "from " + component;
						} else entity = "to " + component;
						String flowName = "";
						
						if(id.equals("Input")) {
							//flowName = st.replaceAll(" ", "");
							flowName = st;
						}else {
							//String help = statement.toString();
							String[] stateParts = st.split("\\.");
							flowName = stateParts[stateParts.length-1];
							//System.out.println("Flow Name: " + flowName);
						}
						if(!entity.equals("")) {
							boolean flowFlag = inspectFlow(flowName);
							if(flowFlag) {
								//flowName = flowName.replaceAll(" ", "");
								flows.put(new SimpleEntry(methodName.asString(), entity), flowName);
							}						
						}						
					}									
				}else {
					String alias = inputAlias(statement, inputs);
					if(!alias.equals("")) {
						inputs.add(alias);
					}
				}
			}			
		}
		
		finalFlows.putAll(inspectFlows(flows));
		
		return finalFlows;
	}
	
	public void printInfo(HashMap<Entry<SimpleName,String>,Statement> methods) {
		System.out.println("MethodCalls");
		for(Entry<Entry<SimpleName,String>,Statement> entry : methods.entrySet()) {
			System.out.println("Method Name: " + entry.getKey().getKey() + " Statement: " + entry.getValue() + " with type: " + entry.getKey().getValue());
		}
	}
	
	public String inputAlias(Statement statement, List<String> inputs) {
		String state = statement.toString();
		String alias = "";
		boolean flag = false;
		for(String st : inputs) {
			if(state.contains(st)) {
				flag = true;
				break;
			}
		}
		
		if(state.contains("=")) {
			if(flag) {
				String[] stateParts = state.split("=");
				alias = stateParts[0];
			}
		}else if(state.contains(".") && !state.contains("=")) {
			if(flag) {
				String[] stateParts = state.split("\\.");
				alias = stateParts[0];
			}
		}
		
		String[] aliasParts = alias.split(" ");
		if(aliasParts.length>1) {
			alias = aliasParts[1];
		}else {
			alias = aliasParts[0];
		}
		return alias;
	}
	
	public HashMap<SimpleName,HashMap<Statement,String>> orderMethodCalls(List<SimpleName> methodNames, HashMap<Entry<SimpleName,String>,Statement> methods){
		HashMap<SimpleName,HashMap<Statement,String>> ordered = new HashMap<>();
		HashMap<Statement,String> methodStatements;
				
		for(SimpleName methodName : methodNames) {
			methodStatements = new HashMap<>();
			for(Entry<Entry<SimpleName,String>,Statement> methodCallEntry : methods.entrySet()) {
				SimpleName name = methodCallEntry.getKey().getKey();
				if(name.equals(methodName)) {
					methodStatements.put(methodCallEntry.getValue(), methodCallEntry.getKey().getValue());
				}
			}
			ordered.put(methodName, methodStatements);
		}		
		return ordered;
	}
	
	public String checkStatement(Statement statement, HashMap<Statement,String> methodCalls) {
		String isMethodCall = "";
		
		for(Entry<Statement,String> entry : methodCalls.entrySet()) {
			if(statement.equals((entry.getKey()))){
				isMethodCall = entry.getValue();
				break;
			}
		}		
		return isMethodCall;
	}
	
	public String inspectType(String type){
		String id = "";
		
		if(type.contains("BufferedReader") || type.contains("Scanner") || type.contains("ServletRequest") || type.contains("InputStreamReader")) {
			id = "Input";
		//} else if (type.contains("FileWriter") || type.contains("FileOutputStream") || type.contains("sql")) {
			//id = "Data Store";
		} else {
			id = "Output";
		}		
		return id;
	}

	public String belongsTo(String type, List<ImportDeclaration> libraries, List<SimpleName> methodNames) {
		String[] typeParts = type.split("\\(");
		String[] typeEntities = typeParts[0].split("\\.");
		String typeCheck = typeEntities[typeEntities.length-2]; 
		String entity = belongsToMethod(typeEntities[typeEntities.length-1], methodNames);	
		
		if(!entity.equals("")) {
			return entity;
		}
		
		for(ImportDeclaration id : libraries) {		
			String lib = id.getNameAsString();
			String lib2 = "";
			if(lib.equals("javax.servlet.http.HttpServletRequest")) {
				lib2 = "javax.servlet.ServletRequest";
			} else if(lib.equals("javax.servlet.http.HttpServletResponse")) {
				lib2 = "javax.servlet.ServletResponse";
			}
			if(type.contains(lib)) {
				entity = lib;
				break;
			}else if(!lib2.equals("")) {
				if(type.contains(lib2)) {
					entity = lib;
					break;
				}
			}
		}
		
		if(entity.equals("")) {
			entity = typeCheck;
		}
		return entity;
	}
	
	public boolean checkState(Statement statement, List<String> inputs, HashMap<String,String> fields) {
		boolean flag = false;
		String state = statement.toString();
		//System.out.println("Statement: " + statement);
		
		for(String st : inputs) {
			if(state.contains(st)) {
				flag = true;
				break;
			}
		}
		
		if(flag==false) {
			if(state.contains("except")) {
				flag=true;
			}
		}
		
		if(flag==false && !fields.isEmpty()) {
			for(Entry<String,String> entry : fields.entrySet()) {
				//System.out.println("Statement: " + state);
				if(state.contains(entry.getKey())) {
					flag = true;
					break;
				}
			}
		}
		return flag;
	}
	
	public String belongsToMethod(String type, List<SimpleName> methodNames) {
		String entity = "";
		for(SimpleName method : methodNames) {
			String name = method.asString();
			if(type.contains(name)) {
				entity = type;
			}
		}		
		return entity;
	}
	
	public HashMap< Entry<String,String>, String> inspectFlows(HashMap< Entry<String,String>, String> flows){
		HashMap< Entry<String,String>, String> flow = new HashMap<>();
		for(Entry<Entry<String,String>, String> entry : flows.entrySet()) {
			String entity = entry.getKey().getValue();
			String flowName = entry.getValue();
			if(entity.contains("sql")) {
				String[] entityParts = entity.split(" ");
				entity = entityParts[0] + " database";
			}
			if(flowName.contains("except")) {
				int index = flowName.indexOf("except");
				flowName = flowName.substring(index);
			}
			if(!flowName.contains("close")) {
				flow.put(new SimpleEntry(entry.getKey().getKey(), entity), flowName);
			}
		}		
		return flow;
	}
	
	public boolean inspectFlow(String flowName) {
		boolean flag = false;
		if(!flowName.contains("close")) {
			flag=true;
		}
		return flag;
	}
	
}

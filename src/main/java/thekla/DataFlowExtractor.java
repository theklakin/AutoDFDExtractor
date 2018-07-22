package thekla;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.Statement;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;


public class DataFlowExtractor {
	
	private List<InfoContainer> allDFDInfo = new ArrayList<>();
	private HashMap<String,String> dataStores;
	private List<ImportDeclaration> externalEntities;
	private List<String> methodNames;
	private HashMap<Entry<String,String>, Entry<String,String>> methodCallTrace;
	private HashMap<String,HashMap<String,String>> allAlias;
	private HashMap<String,String> methodAlias;
	private HashMap<String,String> subDFD= new HashMap<>();
	//private HashMap<Entry<String,String>, String> flows;
	private HashMap<Integer, String> flowsFrom;
	private HashMap<Integer, String> flowsTo;
	private HashMap<Integer, String> flowsName;
	private Integer index;
	private String description;
	
	DataFlowExtractor(List<InfoContainer> allDFDInfo, String s){
		description = s;
		this.allDFDInfo = allDFDInfo;
		dataStores = new HashMap<>();
		externalEntities = allDFDInfo.get(0).getLibraries();
		methodNames = new ArrayList<>();
		methodCallTrace = new HashMap<>();
		allAlias = new HashMap<>();
		flowsFrom = new HashMap<>();
		flowsTo = new HashMap<>();
		flowsName = new HashMap<>();
		index = 0;
		
		for(InfoContainer dfd : allDFDInfo) {
			methodNames.add(dfd.getMethodName());
		}
	}
	
	public HashMap<Integer,String> getFlowsFrom() {
		return flowsFrom;
	}
	
	public HashMap<Integer,String> getFlowsTo() {
		return flowsTo;
	}
	
	public HashMap<Integer,String> getFlowsName() {
		return flowsName;
	}
	
	public HashMap<String,String> getDataStores() {
		return dataStores;
	}
	
	public HashMap<String,HashMap<String,String>> getAllAlias(){
		return allAlias;
	}
	
	public HashMap<Entry<String,String>, Entry<String,String>> getTrace() {
		return methodCallTrace;
	}
	
	public List<ImportDeclaration> getExternalEntities() {
		return externalEntities;
	}
	
	public List<String> getAllMethodNames() {
		return methodNames;
	}
	
	public HashMap<String,String> getSubDFDs() {
		return subDFD;
	}
		
	public void parseDFDInfo() {		
		for(InfoContainer dfd : allDFDInfo) {
			List<String> inputs = new ArrayList<>();
			List<Statement> methodStatements = new ArrayList<>();
			HashMap<Integer, Entry<Statement,String>> methodCalls = new HashMap<>();
			
			inputs = dfd.getInputs();
			methodStatements = dfd.getMethodStmnt();
			methodCalls = dfd.getMethodCalls();			
			String methodName = dfd.getMethodName();
			String pack = dfd.getPack().get().getNameAsString();
			String className = dfd.getClassName();
			subDFD.put(className, pack);
			//flows.putAll(methodFlows(methodName, inputs, methodStatements, methodCalls));
			methodFlows(methodName, inputs, methodStatements, methodCalls);
		}	
	}
	
	private void printFounInfo(List<String> inputs, List<String> methodStatements, HashMap<String,String> methodCalls) {
		//System.out.println("Libraries:");
		//for(String s : externalEntities) {
		//	System.out.println(s);
		//}
		
		System.out.println("Inputs:");
		for(String s : inputs) {
			System.out.println(s);
		}
		
		System.out.println("Statements:");
		for(String s : methodStatements) {
			System.out.println(s);
		}
		
		System.out.println("Method Calls:");
		for(Entry<String,String> entry : methodCalls.entrySet()) {
			System.out.println("Statement: " + entry.getKey() + " has type: " + entry.getValue());
		}
	}
	
	private void methodFlows(String methodName, List<String> inputs, List<Statement> methodStatements, HashMap<Integer, Entry<Statement,String>> methodCalls) {
		//HashMap<Entry<String,String>, String> flows = new HashMap<>();
		//HashMap<Entry<String,String>, String> finalFlows = new HashMap<>();
		methodAlias = new HashMap<>();
		//System.out.println("Method: " + methodName);
		//printFounInfo(inputs, methodStatements, methodCalls);
		
		for(Statement statement : methodStatements) {	
			String state = statement.toString();
			String type = checkStatement(state, methodCalls);
			if(!type.equals("")) {
				//System.out.println("statement " + statement);
				//System.out.println("type " + type);
				traceMethod(state, type, methodName, inputs);
				String component = "";
				String entity="";
				boolean flag = false;					
				String id = inspectType(type);
					
				switch(id) {
				case "Input" :
					String[] inputParts = null;
					if(state.contains("=")) {
						inputParts = state.split("=");
					} else {
						inputParts = state.split("\\.");
					}
						
					if(inputParts.length>1){
						String[] tempS = inputParts[0].split(" ");
						String localVar ="";
						if(tempS.length>1) {
							localVar=tempS[tempS.length-1];
						}else {
							localVar = tempS[0];
						}
						inputs.add(localVar);
					} 
					component = belongsTo(type,externalEntities);
					//System.out.println("Component= " + component);
					break;
				case "Output" :
					boolean contain = checkState(state,inputs);
					String alias = inputAlias(state, inputs);
					if(!alias.equals("")) {
						inputs.add(alias);
					}
					if(contain) {
						//component = belongsToMethod(statement, type, methodName);							
						if(component.equals("")) {
							component = belongsTo(type,externalEntities);
						}
					}
					break;
				default : 
					System.out.println("Default state");
				}
				
				boolean out = type.contains("sql") || type.contains("File");
															
				if(state.contains("=") && !out) {
					flag=true;
				}
										
				if(!component.equals("")) {
					if(flag) {
						entity = "from " + component;
					} else entity = "to " + component;
					String flowName = "";
					//System.out.println("entity: " + entity);
						
					if(id.equals("Input")) {
						flowName = state;
					}else {
						String[] stateParts = state.split("\\.");
						if(stateParts.length>2) {
							for(int i=1; i<stateParts.length; i++) {
								flowName = flowName+stateParts[i];
							}
						}else {
							flowName = stateParts[stateParts.length-1];
						}
					}
					//System.out.println("flow name: " + flowName);
					if(!entity.equals("")) {
						boolean flowFlag = inspectFlow(flowName);
						if(flowFlag) {
							//System.out.println("method name=" + methodName);
							entity = inspectFlows(entity);
							flowName = inspectFlowName(flowName);
							if(!flowName.equals("")) {
								index++;
								flowsName.put(index, flowName);
								if(entity.contains("from")) {
									flowsFrom.put(index, entity);
									flowsTo.put(index, "to " + description + "_" +methodName);
								}else {
									flowsTo.put(index, entity);
									flowsFrom.put(index, "from " + description + "_" +methodName);
								}
							}
							//flows.put(new SimpleEntry(methodName, entity), flowName);
						}						
					}						
				}									
			}else {
				String alias = inputAlias(state, inputs);
				if(!alias.equals("")) {
					inputs.add(alias);
				}
			}
		}					
		//finalFlows.putAll(inspectFlows(flows));
		allAlias.put(methodName, methodAlias);		
		//return finalFlows;
	}
	
	private String inputAlias(String statement, List<String> inputs) {
		String alias = "";
		String in = "";
		boolean flag = false;
		for(String st : inputs) {
			if(statement.contains(st)) {
				in = st;
				flag = true;
				break;
			}
		}
		
		if(statement.contains("=")) {
			if(flag) {
				String[] stateParts = statement.split("=");
				alias = stateParts[0];
			}
		}else if(statement.contains(".") && !statement.contains("=")) {
			if(flag) {
				String[] stateParts = statement.split("\\.");
				alias = stateParts[0];
			}
		}
		
		String[] aliasParts = alias.split(" ");
		if(aliasParts.length>1) {
			alias = aliasParts[1];
		}else {
			alias = aliasParts[0];
		}
		methodAlias.put(alias, in);		
		return alias;
	}
	
	private String checkStatement(String statement, HashMap<Integer, Entry<Statement,String>> methodCalls) {
		String isMethodCall = "";
		for(Entry<Integer, Entry<Statement,String>> entry : methodCalls.entrySet()) {
			Entry<Statement,String> mCall = entry.getValue();
			String state = mCall.getKey().toString();
			if(statement.equals((state))){
				isMethodCall = mCall.getValue();
				break;
			}
		}		
		return isMethodCall;
	}
	
	private String inspectType(String type){
		String id = "";
		
		if(type.contains("BufferedReader") || type.contains("Scanner") || type.contains("ServletRequest") || type.contains("InputStreamReader") || type.contains("FileInputStream")) {
			id = "Input";
		} else {
			id = "Output";
		}		
		return id;
	}

	private String belongsTo(String type, List<ImportDeclaration> libraries) {
		String[] typeParts = type.split("\\(");
		String[] typeEntities = typeParts[0].split("\\.");
		String typeCheck = typeEntities[typeEntities.length-1]; 
		String description = typeEntities[typeEntities.length-2];

		String entity = "";
		
		for(ImportDeclaration library : libraries) {	
			String lib = library.getNameAsString();
			String lib2 = "";
			if(lib.equals("javax.servlet.http.HttpServletRequest")) {
				lib2 = "javax.servlet.ServletRequest";
			} else if(lib.equals("javax.servlet.http.HttpServletResponse")) {
				lib2 = "javax.servlet.ServletResponse";
			} else if(description.contains("printStream")) {
				lib2 = description;
			}
			if(typeParts[0].contains(lib)) {
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
			//entity = typeCheck;
			entity = description + "_" +typeCheck;
		}
		return entity;
	}
	
	private boolean checkState(String statement, List<String> inputs) {
		boolean flag = false;
		
		for(String st : inputs) {
			if(statement.contains(st)) {
				flag = true;
				break;
			}
		}
		
		if(flag==false) {
			if(statement.contains("except")) {
				flag=true;
			}
		}
		return flag;
	}
	
	private void traceMethod(String statement, String type, String methodName, List<String> inputs) {
		//find a way to aliasing the current data used with the parameters of the other method
		String entity = "";
		for(String method : methodNames) {
			if(type.contains(method)) {
				entity = method;
				String al = "";
				String[] stateParts = statement.split("\\(");
				String name = stateParts[1];
				name = name.replace(")", "");
				name = name.replace(";", "");
				int index = 0;
				if(name.contains(",")) {
					String[] nameParts = name.split("\\,");
					for(String s : nameParts) {
						boolean f = checkState(s,inputs);
						if(f==false) {
							index++;
						}else {
							break;
						}
					}
					//locate which parameter is used and to which index and keep that
				}
				List<Parameter> parameters = getSpecificParam(method);
				if(index==0) {
					al = "empty";
				}else {
					al = parameters.get(index).getNameAsString();
				}
				methodCallTrace.put(new SimpleEntry(methodName, entity), new SimpleEntry(name, al));				
			}
		}		
	}
	
	private List<Parameter> getSpecificParam(String methodName){
		List<Parameter> specificParam = new ArrayList<>();
		for(InfoContainer dfd : allDFDInfo) {
			String cName = dfd.getMethodName();
			if(methodName.equals(cName)) {
				specificParam = dfd.getParameters();
				break;
			}
		}
		return specificParam;
	}
	
	private String inspectFlowName(String flowName) {
		if(flowName.contains("except")) {
			int index = flowName.indexOf("except");
			flowName = flowName.substring(index);
		}
		if(flowName.contains("close")) {
			flowName = "";
		}
		return flowName;
	}

	
	private String inspectFlows(String entity){
		//HashMap< Entry<String,String>, String> flow = new HashMap<>();
		//for(Entry<Entry<String,String>, String> entry : flows.entrySet()) {
			//String entity = entry.getKey().getValue();
			//String flowName = entry.getValue();
			if(entity.contains("sql")) {
				String[] entityParts = entity.split(" ");
				entity = entityParts[0] + " database";
			}
			if(entity.contains("File")) {
				String[] entityParts = entity.split(" ");
				entity = entityParts[0] + " file";
			}
		//}		
		return entity;
	}
	
	private boolean inspectFlow(String flowName) {
		boolean flag = false;
		if(!flowName.contains("close")) {
			flag=true;
		}
		if(flowName.contains("(")) {
			//System.out.println("flow name: " + flowName);
			String[] sub1 = flowName.split("\\(");
			String[] sub2 = sub1[1].split("\\)");

			if(sub2[0].equals("")) {
				flag=false;
			}
		}
		if(flowName.contains("readLine")) {
			flag=true;
		}
		return flag;
	}
	
}

package thekla;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparser.Navigator;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;


public class InfoExtractor {
	
	private Optional<PackageDeclaration> pack;
	private List<InfoContainer> allDFDInfo;
	private String className;
	private Integer index;
	private List<String> methodNamesA;
	
	InfoExtractor(){
		System.out.println("InfoExtracor created");
	}
		
	InfoExtractor(String className){
		index = 0;
		this.className = className;
		allDFDInfo = new ArrayList<>();
		methodNamesA = new ArrayList<>();
	}
	
	public Optional<PackageDeclaration> getPackage(){
		return pack;
	}
	
	public List<InfoContainer> getDFDs(){
		return allDFDInfo;
	}
	
	public void information(CompilationUnit cu, CombinedTypeSolver typeSolver) {
        List<ImportDeclaration> libraries = new ArrayList<>();
        HashMap<SimpleName,List<Parameter>> allMethodNames = new HashMap<>();
        //this will return the name of the methods that exist in that source file
		HashMap<SimpleName,List<Parameter>> methodNames = new HashMap<>();
		VoidVisitor<HashMap<SimpleName, List<Parameter>>> methodNameCollector = new MethodNameCollector();
		methodNameCollector.visit(cu, methodNames);
		allMethodNames.putAll(methodNames);
		
		/*for(Entry<SimpleName,List<Parameter>> k : allMethodNames.entrySet()) {
			System.out.println(k.getKey());
			methodNamesA.add(k.getKey().asString());
		}*/
		
		//this will find all the persistent storages
		HashMap<FieldDeclaration, String> fields = new HashMap<>();
		List<FieldDeclaration> fd = Navigator.findAllNodesOfGivenClass(cu, FieldDeclaration.class);
		for(FieldDeclaration f : fd) {
			ResolvedType fit = JavaParserFacade.get(typeSolver).convertToUsage(f.getVariables().get(0).getType(), f);
			if(f.hasComment()) {
				f.removeComment();
			}
			if(fit.isPrimitive()) {
				fields.put(f, fit.describe());
			}else {
				fields.put(f, fit.asReferenceType().getQualifiedName());
			}
		}
		
		HashMap<String, String> fields2 = new HashMap<>();				
		for (Entry<FieldDeclaration, String> entry : fields.entrySet()) {
			FieldDeclaration f = entry.getKey();
			String ff = f.getVariables().get(0).getName().toString();
			fields2.put(ff, entry.getValue());			
		}
			
        //libraries contains the external entities
        libraries = cu.getImports(); 
        		        
        //returns the package
        pack = cu.getPackageDeclaration();
        
        HashMap<SimpleName, List<Statement>> methodStmnt = new HashMap<>();
        List<TypeDeclaration<?>> field = cu.getTypes();	        
        for(TypeDeclaration<?> td : field) {
        	//this is to return all the method calls of a method, i.e. what flows it uses
        	List<MethodDeclaration> tempMethod = td.getMethods();
        	for(MethodDeclaration tdm : tempMethod) {	
        		//get name of method as well as the method statements
        		List<Statement> parentStatement =  Navigator.findAllNodesOfGivenClass(tdm, Statement.class);
        		List<Statement> finalStatement = new ArrayList<>();
        		
				for(Statement st : parentStatement) {
					if(st.isExpressionStmt()) {
						if(st.hasComment()) {
							st.removeComment();
						}
						finalStatement.add(st);
					}
				}
        		methodStmnt.put(tdm.getName(), finalStatement);
        	}
        }
        
		HashMap<Entry<SimpleName,Integer>,Entry<String,Statement>> methods = new HashMap<>();
		int index = 0;
		for (Entry<SimpleName, List<Statement>> entry : methodStmnt.entrySet()) {
			for(Statement st : entry.getValue()) {
				List<MethodCallExpr> methodCalls = Navigator.findAllNodesOfGivenClass(st, MethodCallExpr.class);
				for(MethodCallExpr mc : methodCalls) {
					index++;
					methods.put(new SimpleEntry(entry.getKey(),index), new SimpleEntry(JavaParserFacade.get(typeSolver).solve(mc).getCorrespondingDeclaration().getQualifiedSignature(),st));
				}
				//methodCalls.forEach(mc-> methods.put(new SimpleEntry(entry.getKey(),JavaParserFacade.get(typeSolver).solve(mc).getCorrespondingDeclaration().getQualifiedSignature()),st));
			}
		}
		
		HashMap<SimpleName,HashMap<Statement,String>> orderedMethodCalls = orderMethodCalls(methodNames, methods);
		inform(libraries, fields2, allMethodNames, methodStmnt, orderedMethodCalls);
	}
	
	private void inform(List<ImportDeclaration> libraries, HashMap<String, String> fields, HashMap<SimpleName,List<Parameter>> allMethodNames,  HashMap<SimpleName, List<Statement>> methodStmnt, HashMap<SimpleName,HashMap<Statement,String>> orderedMethodCalls){
		for(Entry<SimpleName,List<Parameter>> entry : allMethodNames.entrySet()) {
			SimpleName name = entry.getKey();
			methodNamesA.add(className + "_" + name.asString());
			InfoContainer dfd = new InfoContainer();
			dfd.setMethodName(entry.getKey().asString());
			dfd.setParameters(entry.getValue());
			dfd.setLibraries(libraries);
			dfd.setFields(fields);
			for(Entry<SimpleName, List<Statement>> en : methodStmnt.entrySet()) {
				SimpleName n = en.getKey();
				if(n.equals(name)) {
					dfd.setMethodStmnt(en.getValue());
				}
			}
			
			for(Entry<SimpleName, HashMap<Statement,String>> en : orderedMethodCalls.entrySet()) {
				SimpleName n = en.getKey();
				if(n.equals(name)) {
					HashMap<Integer, Entry<Statement,String>> methodCalls =  new HashMap<>();
					HashMap<Statement,String> mCalls = en.getValue();
					for(Entry<Statement,String> m : mCalls.entrySet()) {
						index++;
						methodCalls.put(index, new SimpleEntry(m.getKey(),m.getValue()));
					}
					dfd.setMethodCalls(methodCalls);
				}
			}
			List<String> inputs = new ArrayList<>();
			for(Parameter param : entry.getValue()) {
				inputs.add(param.getNameAsString());
			}
			
			for(Entry<String,String> fEntry: fields.entrySet()) {
				inputs.add(fEntry.getKey());
			}
			
			dfd.setInputs(inputs);
			dfd.setPack(pack);
			dfd.setClassName(className);
			allDFDInfo.add(dfd);
		}
	}
	
	/*public void createInfoFile(String file, List<ImportDeclaration> libraries, HashMap<String, String> fields, HashMap<SimpleName,List<Parameter>> allMethodNames,  HashMap<SimpleName, List<Statement>> methodStmnt, HashMap<SimpleName,HashMap<Statement,String>> orderedMethodCalls) {
		try {
			String fileName = "Info" + file;
			PrintWriter bw = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
			bw.println("The following information is about " + file);
			bw.println();	
			bw.println("The methods of this source file are:");
			for(Entry<SimpleName,List<Parameter>> entry : allMethodNames.entrySet()) {
				bw.println(entry.getKey());
			}
			bw.println();			
			bw.println("The libraries are: ");
			for(ImportDeclaration lib : libraries) {
				bw.println(lib.getName());
			}			
			bw.println();
			bw.println("The fields are: ");
			if(fields.isEmpty()) {
				bw.println("There are no fields");
			}else {
				for(Entry<String, String> entry : fields.entrySet()) {
					bw.println("Name: " + entry.getKey() + " type: " + entry.getValue());
				}
			}			
			bw.println();
			
			bw.println("Information for all methods: ");
			bw.println();
			for (Entry<SimpleName,List<Parameter>> entry : allMethodNames.entrySet()) {
				SimpleName name = entry.getKey();
				bw.println("Method Name: " + name + " parameters: " + entry.getValue());
				bw.println();
				bw.println("has statements:");
				for(Entry<SimpleName, List<Statement>> statementEntry :methodStmnt.entrySet()) {
					SimpleName mName = statementEntry.getKey();
					if(mName.equals(name)) {
						for(Statement state : statementEntry.getValue()) {
							bw.println(state);
						}
					}
				}
				bw.println();
				bw.println("has method calls:");
				for(Entry<SimpleName,HashMap<Statement,String>> methodCallEntry :orderedMethodCalls.entrySet()) {
					SimpleName mName = methodCallEntry.getKey();
					if(mName.equals(name)) {
						for(Entry<Statement,String> state: methodCallEntry.getValue().entrySet()) {
							bw.println("Statement: " + state.getKey() + " type: " + state.getValue());
						}
					}
				}
				bw.println();
				bw.println("method end");
				bw.println();
			}
			bw.println();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}       	
	}*/
	
	private HashMap<SimpleName,HashMap<Statement,String>> orderMethodCalls(HashMap<SimpleName,List<Parameter>> methodNames, HashMap<Entry<SimpleName,Integer>,Entry<String,Statement>> methods){
		HashMap<SimpleName,HashMap<Statement,String>> ordered = new HashMap<>();
		HashMap<Statement,String> methodStatements;
				
		for(Entry<SimpleName,List<Parameter>> entry : methodNames.entrySet()) {
			SimpleName methodName = entry.getKey();
			methodStatements = new HashMap<>();
			for(Entry<Entry<SimpleName,Integer>, Entry<String,Statement>> methodCallEntry : methods.entrySet()) {
				SimpleName name = methodCallEntry.getKey().getKey();
				if(name.equals(methodName)) {
					methodStatements.put(methodCallEntry.getValue().getValue(), methodCallEntry.getValue().getKey());
				}
			}
			ordered.put(methodName, methodStatements);
		}		
		return ordered;
	}
	
	public List<String> getMethodNames() {
		return methodNamesA;
	}

	private static class MethodNameCollector extends VoidVisitorAdapter<HashMap<SimpleName,List<Parameter>>> {
		
		public void visit(MethodDeclaration md, HashMap<SimpleName,List<Parameter>> collector) {
			super.visit(md, collector);
			collector.put(md.getName(),md.getParameters());		
		}
	}
}

package thekla;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Map.Entry;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
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
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;


public class App 
{
	
	public static void main( String[] args )
    {		
		//first we need to ask the user to insert the directory of the code		
		System.out.println("Please insert the path to the src file under inspection.");
		//and read his/her input
		Scanner scanner = new Scanner (System.in);
		String name = scanner.next();	
		System.out.println("Please insert the path to the src file dependency.");
		//scanner = new Scanner (System.in);
		//String dependency = scanner.next();	
		scanner.close();
		//create a file that keeps all the relevant information
        OutputCreator output = new OutputCreator();
		FileTraversal fileTraverse = new FileTraversal();
        
		String fileName = output.createOutputFile(name);
		
		List<String> files = new ArrayList<>();
		files = fileTraverse.getFiles(name);
		
		// creates an input stream for the file to be parsed
        FileInputStream in;
		//InputStream in = null;
        List<ImportDeclaration> libraries = null;
        List<Optional<PackageDeclaration>> packages = new ArrayList<>();
        List<SimpleName> allMethodNames = new ArrayList<>();
        
		try {
			for(String s : files) {
				CombinedTypeSolver typeSolver = new CombinedTypeSolver();
				typeSolver.add(new ReflectionTypeSolver());	
				typeSolver.add(new JavaParserTypeSolver(new File("C:\\Users\\thekl\\Desktop\\securibench\\src\\")));
				//typeSolver.add(new JavaParserTypeSolver(new File("C:\\Users\\thekl\\Desktop\\OnlineProjectEvaluator-CODE\\src\\")));
				//typeSolver.add(new JavaParserTypeSolver(new File("C:\\Users\\thekl\\Desktop\\myBenchmark\\src\\")));
				//typeSolver.add(new JavaParserTypeSolver(new File(name)));
				
				in = new FileInputStream(s);
		        // parse the file
		        CompilationUnit cu = JavaParser.parse(in);
		        
		        //this will return the name of the methods that exist in that source file
				List<SimpleName> methodNames = new ArrayList<>();
				VoidVisitor<List<SimpleName>> methodNameCollector = new MethodNameCollector();
				methodNameCollector.visit(cu, methodNames);
				allMethodNames.addAll(methodNames);
				
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
				libraries = new ArrayList<>();
		        libraries = cu.getImports(); 
		        		        
		        //returns the package
		        Optional<PackageDeclaration> pack = cu.getPackageDeclaration();
		        packages.add(pack);
		        
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
		        
				HashMap<Entry<SimpleName,String>,Statement> methods = new HashMap<>();
		        //HashMap<SimpleName,HashMap<Statement,String>> methods = new HashMap<>();

				for (Entry<SimpleName, List<Statement>> entry : methodStmnt.entrySet()) {
					//HashMap<Statement,String> mCallsPerMethod = new HashMap<>();
					//System.out.println("Method: " +entry.getKey());
					for(Statement st : entry.getValue()) {
						List<MethodCallExpr> methodCalls = Navigator.findAllNodesOfGivenClass(st, MethodCallExpr.class);
						//methodCalls.forEach(mc->System.out.println("Statement: " +st + " has type: " + JavaParserFacade.get(typeSolver).solve(mc).getCorrespondingDeclaration().getQualifiedSignature()));
						//methodCalls.forEach(mc-> mCallsPerMethod.put(st,JavaParserFacade.get(typeSolver).solve(mc).getCorrespondingDeclaration().getQualifiedSignature()));
						methodCalls.forEach(mc-> methods.put(new SimpleEntry(entry.getKey(),JavaParserFacade.get(typeSolver).solve(mc).getCorrespondingDeclaration().getQualifiedSignature()),st));
					}
					//methods.put(entry.getKey(), mCallsPerMethod);
				}
				
		        //now i need to get flows
				DataFlowExtractor dataFlow = new DataFlowExtractor();
				HashMap< Entry<String,String>, String> flows = new HashMap<>();
				flows = dataFlow.methodFlows2(methodStmnt, libraries, methods, fields2, methodNames);
				HashMap<String,String> dataStores = dataFlow.getDataStores();
				output.writeOutput(pack, libraries, dataStores, flows, fileName,s);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		DotFileCreator dotFileCreator = new DotFileCreator();
		EntireDFDExtractor dfd = new EntireDFDExtractor();
		dfd.extractDFD(allMethodNames, packages,fileName);
		dotFileCreator.createVisualFile(fileName);
		System.out.println("I have finished");
    }    
	
	private static class MethodNameCollector extends VoidVisitorAdapter<List<SimpleName>> {
		
		@Override
		public void visit(MethodDeclaration md, List<SimpleName> collector) {
			super.visit(md, collector);
			//BlockStmt body = md.getBody().get();
			collector.add(md.getName());
		}
	}
	
}
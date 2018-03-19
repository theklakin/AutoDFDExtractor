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
import com.github.javaparser.ast.stmt.BlockStmt;
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
		scanner.close();
		//create a file that keeps all the relevant information
        OutputCreator inF = new OutputCreator();
		FileTraversal ftr = new FileTraversal();
        
		String fileName = inF.createOutputFile(name);
		
		List<String> files = new ArrayList<>();
		files = ftr.getFiles(name);
		
		// creates an input stream for the file to be parsed
        FileInputStream in;
		//InputStream in = null;
        
		try {
			for(String s : files) {				
				CombinedTypeSolver typeSolver = new CombinedTypeSolver();
				typeSolver.add(new ReflectionTypeSolver());	
				typeSolver.add(new JavaParserTypeSolver(new File("C:\\Users\\thekl\\Desktop\\securibench\\src\\")));
				
				in = new FileInputStream(s);
		        // parse the file
		        CompilationUnit cu = JavaParser.parse(in);
		        
		        //this will return the name of the methods that exist in that source file
				HashMap<SimpleName, BlockStmt> methodNames = new HashMap<>();
				VoidVisitor<HashMap<SimpleName, BlockStmt>> methodNameCollector = new MethodNameCollector();
				methodNameCollector.visit(cu, methodNames);
				
				//this will find all the persistent storages
				HashMap<FieldDeclaration, String> fields = new HashMap<>();
				List<FieldDeclaration> fd = Navigator.findAllNodesOfGivenClass(cu, FieldDeclaration.class);
				for(FieldDeclaration f : fd) {
					ResolvedType fit = JavaParserFacade.get(typeSolver).convertToUsage(f.getVariables().get(0).getType(), f);
					fields.put(f, fit.asReferenceType().getQualifiedName());
				}
								
				HashMap<String, String> fields2 = new HashMap<>();
				for (Entry<FieldDeclaration, String> entry : fields.entrySet()) {
					FieldDeclaration f = entry.getKey();
					String ff = f.getVariables().get(0).getName().toString();
					fields2.put(ff, entry.getValue());			
				}
				
				
				HashMap<MethodCallExpr,String> methods = new HashMap<>();
				List<MethodCallExpr> methodCalls = Navigator.findAllNodesOfGivenClass(cu, MethodCallExpr.class);
				methodCalls.forEach(mc-> methods.put(mc, JavaParserFacade.get(typeSolver).solve(mc).getCorrespondingDeclaration().getQualifiedSignature()));
				
				//for (Entry<MethodCallExpr,String> entry : methods.entrySet()) {
				//	System.out.println("Method: " + entry.getKey()+" Method Type: "+entry.getValue());
				//}
				
		        //libraries contains the external entities
				List<ImportDeclaration> libraries = new ArrayList<>();
		        libraries = cu.getImports(); 
		        
				//HashMap<SimpleName,FieldDeclaration> storageFlows = new HashMap<>();
		        
		        //returns the package
		        Optional<PackageDeclaration> pack = cu.getPackageDeclaration();
		        HashMap<SimpleName, List<Statement>> methodStmnt = new HashMap<>();
		        List<TypeDeclaration<?>> field = cu.getTypes();	        
		        for(TypeDeclaration<?> td : field) {
		        	//this is to return all the method calls of a method, i.e. what flows it uses
		        	List<MethodDeclaration> tempMethod = td.getMethods();
		        	for(MethodDeclaration tdm : tempMethod) {	
		        		//get name of method as well as the method statements
		        		List<Statement> methodStatement =  tdm.getBody().get().getStatements();
		        		methodStmnt.put(tdm.getName(), methodStatement);
		        	}
		        }
		        
				HashMap<Statement,Entry<SimpleName,String>> methods2 = new HashMap<>();

				for (Entry<SimpleName, List<Statement>> entry : methodStmnt.entrySet()) {
					//System.out.println("Method: " + entry.getKey()+" Has the following statements: ");
					for(Statement st : entry.getValue()) {
						List<MethodCallExpr> methodCalls2 = Navigator.findAllNodesOfGivenClass(st, MethodCallExpr.class);
						methodCalls2.forEach(mc-> methods2.put(st , new SimpleEntry(entry.getKey(),JavaParserFacade.get(typeSolver).solve(mc).getCorrespondingDeclaration().getQualifiedSignature())));
					}
				}
				
				//for(Entry<Statement,Entry<SimpleName,String>> entry : methods2.entrySet()) {
				//	System.out.println("The statement: " + entry.getKey() + " contains the following information:");
				//	Entry<SimpleName,String> help = entry.getValue();
				//    System.out.println("Belongs to: " + help.getKey() + " and has type: " + help.getValue());
			//	}
		        
		        //now i need to get flows
				System.out.println("info for: " + s);
				DataFlowExtractor dfe = new DataFlowExtractor();
				dfe.methodFlows(methodStmnt, libraries, methods2, fields2);		      
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }    
	
	private static class MethodNameCollector extends VoidVisitorAdapter<HashMap<SimpleName, BlockStmt>> {
		
		@Override
		public void visit(MethodDeclaration md, HashMap<SimpleName, BlockStmt> collector) {
			super.visit(md, collector);
			BlockStmt body = md.getBody().get();
			collector.put(md.getName(), body);
		}
	}
}
package thekla;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
		//System.out.println("Please insert the path to the src file dependency.");
		//scanner = new Scanner (System.in);
		//String dependency = scanner.next();	
		//scanner.close();
		//create a file that keeps all the relevant information
        OutputCreator output = new OutputCreator();
		FileTraversal fileTraverse = new FileTraversal();
        
		String fileName = output.createOutputFile(name);
		
		List<String> files = new ArrayList<>();
		files = fileTraverse.getFiles(name);
		
		// creates an input stream for the file to be parsed
        FileInputStream in;
		//InputStream in = null;
        //List<ImportDeclaration> libraries = null;
        //List<Optional<PackageDeclaration>> packages = new ArrayList<>();
       // HashMap<SimpleName,List<Parameter>> allMethodNames = new HashMap<>();
        InfoExtractor info = new InfoExtractor();
        HashMap<Entry<String,String>, Entry<String, String>> methodCallTrace = new HashMap<>();
        HashMap<String,HashMap<String,String>> alias = new HashMap<>();
        
		try {
			for(String s : files) {
				CombinedTypeSolver typeSolver = new CombinedTypeSolver();
				typeSolver.add(new ReflectionTypeSolver());	
				typeSolver.add(new JavaParserTypeSolver(new File("C:\\Users\\thekl\\Desktop\\securibench\\src\\")));
				//typeSolver.add(new JavaParserTypeSolver(new File("C:\\Users\\thekl\\Desktop\\OnlineProjectEvaluator-CODE\\src\\")));
				typeSolver.add(new JavaParserTypeSolver(new File("C:\\Users\\thekl\\Desktop\\myBenchmark\\src\\")));
				//typeSolver.add(new JavaParserTypeSolver(new File(name)));
				
				in = new FileInputStream(s);
		        // parse the file
		        CompilationUnit cu = JavaParser.parse(in);
		        
		        String[] fileParts = s.split("\\\\");
		        String[] fname = fileParts[fileParts.length-1].split("\\.");
		        //String fileN = fname[0];
		        
		        info.information(cu, typeSolver);
		        //String f = "Info" + fileN;
		        List<DFD> allDFDInfo = info.getDFDs();		    
				
		        //now i need to get flows
				DataFlowExtractor dataFlow = new DataFlowExtractor(allDFDInfo,s);
				HashMap< Entry<String,String>, String> flows = new HashMap<>();
				flows = dataFlow.parseDFDInfo();
				//flows = dataFlow.parseInfoFile(f);
				//flows = dataFlow.methodFlows2(methodStmnt, libraries, methods, fields2, methodNames);
				HashMap<String,String> dataStores = dataFlow.getDataStores();
				Optional<PackageDeclaration> pack = info.getPackage();
				methodCallTrace = dataFlow.getTrace();
				
				/*for(Entry<Entry<String,String>,Entry<String,String>> entry : methodCallTrace.entrySet()) {
					Entry<String,String> meth = entry.getKey();
					Entry<String,String> al = entry.getValue();
					System.out.println("Method " + meth.getKey() + " calls method " + meth.getValue());
					System.out.println("with parameter " + al.getKey() + " aliasing " + al.getValue());
				}*/

				alias = dataFlow.getAllAlias();
				/*for(Entry<String, HashMap<String, String>> aliasEntry : alias.entrySet()) {
					System.out.println("Method: " + aliasEntry.getKey() + " has the following info about alias.");
					HashMap<String, String> temp = aliasEntry.getValue();
					for(Entry<String,String> tempE : temp.entrySet()) {
						System.out.println("The " + tempE.getValue() + " has alias: " + tempE.getKey());
					}
				}*/
				List<ImportDeclaration> libraries = dataFlow.getExternalEntities();
				output.writeOutput(pack, libraries, dataStores, flows, fileName,s);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String subfile = "sub" + fileName;
		DotFileCreator dotFileCreator = new DotFileCreator();
		//EntireDFDExtractor dfd = new EntireDFDExtractor();
		//dfd.extractDFD(allMethodNames, packages,fileName);
		dotFileCreator.createVisualFile(subfile);
		System.out.println("I have finished");
		//String variable = "";
		fileName = "sub" + fileName;
		System.out.println("Would you like to see the DFD of a specific variable?(Answer: Variable/No)");
		//scanner = new Scanner (System.in);
		String variable = scanner.next();
		while(!variable.equals("No")) {		
				SpecificDFD specDFD = new SpecificDFD(fileName, variable, methodCallTrace, alias);
				specDFD.creteSpecificDFD();
				System.out.println("Would you like to see the DFD of a specific variable?(Answer: Variable/No)");
				variable = scanner.next();
		}
		scanner.close();
    }    
}
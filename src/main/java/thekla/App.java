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
        
		String DFDfileName = output.createOutputFile(name);
		
		List<String> files = new ArrayList<>();
		files = fileTraverse.getFiles(name);
		
		// creates an input stream for the file to be parsed
        FileInputStream in;
        
        HashMap<Entry<String,String>, Entry<String, String>> methodCallTrace = new HashMap<>();
        HashMap<String,HashMap<String,String>> alias = new HashMap<>();
        List<Optional<PackageDeclaration>> packages = new ArrayList<>();
        List<String> subFiles = new ArrayList<>();
        
		try {
			for(String s : files) {
				String fileName = output.createOutputFile(s);
				subFiles.add(fileName);
				InfoExtractor info = new InfoExtractor();
				CombinedTypeSolver typeSolver = new CombinedTypeSolver();
				typeSolver.add(new ReflectionTypeSolver());	
				//typeSolver.add(new JavaParserTypeSolver(new File("C:\\Users\\thekl\\Desktop\\securibench\\src\\")));
				typeSolver.add(new JavaParserTypeSolver(new File("C:\\Users\\thekl\\Desktop\\qatch\\src\\com\\issel\\")));
				//typeSolver.add(new JavaParserTypeSolver(new File("C:\\Users\\thekl\\Desktop\\myBenchmark\\src\\")));
				//typeSolver.add(new JavaParserTypeSolver(new File("C:\\Users\\thekl\\Desktop\\alias\\Alias\\src\\")));
				
				in = new FileInputStream(s);
		        // parse the file
		        CompilationUnit cu = JavaParser.parse(in);
		        
		        //String[] fileParts = s.split("\\\\");
		        //String[] fname = fileParts[fileParts.length-1].split("\\.");
		        
		        info.information(cu, typeSolver);
		        List<DFD> allDFDInfo = new ArrayList<>();
		        allDFDInfo = info.getDFDs();	
		        packages.add(info.getPackage());
				
		        //now i need to get flows
				DataFlowExtractor dataFlow = new DataFlowExtractor(allDFDInfo,s);
				HashMap< Entry<String,String>, String> flows = new HashMap<>();
				flows = dataFlow.parseDFDInfo();
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
		
		//creation of the entire DFD
		//first keep track of all the packages in order to know which external entities are actually not = packages
		//then call the proper class
		List<String> pack = new ArrayList<>();
		for(Optional<PackageDeclaration> p : packages) {
			pack.add(p.get().getNameAsString());
		}
		
		EntireDFDExtractor finalDFD = new EntireDFDExtractor(pack);
		finalDFD.extractDFD(subFiles, DFDfileName);
				
		DotFileCreator dotFileCreator = new DotFileCreator();
		//EntireDFDExtractor dfd = new EntireDFDExtractor();
		//dfd.extractDFD(allMethodNames, packages,fileName);
		dotFileCreator.createVisualFile(DFDfileName);
		System.out.println("I have finished");
		System.out.println("Would you like to see the DFD of a specific variable?(Answer: Variable/No)");
		String variable = scanner.next();
		while(!variable.equals("No")) {		
				SpecificDFD specDFD = new SpecificDFD(DFDfileName, variable, methodCallTrace, alias);
				specDFD.creteSpecificDFD();
				System.out.println("Would you like to see the DFD of a specific variable?(Answer: Variable/No)");
				variable = scanner.next();
		}
		scanner.close();
    }    
}
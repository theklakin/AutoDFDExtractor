package thekla;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.Map.Entry;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;


public class App 
{
	
	public static void main( String[] args )
    {		
		//get the dependencies
		System.out.println("Please insert the path to the dependency file.");
		//and read his/her input
		Scanner scanner = new Scanner (System.in);
		String path = scanner.next();	
		Dependencies dependencies = new Dependencies(path);
		dependencies.addDependencies();
		List<String> dependencyJars = dependencies.getDependencies();
		System.out.println("Please insert the path to the src file under inspection.");
		//and read his/her input
		String name = scanner.next();	
		//now the procedure starts... time it
		long startTime = System.nanoTime();
		//create a file that keeps all the relevant information
        OutputCreator output = new OutputCreator();
		FileTraversal fileTraverse = new FileTraversal();
        
		String DFDfileName = output.createOutputFile(name);
		
		List<String> files = new ArrayList<>();
		files = fileTraverse.getFiles(name);
		
		// creates an input stream for the file to be parsed
        FileInputStream in;
        
        //get input/output libs
        InOut inOut = new InOut();
        List<String> inputLibs = inOut.getInputLibs();
        List<String> outputLibs = inOut.getOutputLibs();
        
        HashMap<Entry<String,String>, Entry<String, String>> methodCallTrace = new HashMap<>();
        HashMap<String,HashMap<String,String>> alias = new HashMap<>();
        List<Optional<PackageDeclaration>> packages = new ArrayList<>();
        List<String> subFiles = new ArrayList<>();
        HashMap<Integer, Entry<String,String>> subDFD = new HashMap<>();
        int i = 0;
        
		try {
			for(String s : files) {
				String fileName = output.createOutputFile(s);
				String[] temp = fileName.split("_");
				String className = temp[1];
				subFiles.add(fileName);
				InfoExtractor info = new InfoExtractor(className);
				CombinedTypeSolver typeSolver = new CombinedTypeSolver();
				typeSolver.add(new ReflectionTypeSolver(false));	
				//typeSolver.add(new JavaParserTypeSolver(new File("C:\\Users\\thekl\\Desktop\\securibench\\src\\")));
				//typeSolver.add(new JavaParserTypeSolver(new File("C:\\Users\\thekl\\Desktop\\myBenchmark\\src\\")));
				//typeSolver.add(new JavaParserTypeSolver(new File("C:\\Users\\thekl\\Desktop\\alias\\Alias\\src\\")));
				//typeSolver.add(new JavaParserTypeSolver(new File("C:\\Users\\thekl\\Desktop\\qatch\\qatch\\src\\")));
				try {
					for(String jar: dependencyJars) {
						typeSolver.add(new JarTypeSolver(jar));
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				typeSolver.add(new JavaParserTypeSolver(new File(name+"\\")));
				
				in = new FileInputStream(s);
		        // parse the file
		        CompilationUnit cu = JavaParser.parse(in);
		        cu.removeComment();
		        
		        info.information(cu, typeSolver);
		        List<InfoContainer> allDFDInfo = new ArrayList<>();
		        allDFDInfo = info.getDFDs();	
		        packages.add(info.getPackage());
				
		        //now i need to get flows
				DataFlowExtractor dataFlow = new DataFlowExtractor(allDFDInfo,className, inputLibs, outputLibs);
				HashMap<Integer, String> flowsFrom = new HashMap<>();
				HashMap<Integer, String> flowsTo = new HashMap<>();
				HashMap<Integer, String> flowsName = new HashMap<>();
				dataFlow.parseDFDInfo();
				flowsFrom = dataFlow.getFlowsFrom();
				flowsTo = dataFlow.getFlowsTo();
				flowsName = dataFlow.getFlowsName();
				HashMap<String,String> temp2 = dataFlow.getSubDFDs();
				
				for(Entry<String,String> entry : temp2.entrySet()) {
					subDFD.put(i, entry);
					i++;
				}			
				
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
					HashMap<String, String> tem = aliasEntry.getValue();
					for(Entry<String,String> tempE : tem.entrySet()) {
						System.out.println("The " + tempE.getValue() + " has alias: " + tempE.getKey());
					}
				}*/
				List<ImportDeclaration> libraries = dataFlow.getExternalEntities();
				output.writeOutput(pack, libraries, dataStores, flowsFrom, flowsTo, flowsName, fileName,s);
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
				
		DotFileCreator dotFileCreator = new DotFileCreator(DFDfileName, subDFD);
		dotFileCreator.createVisualFile();
		System.out.println("I have finished");
		long endTime   = System.nanoTime();
		long totalTime = endTime - startTime;
		double seconds = (double)totalTime / 1000000000.0;
		System.out.println("It lasted for: " + seconds + " seconds");
		System.out.println(TimeUnit.SECONDS.convert(totalTime, TimeUnit.NANOSECONDS));
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
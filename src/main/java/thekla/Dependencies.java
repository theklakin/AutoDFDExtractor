package thekla;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Dependencies {
	
	private List<String> depend;
	private String path;
	
	Dependencies(){
		
	}
	
	Dependencies(String path){
		this.path=path;
		depend = new ArrayList<>();
		System.out.println("Adding all dependencies!");
	}
	
	public List<String> getDependencies(){
		return depend;
	}
	
	public void addDependencies() {	
		if(path.isEmpty()) {
			System.out.println("The given path is empty...");
		}else {
			//get all files in order to locate the pox.xml
			File inputFile = new File(path);
			File[] containedFiles = inputFile.listFiles();
			for(File file : containedFiles) {
				String fileName = file.getName();
				if(fileName.contains(".jar")) {
					System.out.println(file.getAbsolutePath());
					depend.add(file.getAbsolutePath());
				}
			}	
		}		
	}
}
package thekla;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileTraversal {
	
	FileTraversal(){
		System.out.println("Object FileTraversal created!");
	}
	
	public List<String> getFiles(String path){
		List<String> files = new ArrayList<>();
		File inputFile = new File(path);		
		File[] containedFiles = inputFile.listFiles();
		for(File f : containedFiles){
			files.addAll(traverse(f));
		}		
		return files;
	}
	
	public List<String> traverse(File file){
		List<String> files = new ArrayList<>();		
		if(file.isDirectory()){
			for(File j : file.listFiles()){
				List<String> help = new ArrayList<>();
				help = traverse(j);
				files.addAll(help);
			}
		} else if (file.getAbsolutePath().contains("java")){
			files.add(file.getAbsolutePath());				
		}	
		return files;		
	}
}
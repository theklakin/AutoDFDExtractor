package thekla;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileTraversal {
	
	//constructor
	FileTraversal(){
		System.out.println("File traversal created!");
	}
	
	public List<String> getFiles(String path){
		List<String> files = new ArrayList<>();
		File inputFile = new File(path);		
		File[] containedFiles = inputFile.listFiles();
		for(File f : containedFiles){
			if(f.isDirectory()) {
				//i need to go deeper to get the source file
				for(File j : f.listFiles()){
					List<String> help = new ArrayList<>();
					help = helper(j);
					files.addAll(help);
				} 
			}else if (f.getAbsolutePath().contains("java")){
				files.add(f.getAbsolutePath());	
				}
		}		
		return files;
	}
	
	public List<String> helper(File f){
		List<String> h = new ArrayList<>();		
		if(f.isDirectory()){
			//go deeper
			for(File j : f.listFiles()){
				List<String> help = new ArrayList<>();
				help = helper(j);
				h.addAll(help);
			}
		} else if (f.getAbsolutePath().contains("java")){
			h.add(f.getAbsolutePath());				
		}	
		return h;		
	}
}
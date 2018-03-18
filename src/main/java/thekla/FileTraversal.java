package thekla;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileTraversal {
	
	//constructor
	FileTraversal(){
		System.out.println("File traversal created!");
	}
	
	public boolean correctInput(String path) {
		boolean flag = false;
		
		String[] temp = path.split("\\\\");
		if(temp[temp.length-1].equals("src")) {
			flag=true;
		}				
		return flag;
	}
	
	public List<String> getFiles(String path){
		List<String> files = new ArrayList<>();
		File flag = new File(path);		
		File[] temp = flag.listFiles();
		for(File k : temp){
			if(k.isDirectory()) {
				//i need to go deeper to get the source file
				for(File j : k.listFiles()){
					List<String> help = new ArrayList<>();
					help = helper(j);
					files.addAll(help);
				} 
			}else {
					files.add(k.getAbsolutePath());	
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
		} else{
			//traverse it to get all the methods
			h.add(f.getAbsolutePath());				
		}	
		return h;		
	}
}
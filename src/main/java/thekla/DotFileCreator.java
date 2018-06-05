package thekla;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

public class DotFileCreator {
	
	private int index;
	
	DotFileCreator(){
		index = 0;
		System.out.println("DotFileCreator Object is created");
	}
	
	public void createVisualFile(String file) {
		//System.out.println(file);
		HashMap<Integer,Entry<String,String>> visual = new HashMap<>();
		List<String> externalEntities = new ArrayList<>();
		BufferedReader br = null;
		FileReader fr = null;
		String sCurrentLine;
		//boolean flag = false;
		HashMap<String,String> shapes = new HashMap<>();
		boolean externalEntityFlag = false;
		
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			while ((sCurrentLine = br.readLine()) != null) {
				//if i reached that line then I found the flows
				if(sCurrentLine.contains("The external entities are:")) {
					externalEntityFlag = true;
				}
				if(sCurrentLine.contains("The fields are:")) {
					externalEntityFlag = false;
				}
				
				if(externalEntityFlag) {
					if(!sCurrentLine.isEmpty()) {
						externalEntities.add(sCurrentLine);
					}
				}else {
					if(sCurrentLine.contains("There is a flow")){
						String from = "";
						String to = "";
						String name = "";
						String[] lineParts = sCurrentLine.split(" ");
						for(int i = 0 ; i<lineParts.length;i++) {
							if(lineParts[i].equals("from") && from.equals("")) {
								from =lineParts[i+1]; 
							} else if(lineParts[i].equals("to") && to.equals("")) {
								to = lineParts[i+1];
							} else if(lineParts[i].equals("name:")) {
								for(int j = i+1 ; j<lineParts.length; j++ ) {
									name = name + lineParts[j];
								}
								if(name.contains(";")) {
									name = name.replace(";", "");
								}
							}
						}
						//System.out.println("from: " + from + " to " + to);
						String from2 = check(from);
						String to2 = check(to);
						name = chackName(name);
						
						String shape = getShape(from, externalEntities);
						if(!shape.equals("")) {
							shapes.put(from2, shape);
						}
						shape = getShape(to, externalEntities);
						if(!shape.equals("")) {
							shapes.put(to2, shape);
						}
						
						from2 = from2 + " ->";
						to2 = to2 + " [label=\"" + name +"\"];";
						visual.put(index, new SimpleEntry(from2, to2));
						index++;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		String fileName = file + ".gv";
		try {
			PrintWriter v = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
			v.println("digraph G{");
			for(Entry<Integer,Entry<String,String>> entry : visual.entrySet()) {
				Entry<String,String> flow = entry.getValue();
				v.println("    " + flow.getKey() + " " + flow.getValue());
			}
			v.println();
			v.println();
			
			for(Entry<String,String> entry : shapes.entrySet()) {
				v.println("    " + entry.getKey() + " [shape=" + entry.getValue() + "];");
			}
			
			v.println("}");
			v.close();
			createImage(fileName, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void createImage(String file, String name) {
		//the command that is going to be given to cmd
		String dfdName = name + ".jpeg";
		String command = "dot -Tjpeg " + file + " -o " + dfdName;
		
		//put it in the cmd
		ProcessBuilder builder = new ProcessBuilder("cmd.exe","/c",command);
		builder.redirectErrorStream(true);
		
		try{
			//Process p = 
			builder.start();
		}catch(IOException e){
			System.out.println(e.getMessage());
		}	
	}
	
	public String chackName(String name) {
		String newName=name;
		
		if(name.contains(")") && !name.contains("(")) {
			String[] nameParts = name.split("\\)");
			newName = nameParts[0];
		} 		
		return newName;
	}
	
	public String check(String name) {
		String newName = name;
		
		/*
		if(name.contains(".")) {
			if(name.contains("logging")) {
				newName = "log";
			} else {
				String[] nameParts = name.split("\\.");
				newName = nameParts[nameParts.length-1];
			}
		}*/
		
		if(name.contains(".")) {
			newName = name.replace(".", "_");
		}
		
		return newName;
	}
	
	public String getShape(String name, List<String> libraries) {
		String shape = "circle";
		
		if(name.contains("database") || name.contains("file")) {
			//System.out.println("Name: " +name);
			shape = "diamond";
		} else {
			for(String lib : libraries) {
				//String libName = lib.getNameAsString();
				if(lib.contains(name)) {
					shape = "box";
					break;
				}
			}
		}
		return shape;		
	}

}

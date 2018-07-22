package thekla;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

public class DotFileCreator {
	
	private int index;
	private String file;
	private List<String> externalEntities;
	private HashMap<Integer,Entry<String,String>> visual;
	private HashMap<Integer,Entry<String,String>> groupedFlows;
	private HashMap<String,String> shapes;
	private HashMap<Integer,Entry<String,String>> subDFD;
	private HashMap<Integer, String> fromFlows;
	private HashMap<Integer, String> toFlows;
	private HashMap<Integer, String> nameFlows;
	private HashMap<String,Integer> frequency;
	
	DotFileCreator(String file, HashMap<Integer,Entry<String,String>> subDFD){
		this.file = file;
		this.subDFD = subDFD;
		externalEntities = new ArrayList<>();
		shapes = new HashMap<>();
		visual = new HashMap<>();
		groupedFlows = new HashMap<>();
		fromFlows = new HashMap<>();
		toFlows = new HashMap<>();
		nameFlows = new HashMap<>();
		frequency = new HashMap<>();
		index = 0;
		System.out.println("DotFileCreator Object is created");
	}
	
	public void createVisualFile() {
		//System.out.println(file);	
		BufferedReader br = null;
		FileReader fr = null;
		String sCurrentLine;
		//boolean flag = false;
		boolean externalEntityFlag = false;
		boolean flowFlag=false;
		
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			while ((sCurrentLine = br.readLine()) != null) {
				//if i reached that line then I found the flows
				if(sCurrentLine.contains("The external entities are:")) {
					externalEntityFlag = true;
					flowFlag=false;
				}
				if(sCurrentLine.contains("The fields are:")) {
					externalEntityFlag = false;
					flowFlag=false;
				}
				if(sCurrentLine.contains("The flows are:")) {
					flowFlag=true;
					externalEntityFlag = false;
				}
				
				if(externalEntityFlag) {
					if(!sCurrentLine.isEmpty()) {
						externalEntities.add(sCurrentLine);
					}
				}else if(flowFlag){
					if(!sCurrentLine.equals(" ")){
						//System.out.println(sCurrentLine);
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
								if(name.contains("\"")){
									name = name.replace("\"", "");
								}
							}
						}
						//System.out.println("from: " + from + " to " + to);
						String from2 = check(from);
						String to2 = check(to);
						name = chackName(name);
						
						if(from2.equals("") || to2.equals("") || name.equals("")) {
							continue;
						}
						
						fromFlows.put(index, from2);
						toFlows.put(index, to2);
						nameFlows.put(index, name);
						
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
		
		detailedView();
		if(!subDFD.isEmpty()) {
			initializeGroupedFlows();
			intermediateView(); 
			abstractView();
		}
	}
	
	public void detailedView() {
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
	
	public void intermediateView() {
		String fileName = "Intermediate_View_" + file + ".gv";
		String DFDfile = "Intermediate_View_" + file;
		try {
			PrintWriter v = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
			v.println("digraph G{");
			for(Entry<Integer,Entry<String,String>> entry : groupedFlows.entrySet()) {
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
			createImage(fileName, DFDfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initializeGroupedFlows() {
		int subIndex = 0;
		Set<String> gFlows = new HashSet<>();
		List<String> allFlows = new ArrayList<>();
		for(Entry<Integer,String> entry : fromFlows.entrySet()) {
			String from = entry.getValue();
			int ind = entry.getKey();
			String to = toFlows.get(ind);
			allFlows.add(from+ " " +to);
		}
		
		gFlows.addAll(allFlows);
		setFrequencyCount(gFlows, allFlows);
		
		for(String s: gFlows) {
			int counter = getFrequencyCount(s);
			String[] flowParts = s.split(" ");
			String from = flowParts[0];
			String to = flowParts[1];
			String labelName = getLabel(from, to);
			String from2 = from + "->";
			to = to + " [label=\"" + labelName +"\"];";
			//to = to + " [label=\"" + counter +"\"];";
			subIndex++;
			groupedFlows.put(subIndex, new SimpleEntry(from2, to));
		}		
	}
	
	private Integer getFrequencyCount(String flowName) {
		int count=1;
		for(Entry<String,Integer> s : frequency.entrySet()) {
			String name = s.getKey();
			if(flowName.equals(name)) {
				count = s.getValue();
				break;
			}
		}
		return count;
	}
	
	private void setFrequencyCount(Set<String> gFlows, List<String> allFlows) {
		for(String flowName: gFlows) {
			int counter = 0;
			for(String name: allFlows) {
				if(flowName.equals(name)) {
					counter++;
				}
			}
			frequency.put(flowName, counter);
		}
	}
	
	private String getLabel(String from, String to) {
		String labelName="";
		for(Entry<Integer,String> entry : fromFlows.entrySet()) {
			String currentFrom = entry.getValue();
			int ind = entry.getKey();
			String currentTo = toFlows.get(ind);
			if(currentFrom.contains(from) && currentTo.contains(to)) {
				String name = nameFlows.get(ind) + " || ";
				labelName = name + labelName;
			}
		}		
		return labelName;
	}
	
	/*public void intermediateView() {
		String fileName = "Intermediate_View_" + file + ".gv";
		String DFDfile = "Intermediate_View_" + file;
		HashMap<String,String> subShapes = new HashMap<>();
		try {
			PrintWriter v = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
			v.println("digraph G{");
			for(Entry<Integer,Entry<String,String>> entry : groupedFlows.entrySet()) {
				Entry<String,String> flow = entry.getValue();
				String from = intermediateName(flow.getKey());
				String shape = "";
				if(from.isEmpty()) {
					from = flow.getKey();	
					shape = getSubShape(from);
				}else {
					shape = "circle";
				}
				
				String[] fromParts = from.split(" ");
				subShapes.put(fromParts[0], shape);
				
				String to = intermediateName(flow.getValue());
				if(to.isEmpty()) {
					to = flow.getValue();
					shape = getSubShape(to);
				}else {
					shape= "circle";
				}
				String[] toParts = to.split(" ");
				subShapes.put(toParts[0], shape);
				
				v.println("    " + from + " " + to);
			}
			
			v.println();
			v.println();
			
			for(Entry<String,String> entry : subShapes.entrySet()) {
				v.println("    " + entry.getKey() + " [shape=" + entry.getValue() + "];");
			}
			
			v.println("}");
			v.close();
			createImage(fileName, DFDfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	
	
	public void abstractView() {
		String fileName = "Abstract_View_" + file + ".gv";
		String DFDfile = "Abstract_View_" + file;
		HashMap<String,String> subShapes = new HashMap<>();
		try {
			PrintWriter v = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
			v.println("digraph G{");
			for(Entry<Integer,Entry<String,String>> entry : visual.entrySet()) {
				Entry<String,String> flow = entry.getValue();
				String from = abstractName(flow.getKey());
				String shape = "";
				if(from.isEmpty()) {
					from = flow.getKey();	
					shape = getSubShape(from);
				}else {
					shape = "circle";
				}
				
				String[] fromParts = from.split(" ");
				subShapes.put(fromParts[0], shape);
				
				String to = abstractName(flow.getValue());
				if(to.isEmpty()) {
					to = flow.getValue();
					shape = getSubShape(to);
				}else {
					shape= "circle";
				}
				String[] toParts = to.split(" ");
				subShapes.put(toParts[0], shape);
				
				v.println("    " + from + " " + to);
			}
			
			v.println();
			v.println();
			
			for(Entry<String,String> entry : subShapes.entrySet()) {
				v.println("    " + entry.getKey() + " [shape=" + entry.getValue() + "];");
			}
			
			v.println("}");
			v.close();
			createImage(fileName, DFDfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getSubShape(String name) {
		String shape = "";
		for(Entry<String,String> entry : shapes.entrySet()) {
			String n = entry.getKey();
			if(name.contains(n)) {
				shape = entry.getValue();
			}
		}
		return shape;
	}
	
	private String abstractName(String name) {
		String finalName = "";
		for(Entry<Integer,Entry<String,String>> entry : subDFD.entrySet()) {
			Entry<String,String> sub = entry.getValue();
			if(name.contains(sub.getKey())) {
				String[] nameParts = name.split(" ");
				
				finalName = sub.getValue() + " " + nameParts[1];
			}
		}		
		return finalName;
	}
	
	private String intermediateName(String name) {
		String finalName = "";
		for(Entry<Integer,Entry<String,String>> entry : subDFD.entrySet()) {
			Entry<String,String> sub = entry.getValue();
			if(name.contains(sub.getKey())) {
				String[] nameParts = name.split(" ");
				
				finalName = sub.getKey() + " " + nameParts[1];
			}
		}		
		return finalName;
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
	
	private String chackName(String name) {
		String newName=name;
		//System.out.println("name: " + name);
		
		if(name.contains(")") && !name.contains("(")) {
			String[] nameParts = name.split("\\)");
			newName = nameParts[0];
		} 		
		return newName;
	}
	
	private String check(String name) {
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
	
	private String getShape(String name, List<String> libraries) {
		String shape = "ellipse";
		
		if(name.contains("database") || name.contains("file")) {
			//System.out.println("Name: " +name);
			shape = "diamond";
		} else {
			for(String lib : libraries) {
				//String libName = lib.getNameAsString();
				if(lib.contains(name) || name.contains("PrintStream")) {
					shape = "box";
					break;
				}
			}
		}
		return shape;		
	}

}

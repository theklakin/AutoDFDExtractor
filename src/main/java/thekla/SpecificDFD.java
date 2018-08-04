package thekla;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class SpecificDFD {
	
	private String file;
	private String variable;
	private HashMap<Entry<String,String>, Entry<String,String>> methodCallTrace; //HashMap<Entry<fromM,toM>,<passedData,parameterAlias>>
	private HashMap<String,HashMap<String,String>> alias ;
	private int threshold;
	
	SpecificDFD(){
		System.out.println("Object SpecificDFD is created");
	}
	
	SpecificDFD(String file, String variable, HashMap<Entry<String,String>, Entry<String,String>> methodCallTrace, HashMap<String,HashMap<String,String>> alias, int threshold){
		this.file = file;
		this.variable = variable;
		this.methodCallTrace = methodCallTrace;
		this.alias = alias;
		this.threshold = threshold;
	}
	
	public void creteSpecificDFD() {
		List<String> aliased = getAllAliasing();
		BufferedReader br = null;
		FileReader fr = null;
		String sCurrentLine;
		List<String> externalEntities = new ArrayList<>();
		Set<String> flows = new HashSet<>();
		List<String> dataStores = new ArrayList<>();
		boolean externalEntityFlag =false;
		boolean fieldsFlag =false;
		boolean flowsFlag =false;
		
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			while ((sCurrentLine = br.readLine()) != null) {			
				if(sCurrentLine.contains("The external entities are:")) {
					externalEntityFlag = true;
					continue;
				}else if(sCurrentLine.contains("The fields are:")) {
					externalEntityFlag = false;
					fieldsFlag = true;
					continue;
				}else if(sCurrentLine.contains("The flows are:")) {
					fieldsFlag = false;
					flowsFlag = true;
					continue;
				}
				
				if(!sCurrentLine.isEmpty()) {
					if(externalEntityFlag) {	
						externalEntities.add(sCurrentLine);
					}else if(fieldsFlag) {
						dataStores.add(sCurrentLine);
					}else if(flowsFlag) {
						if(sCurrentLine.contains(variable)) {
							flows.add(sCurrentLine);
						}else{
							for(String s : aliased) {
								if(sCurrentLine.contains(s)) {
									flows.add(sCurrentLine);
								}
							}
							
							//methodCallTrace: if the requested value is the entry.getValue().getKey() then the entry.getValue().getValue() needs to be shown
							// but careful the current line needs to contain the method name entry.getKey().getValue()
							for(Entry<Entry<String,String>,Entry<String,String>> traceEntry: methodCallTrace.entrySet()) {
								String var = traceEntry.getValue().getKey();
								if(var.equals(variable)) {
									String method = traceEntry.getKey().getValue();
									String varAlias = traceEntry.getValue().getKey();
									if(sCurrentLine.contains(method) && sCurrentLine.contains(varAlias)) {
										flows.add(sCurrentLine);
									}
								}
							}
						}
					}
				}				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		OutputCreator out = new OutputCreator();
		String fileName = variable + "_DFD";
		out.writeSpecificDFD(variable, externalEntities, dataStores, flows);
		
		HashMap<Integer,Entry<String,String>> subDFD = new HashMap<>();
		DotFileCreator subDFDVis = new DotFileCreator(fileName, subDFD, threshold);
		subDFDVis.createVisualFile();
	}
	
	private List<String> getAllAliasing() {
		List<String> aliased = new ArrayList<>();
		for(Entry<String,HashMap<String,String>> aliasEntry : alias.entrySet()) {
			HashMap<String,String> valueAlias = aliasEntry.getValue();
			for(Entry<String,String> ali : valueAlias.entrySet()) {
				if(variable.equals(ali.getKey())) {
					aliased.add(ali.getValue());
				}else if(variable.equals(ali.getValue())) {
					aliased.add(ali.getKey());
				}
			}
		}		
		return aliased;
	}
}
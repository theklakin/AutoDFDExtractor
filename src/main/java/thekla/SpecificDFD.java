package thekla;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
	
	SpecificDFD(String file, String variable, HashMap<Entry<String,String>, Entry<String,String>> methodCallTrace, HashMap<String,HashMap<String,String>> alias){
		this.file = file;
		this.variable = variable;
		this.methodCallTrace = methodCallTrace;
		this.alias = alias;
		System.out.println("Object SpecificDFD is created");
	}
	
	public void creteSpecificDFD() {		
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
							for(Entry<String,HashMap<String,String>> aliasEntry : alias.entrySet()) {
								HashMap<String,String> valueAlias = aliasEntry.getValue();
								for(Entry<String,String> subAliasEntry : valueAlias.entrySet()) {
									if(sCurrentLine.contains(subAliasEntry.getKey()) || sCurrentLine.contains(subAliasEntry.getValue())) {
										flows.add(sCurrentLine);
									}
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
		
		try {
			String fileName = variable + "DFD";
			PrintWriter bw = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
			bw.println("This file contains the DFD for the variable " + variable);
			bw.println();
			bw.println("The external entities are: ");
			bw.println();
			for(String externalEntity : externalEntities) {
				bw.println(externalEntity);
			}			
			bw.println();
			bw.println("The fields are: ");
			bw.println();
			if(dataStores.isEmpty()) {
				bw.println("There are no fields");
			}else {
				for(String dataStore : dataStores) {
					bw.println(dataStore);
				}
			}			
			bw.println();
			
			bw.println("The flows are: ");
			bw.println();
			for (String dataFLow : flows) {
				bw.println(dataFLow);				
			}
			bw.println();
			bw.close();
			
			DotFileCreator subDFDVis = new DotFileCreator();
			subDFDVis.createVisualFile(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}  		
	}
}

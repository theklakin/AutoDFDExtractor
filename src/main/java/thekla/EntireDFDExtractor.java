package thekla;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntireDFDExtractor {
	
	private List<String> packages;
	
	EntireDFDExtractor(){
		System.out.println("Object EntireDFDExtractor is created");
	}
	
	EntireDFDExtractor(List<String> packages){
		this.packages = packages;
	}
	
	public void extractDFD(List<String> subFiles, String DFDFileName) {
		String sCurrentLine;
		BufferedReader br = null;
		FileReader fr = null;
		Set<String> hsEE = new HashSet<>();
		Set<String> hsF = new HashSet<>();
		List<String> externalEntities = new ArrayList<>();
		List<String> finalExternalEntities = new ArrayList<>();
		List<String> flows = new ArrayList<>();
		List<String> finalFlows = new ArrayList<>();
		List<String> dataStores = new ArrayList<>();
		boolean externalEntityFlag =false;
		boolean fieldsFlag =false;
		boolean flowsFlag =false;
		OutputCreator finalFile = new OutputCreator();
		
		try {
			for(String file : subFiles) {
				fr = new FileReader(file);
				br = new BufferedReader(fr);

				while ((sCurrentLine = br.readLine()) != null) {
					/*if(sCurrentLine.contains("The following information is about")) {
						externalEntityFlag =false;
						fieldsFlag =false;
						flowsFlag =false;
						continue;
					}*/
					if(sCurrentLine.contains("The external entities are:")) {
						externalEntityFlag = true;
						flowsFlag = false;
						fieldsFlag =false;
						continue;
					}else if(sCurrentLine.contains("The fields are:")) {
						externalEntityFlag = false;
						fieldsFlag = true;
						flowsFlag = false;
						continue;
					}else if(sCurrentLine.contains("The flows are:")) {
						fieldsFlag = false;
						flowsFlag = true;
						externalEntityFlag = false;
						continue;
					}
					
					if(!sCurrentLine.isEmpty()) {
						if(externalEntityFlag && !sCurrentLine.contains("sql")) {	
							externalEntities.add(sCurrentLine);
						}else if(fieldsFlag) {
							dataStores.add(sCurrentLine);
						}else if(flowsFlag) {
							flows.add(sCurrentLine);
						}
					}
				}
				
				for(String externalEntity : externalEntities) {
					boolean contain = checkExternalEntities(externalEntity);
					if(!contain) {
						finalExternalEntities.add(externalEntity);
					}
				}
				
				for(String flow : flows) {
					String updatedFlow = checkFlows(flow);
					if(!updatedFlow.equals("")) {
						finalFlows.add(updatedFlow);
					}else {
						finalFlows.add(flow);
					}				
				}
			}			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		hsEE.addAll(finalExternalEntities);
		hsF.addAll(finalFlows);
		finalFile.writeEntireDFD(hsEE, hsF, dataStores, DFDFileName);
		finalFile.writeDFDAsXML(hsEE, hsF, dataStores, DFDFileName);
	}
	
	private String checkFlows(String flow) {
		String updatedFlow = "";
		boolean flag = true;
		
		for(String pack : packages) {
			if(flow.contains(pack)) {
				String[] flowParts = flow.split(" ");
				for(int i=0;i<flowParts.length;i++) {
					if(flowParts[i].equals("name:")) {
						flag=false;
					}
					if(flag) {
						if(flowParts[i].contains(pack)) {
							String[] temp2 = flowParts[i].split("\\.");
							if(temp2[0].equals(pack)) {
								String prefix = temp2[1];
								String component = getComponent(flowParts, i, prefix);
								updatedFlow = updatedFlow + " " + component;
							}else {
								updatedFlow = updatedFlow + " " + flowParts[i];
							}
						}else {
							updatedFlow = updatedFlow + " " + flowParts[i];
						}						
					}else {
						updatedFlow = updatedFlow + " " + flowParts[i];
					}
				}
			}
		}	
		return updatedFlow;
	}
	
	private String getComponent(String[] flowParts, int i, String prefix) {
		String component = "";
		String methodName = "";
		//part1: name of the method
		for(int j =0; j<flowParts.length-1;j++) {
			if(flowParts[j].equals("name:")) {
				methodName = flowParts[j+1];
			}
		}
		String[] temp = methodName.split("\\(");
		String method = temp[0];
		
		component = prefix+ "_" + method;
				
		return component;
	}
	
	private boolean checkExternalEntities(String externalEntity) {
		boolean contain = false;
		
		/*for(SimpleName name : methodName) {
			String mName =name.asString();
			if(externalEntity.contains(mName)) {
				contain = true;
				break;
			}
		}*/
		
		if(contain==false) {
			for(String pack : packages) {
				String pName = pack.toString();
				if(externalEntity.contains(pName)) {
					contain = true;
					break;
				}
			}
		}
		return contain;
	}
}
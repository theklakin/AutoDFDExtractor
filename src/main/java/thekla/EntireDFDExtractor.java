package thekla;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.expr.SimpleName;

public class EntireDFDExtractor {
	
	EntireDFDExtractor(){
		System.out.println("Object EntireDFDExtractor is created");
	}
	
	public void extractDFD(List<SimpleName> methodNames,  List<Optional<PackageDeclaration>> packages, String fileName) {
		String sCurrentLine;
		BufferedReader br = null;
		FileReader fr = null;
		Set<String> hs = new HashSet<>();
		List<String> externalEntities = new ArrayList<>();
		List<String> flows = new ArrayList<>();
		List<String> dataStores = new ArrayList<>();
		boolean externalEntityFlag =false;
		boolean fieldsFlag =false;
		boolean flowsFlag =false;
		OutputCreator finalFile = new OutputCreator();
		
		try {
			String file = "sub"+fileName;
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			while ((sCurrentLine = br.readLine()) != null) {
				if(sCurrentLine.contains("The following information is about")) {
					externalEntityFlag =false;
					fieldsFlag =false;
					flowsFlag =false;
					continue;
				}
				if(sCurrentLine.contains("The external entities are:")) {
					externalEntityFlag = true;
					flowsFlag = false;
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
					if(externalEntityFlag && !sCurrentLine.contains("sql")) {	
						hs.add(sCurrentLine);
					}else if(fieldsFlag) {
						dataStores.add(sCurrentLine);
					}else if(flowsFlag) {
						flows.add(sCurrentLine);
					}
				}
			}
			externalEntities.addAll(hs);
			for(String externalEntity : externalEntities) {
				boolean contain = checkExternalEntities(externalEntity, methodNames, packages);
				if(contain) {
					externalEntities.remove(externalEntity);
				}
			}
			finalFile.writeEntireDFD(externalEntities, flows, dataStores,fileName);
			
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean checkExternalEntities(String externalEntity, List<SimpleName> methodName,  List<Optional<PackageDeclaration>> packages) {
		boolean contain = false;
		
		for(SimpleName name : methodName) {
			String mName =name.asString();
			if(externalEntity.contains(mName)) {
				contain = true;
				break;
			}
		}
		
		if(contain==false) {
			for(Optional<PackageDeclaration> pack : packages) {
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

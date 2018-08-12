package thekla;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InOut {
	
	private List<String> inputLibs;
	private List<String> outputLibs;
	private List<String> dsLibs;
	
	InOut(){
		inputLibs = new ArrayList<>();
		outputLibs = new ArrayList<>();
		dsLibs = new ArrayList<>();
		setInputLibs();
		setOutputLibs();
		setDSLibs();
	}

	public List<String> getInputLibs() {
		return inputLibs;
	}
	
	public List<String> getDSLibs() {
		return dsLibs;
	}

	private void setInputLibs() {
		BufferedReader br = null;
		FileReader fr = null;
		String sCurrentLine;
		
		try {
			fr = new FileReader(new File("C:\\Users\\thekl\\Desktop\\thesis_maven\\dfdScanner\\src\\main\\java\\thekla\\Inputs.txt"));
			br = new BufferedReader(fr);

			while ((sCurrentLine = br.readLine()) != null) {
				inputLibs.add(sCurrentLine);
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<String> getOutputLibs() {
		return outputLibs;
	}

	private void setOutputLibs() {
		BufferedReader br = null;
		FileReader fr = null;
		String sCurrentLine;
		
		try {
			fr = new FileReader(new File("C:\\Users\\thekl\\Desktop\\thesis_maven\\dfdScanner\\src\\main\\java\\thekla\\Outputs.txt"));
			br = new BufferedReader(fr);

			while ((sCurrentLine = br.readLine()) != null) {
				outputLibs.add(sCurrentLine);
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	private void setDSLibs() {
		BufferedReader br = null;
		FileReader fr = null;
		String sCurrentLine;
		
		try {
			fr = new FileReader(new File("C:\\Users\\thekl\\Desktop\\thesis_maven\\dfdScanner\\src\\main\\java\\thekla\\DataStores.txt"));
			br = new BufferedReader(fr);

			while ((sCurrentLine = br.readLine()) != null) {
				dsLibs.add(sCurrentLine);
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}
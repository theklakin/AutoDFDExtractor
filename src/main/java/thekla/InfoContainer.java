package thekla;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.Statement;

public class InfoContainer {
	
	private String methodName;
	private List<String> inputs;
	private List<Parameter> parameters;
	private HashMap<String, String> fields;
	private List<ImportDeclaration> libraries;
	private Optional<PackageDeclaration> pack;
	private String className;
	private List<Statement> methodStmnt;
	private HashMap<Integer, Entry<Statement,String>> methodCalls;
	
	InfoContainer(){
		inputs = new ArrayList<>();
		//setInputs(new ArrayList<>());
		parameters = new ArrayList<>();
		//setParameters(new ArrayList<>());
		fields = new HashMap<>();
		//setFields(new HashMap<>());
		libraries = new ArrayList<>();
		//setLibraries(new ArrayList<>());
		methodStmnt = new ArrayList<>();
		//setMethodStmnt(new ArrayList<>());
		methodCalls = new HashMap<>();
		//setMethodCalls(new HashMap<>());
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public List<String> getInputs() {
		return inputs;
	}

	public void setInputs(List<String> inputs) {
		this.inputs = inputs;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public HashMap<String, String> getFields() {
		return fields;
	}

	public void setFields(HashMap<String, String> fields) {
		this.fields = fields;
	}

	public List<ImportDeclaration> getLibraries() {
		return libraries;
	}

	public void setLibraries(List<ImportDeclaration> libraries) {
		this.libraries = libraries;
	}

	public Optional<PackageDeclaration> getPack() {
		return pack;
	}

	public void setPack(Optional<PackageDeclaration> pack) {
		this.pack = pack;
	}

	public List<Statement> getMethodStmnt() {
		return methodStmnt;
	}

	public void setMethodStmnt(List<Statement> methodStmnt) {
		this.methodStmnt = methodStmnt;
	}

	public HashMap<Integer, Entry<Statement,String>> getMethodCalls() {
		return methodCalls;
	}

	public void setMethodCalls(HashMap<Integer, Entry<Statement,String>> methodCalls) {
		this.methodCalls = methodCalls;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}	
}

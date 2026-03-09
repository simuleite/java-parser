package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Function represents a function or method declaration.
 * Corresponds to uniast.Function in Go
 */
public class Function {
    @JsonProperty("Exported")
    private boolean exported;

    @JsonProperty("IsMethod")
    private boolean isMethod;

    @JsonProperty("IsInterfaceMethod")
    private boolean isInterfaceMethod;

    @JsonProperty("ModPath")
    private String modPath;

    @JsonProperty("PkgPath")
    private String pkgPath;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("File")
    private String file;

    @JsonProperty("Line")
    private int line;

    @JsonProperty("StartOffset")
    private int startOffset;

    @JsonProperty("EndOffset")
    private int endOffset;

    // [新增] end line number (1-based)
    @JsonProperty("EndLine")
    private int endLine;

    @JsonProperty("Content")
    private String content;

    @JsonProperty("Signature")
    private String signature;

    @JsonProperty("Receiver")
    private Receiver receiver; // method receiver, Java is null

    @JsonProperty("Params")
    private List<Dependency> params = new ArrayList<>();

    @JsonProperty("Results")
    private List<Dependency> results = new ArrayList<>();

    @JsonProperty("FunctionCalls")
    private List<Dependency> functionCalls = new ArrayList<>();

    @JsonProperty("MethodCalls")
    private List<Dependency> methodCalls = new ArrayList<>();

    @JsonProperty("Types")
    private List<Dependency> types = new ArrayList<>();

    @JsonProperty("GlobalVars")
    private List<Dependency> globalVars = new ArrayList<>();

    @JsonProperty("References")
    private List<Reference> references = new ArrayList<>();

    @JsonProperty("compress_data")
    private String compressData;

    public Function() {
    }

    public boolean isExported() {
        return exported;
    }

    public void setExported(boolean exported) {
        this.exported = exported;
    }

    public boolean isMethod() {
        return isMethod;
    }

    public void setMethod(boolean method) {
        isMethod = method;
    }

    public boolean isInterfaceMethod() {
        return isInterfaceMethod;
    }

    public void setInterfaceMethod(boolean interfaceMethod) {
        isInterfaceMethod = interfaceMethod;
    }

    public String getModPath() {
        return modPath;
    }

    public void setModPath(String modPath) {
        this.modPath = modPath;
    }

    public String getPkgPath() {
        return pkgPath;
    }

    public void setPkgPath(String pkgPath) {
        this.pkgPath = pkgPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public List<Dependency> getParams() {
        return params;
    }

    public void setParams(List<Dependency> params) {
        this.params = params;
    }

    public void addParam(Dependency param) {
        this.params.add(param);
    }

    public List<Dependency> getResults() {
        return results;
    }

    public void setResults(List<Dependency> results) {
        this.results = results;
    }

    public void addResult(Dependency result) {
        this.results.add(result);
    }

    public List<Dependency> getFunctionCalls() {
        return functionCalls;
    }

    public void setFunctionCalls(List<Dependency> functionCalls) {
        this.functionCalls = functionCalls;
    }

    public void addFunctionCall(Dependency functionCall) {
        this.functionCalls.add(functionCall);
    }

    public List<Dependency> getMethodCalls() {
        return methodCalls;
    }

    public void setMethodCalls(List<Dependency> methodCalls) {
        this.methodCalls = methodCalls;
    }

    public void addMethodCall(Dependency methodCall) {
        this.methodCalls.add(methodCall);
    }

    public List<Dependency> getTypes() {
        return types;
    }

    public void setTypes(List<Dependency> types) {
        this.types = types;
    }

    public void addType(Dependency type) {
        this.types.add(type);
    }

    public List<Dependency> getGlobalVars() {
        return globalVars;
    }

    public void setGlobalVars(List<Dependency> globalVars) {
        this.globalVars = globalVars;
    }

    public void addGlobalVar(Dependency globalVar) {
        this.globalVars.add(globalVar);
    }

    public List<Reference> getReferences() {
        return references;
    }

    public void setReferences(List<Reference> references) {
        this.references = references;
    }

    public void addReference(Identity refId) {
        Reference ref = new Reference();
        ref.setModPath(refId.getModPath());
        ref.setPkgPath(refId.getPkgPath());
        ref.setName(refId.getName());
        this.references.add(ref);
    }

    public String getCompressData() {
        return compressData;
    }

    public void setCompressData(String compressData) {
        this.compressData = compressData;
    }

    /**
     * Converts to Identity
     */
    public Identity toIdentity() {
        return new Identity(modPath, pkgPath, name);
    }
}

package com.uniast.parser.builder;

import com.uniast.parser.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 引用图构建器
 * 负责构建节点间的引用关系和名称索引
 */
public class ReferenceGraphBuilder {

    private boolean verbose;

    public ReferenceGraphBuilder() {
        this(false);
    }

    public ReferenceGraphBuilder(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * 构建完整的依赖图和反向引用
     * @param repo 仓库模型
     */
    public void build(Repository repo) {
        buildReferenceGraph(repo);
        buildNameToLocations(repo);
    }

    /**
     * Build complete dependency graph and reverse references
     */
    private void buildReferenceGraph(Repository repo) {
        // Step 1: Add all nodes to Graph with Dependencies (outgoing)
        for (UniModule mod : repo.getModules().values()) {
            // Skip external modules (dir is not null and not "." which means internal module)
            String dir = mod.getDir();
            if (dir != null && !dir.equals(".") && !dir.isEmpty()) {
                continue;
            }

            for (UniPackage pkg : mod.getPackages().values()) {
                // Process all functions
                if (pkg.getFunctions() != null) {
                    for (Function func : pkg.getFunctions().values()) {
                        addNodeToGraph(repo, func);
                    }
                }

                // Process all types
                if (pkg.getTypes() != null) {
                    for (UniType type : pkg.getTypes().values()) {
                        addNodeToGraph(repo, type);
                    }
                }

                // Process all variables
                if (pkg.getVars() != null) {
                    for (Var var : pkg.getVars().values()) {
                        addNodeToGraph(repo, var);
                    }
                }
            }
        }

        // Step 2: Build References (incoming) - reverse lookup
        for (UniModule mod : repo.getModules().values()) {
            String dir = mod.getDir();
            if (dir != null && !dir.equals(".") && !dir.isEmpty()) {
                continue;
            }

            for (UniPackage pkg : mod.getPackages().values()) {
                // Process all functions
                if (pkg.getFunctions() != null) {
                    for (Function func : pkg.getFunctions().values()) {
                        buildInReferences(repo, func);
                    }
                }

                // Process all types
                if (pkg.getTypes() != null) {
                    for (UniType type : pkg.getTypes().values()) {
                        buildInReferences(repo, type);
                    }
                }

                // Process all variables
                if (pkg.getVars() != null) {
                    for (Var var : pkg.getVars().values()) {
                        buildInReferences(repo, var);
                    }
                }
            }
        }
    }

    /**
     * Add a Function to the Graph
     */
    private void addNodeToGraph(Repository repo, Function func) {
        Node node = new Node();
        node.setModPath(func.getModPath());
        node.setPkgPath(func.getPkgPath());
        node.setName(func.getName());
        node.setType("FUNCTION");
        node.setFile(func.getFile());
        node.setLine(func.getLine());
        node.setStartOffset(func.getStartOffset());
        node.setEndOffset(func.getEndOffset());
        node.setCodes(func.getContent());

        // Collect all dependencies: params, results, functionCalls, methodCalls, types, globalVars
        List<Dependency> allDeps = new ArrayList<>();

        if (func.getParams() != null) allDeps.addAll(func.getParams());
        if (func.getResults() != null) allDeps.addAll(func.getResults());
        if (func.getFunctionCalls() != null) allDeps.addAll(func.getFunctionCalls());
        if (func.getMethodCalls() != null) allDeps.addAll(func.getMethodCalls());
        if (func.getTypes() != null) allDeps.addAll(func.getTypes());
        if (func.getGlobalVars() != null) allDeps.addAll(func.getGlobalVars());

        if (!allDeps.isEmpty()) {
            node.setDependencies(allDeps.toArray(new Dependency[0]));
        }

        String nodeId = node.getModPath() + "?" + node.getPkgPath() + "#" + node.getName();
        repo.addNode(nodeId, node);
    }

    /**
     * Add a Type to the Graph
     */
    private void addNodeToGraph(Repository repo, UniType type) {
        Node node = new Node();
        node.setModPath(type.getModPath());
        node.setPkgPath(type.getPkgPath());
        node.setName(type.getName());
        node.setType("TYPE");
        node.setFile(type.getFile());
        node.setLine(type.getLine());
        node.setStartOffset(type.getStartOffset());
        node.setEndOffset(type.getEndOffset());
        node.setCodes(type.getContent());

        // Collect all dependencies: subStruct (extends), implements (implements)
        List<Dependency> allDeps = new ArrayList<>();

        if (type.getSubStruct() != null) allDeps.addAll(type.getSubStruct());
        if (type.getImplements() != null) allDeps.addAll(type.getImplements());

        if (!allDeps.isEmpty()) {
            node.setDependencies(allDeps.toArray(new Dependency[0]));
        }

        String nodeId = node.getModPath() + "?" + node.getPkgPath() + "#" + node.getName();
        repo.addNode(nodeId, node);
    }

    /**
     * Add a Var to the Graph
     */
    private void addNodeToGraph(Repository repo, Var var) {
        Node node = new Node();
        node.setModPath(var.getModPath());
        node.setPkgPath(var.getPkgPath());
        node.setName(var.getName());
        node.setType("VAR");
        node.setFile(var.getFile());
        node.setLine(var.getLine());
        node.setStartOffset(var.getStartOffset());
        node.setEndOffset(var.getEndOffset());
        node.setCodes(var.getContent());

        String nodeId = node.getModPath() + "?" + node.getPkgPath() + "#" + node.getName();
        repo.addNode(nodeId, node);
    }

    /**
     * Build name → files reverse index for search_symbol API
     */
    private void buildNameToLocations(Repository repo) {
        for (UniModule mod : repo.getModules().values()) {
            for (UniPackage pkg : mod.getPackages().values()) {
                // Process types
                if (pkg.getTypes() != null) {
                    for (UniType type : pkg.getTypes().values()) {
                        String name = type.getName();
                        String file = type.getFile();
                        if (name != null && !name.isEmpty() && file != null && !file.isEmpty()) {
                            repo.addNameLocation(name, file);
                        }
                    }
                }

                // Process functions
                if (pkg.getFunctions() != null) {
                    for (Function func : pkg.getFunctions().values()) {
                        String name = func.getName();
                        String file = func.getFile();
                        if (name != null && !name.isEmpty() && file != null && !file.isEmpty()) {
                            repo.addNameLocation(name, file);
                        }
                    }
                }

                // Process variables
                if (pkg.getVars() != null) {
                    for (Var var : pkg.getVars().values()) {
                        String name = var.getName();
                        String file = var.getFile();
                        if (name != null && !name.isEmpty() && file != null && !file.isEmpty()) {
                            repo.addNameLocation(name, file);
                        }
                    }
                }
            }
        }
    }

    /**
     * Build References (incoming) for a single node.
     * This finds all nodes that depend on the current node (reverse lookup).
     */
    private void buildInReferences(Repository repo, Object nodeObj) {
        // Get current node's identity and outgoing dependencies
        Identity nodeId;
        List<Dependency> allOutgoing = new ArrayList<>();

        if (nodeObj instanceof Function) {
            Function func = (Function) nodeObj;
            nodeId = new Identity(func.getModPath(), func.getPkgPath(), func.getName());
            allOutgoing.addAll(func.getParams());
            allOutgoing.addAll(func.getResults());
            allOutgoing.addAll(func.getFunctionCalls());
            allOutgoing.addAll(func.getMethodCalls());
            allOutgoing.addAll(func.getTypes());
            allOutgoing.addAll(func.getGlobalVars());
        } else if (nodeObj instanceof UniType) {
            UniType type = (UniType) nodeObj;
            nodeId = new Identity(type.getModPath(), type.getPkgPath(), type.getName());
            allOutgoing.addAll(type.getSubStruct());
            allOutgoing.addAll(type.getImplements());
        } else if (nodeObj instanceof Var) {
            Var var = (Var) nodeObj;
            nodeId = new Identity(var.getModPath(), var.getPkgPath(), var.getName());
            allOutgoing.addAll(var.getDependencies());
            if (var.getType() != null) {
                Dependency typeDep = new Dependency();
                typeDep.setModPath(var.getType().getModPath());
                typeDep.setPkgPath(var.getType().getPkgPath());
                typeDep.setName(var.getType().getName());
                allOutgoing.add(typeDep);
            }
        } else {
            return;
        }

        // For each outgoing dependency, find the target node and add incoming reference
        for (Dependency dep : allOutgoing) {
            String depModPath = dep.getModPath();
            String depPkgPath = dep.getPkgPath();
            String depName = dep.getName();

            // Find the dependent node in Graph
            String depNodeId = depModPath + "?" + depPkgPath + "#" + depName;
            Node depNode = repo.getGraph().get(depNodeId);

            if (depNode != null) {
                // Add incoming reference to the dependent node
                // (depNode is being referenced by nodeObj)
                Dependency ref = new Dependency();
                ref.setModPath(nodeId.getModPath());
                ref.setPkgPath(nodeId.getPkgPath());
                ref.setName(nodeId.getName());

                Dependency[] existingRefs = depNode.getReferences();
                if (existingRefs == null || existingRefs.length == 0) {
                    depNode.setReferences(new Dependency[]{ref});
                } else {
                    // Check for duplicates
                    boolean exists = false;
                    for (Dependency existing : existingRefs) {
                        if (existing.getModPath().equals(ref.getModPath()) &&
                            existing.getPkgPath().equals(ref.getPkgPath()) &&
                            existing.getName().equals(ref.getName())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        Dependency[] newRefs = new Dependency[existingRefs.length + 1];
                        System.arraycopy(existingRefs, 0, newRefs, 0, existingRefs.length);
                        newRefs[existingRefs.length] = ref;
                        depNode.setReferences(newRefs);
                    }
                }

                if (verbose) {
                    System.err.println("   ✅ Added incoming reference: " + nodeId.getName() + " → " + depName);
                }
            }
        }
    }

}

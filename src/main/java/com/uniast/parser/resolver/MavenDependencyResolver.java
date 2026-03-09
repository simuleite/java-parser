package com.uniast.parser.resolver;

import org.apache.maven.model.*;
import org.apache.maven.model.building.*;
import org.apache.maven.model.resolution.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.File;

import java.nio.file.*;
import java.util.*;

/**
 * Maven 依赖解析器实现
 * 解析 pom.xml，获取所有传递依赖的 JAR 文件路径
 */
public class MavenDependencyResolver implements IDependencyResolver {

    private final String localRepo;

    public MavenDependencyResolver() {
        // 默认使用 Maven 本地仓库
        this.localRepo = System.getProperty("user.home") + "/.m2/repository";
    }

    /**
     * 解析依赖
     * @param pomFile pom.xml 路径
     * @return 依赖 JAR 路径列表
     */
    @Override
    public List<String> resolve(Path pomFile) throws Exception {
        List<String> dependencies = new ArrayList<>();

        if (!Files.exists(pomFile)) {
            System.out.println("⚠️  pom.xml not found: " + pomFile);
            return dependencies;
        }

        try {
            // 1. 读取 pom.xml
            ModelBuildingRequest request = new DefaultModelBuildingRequest();
            request.setPomFile(pomFile.toFile());
            request.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
            request.setSystemProperties(System.getProperties());
            request.setProcessPlugins(false); // 不处理插件，加速解析

            // 2. 构建 Model
            ModelBuildingResult result = new DefaultModelBuilderFactory().newInstance().build(request);
            Model model = result.getEffectiveModel();

            // 3. 收集所有依赖（包括传递依赖）
            Set<String> visited = new HashSet<>(); // 防止循环依赖
            collectDependencies(model.getDependencies(), dependencies, visited);

            System.out.println("✅ Resolved " + dependencies.size() + " Maven dependencies from pom.xml");

        } catch (Exception e) {
            System.err.println("⚠️  Failed to resolve Maven dependencies: " + e.getMessage());
            // 不抛出异常，继续解析（使用默认 classpath）
        }

        return dependencies;
    }

    /**
     * 提取模块信息
     * @param pomFile pom.xml 路径
     * @return 依赖解析结果（包含模块信息）
     */
    @Override
    public DependencyResult extractModuleInfo(Path pomFile) throws Exception {
        if (!Files.exists(pomFile)) {
            throw new IllegalArgumentException("pom.xml not found: " + pomFile);
        }

        try {
            // 读取 pom.xml
            ModelBuildingRequest request = new DefaultModelBuildingRequest();
            request.setPomFile(pomFile.toFile());
            request.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
            request.setSystemProperties(System.getProperties());
            request.setProcessPlugins(false);

            // 构建 Model
            ModelBuildingResult result = new DefaultModelBuilderFactory().newInstance().build(request);
            Model model = result.getEffectiveModel();

            // ✅ 修复: 对于子模块，使用 getRawModel() 获取原始 pom 内容
            // 否则子模块会继承父 pom 的 groupId/version，导致 ID 变成父项目的
            Model rawModel = result.getRawModel();
            if (rawModel != null) {
                model = rawModel;
            }

            // 提取模块信息
            // ✅ 修复: rawModel 包含子模块自己在 pom.xml 中定义的字段
            // 子模块的 artifactId 是自己的，groupId/version 如果没定义则为 null
            String groupId = model.getGroupId();
            String artifactId = model.getArtifactId();
            String version = model.getVersion();

            // 如果 groupId/version 为 null，从 parent 继承
            if (groupId == null && model.getParent() != null) {
                groupId = model.getParent().getGroupId();
            }
            if (version == null && model.getParent() != null) {
                version = model.getParent().getVersion();
            }

            // 使用默认值
            if (groupId == null) {
                groupId = "unknown";
            }
            if (version == null) {
                version = "unknown";
            }
            if (artifactId == null) {
                throw new IllegalStateException("artifactId not found in pom.xml");
            }

            return new DependencyResult(groupId, artifactId, version);

        } catch (Exception e) {
            throw new Exception("Failed to extract module info from pom.xml: " + e.getMessage(), e);
        }
    }

    /**
     * 简单提取模块信息（不使用完整 Maven 模型）
     * @param pomFile pom.xml 路径
     * @return 依赖解析结果（包含模块信息）
     */
    @Override
    public DependencyResult extractModuleInfoSimple(Path pomFile) throws Exception {
        if (!Files.exists(pomFile)) {
            throw new IllegalArgumentException("pom.xml not found: " + pomFile);
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(pomFile.toFile());
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();

            // 读取 artifactId (必需) - 使用子模块自己的 artifactId
            String artifactId = getTagValue(root, "artifactId");
            if (artifactId == null) {
                throw new IllegalStateException("artifactId not found in pom.xml");
            }

            // 读取 groupId - 先尝试自己的，没有则从 parent 继承
            String groupId = getTagValue(root, "groupId");
            if (groupId == null) {
                Element parent = getChildElement(root, "parent");
                if (parent != null) {
                    groupId = getTagValue(parent, "groupId");
                }
            }

            // 读取 version - 先尝试自己的，没有则从 parent 继承
            String version = getTagValue(root, "version");
            if (version == null) {
                Element parent = getChildElement(root, "parent");
                if (parent != null) {
                    version = getTagValue(parent, "version");
                }
            }

            // 使用默认值
            if (groupId == null) {
                groupId = "unknown";
            }
            if (version == null) {
                version = "unknown";
            }

            return new DependencyResult(groupId, artifactId, version);

        } catch (Exception e) {
            throw new Exception("Failed to extract module info from pom.xml (simple mode): " + e.getMessage(), e);
        }
    }

    /**
     * 解析 Monorepo 模块列表
     * @param pomFile parent pom.xml
     * @return 模块名称列表
     */
    @Override
    public List<String> parseModules(Path pomFile) {
        List<String> modules = new ArrayList<>();

        if (!Files.exists(pomFile)) {
            return modules;
        }

        try {
            // Check if this is a Monorepo (packaging = pom)
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(pomFile.toFile());
            doc.getDocumentElement().normalize();

            // Check packaging type
            Element root = doc.getDocumentElement();
            String packaging = getTagValue(root, "packaging");
            if (packaging == null || !"pom".equals(packaging)) {
                // Not a Monorepo parent
                return modules;
            }

            // Parse modules
            NodeList moduleNodes = root.getElementsByTagName("module");
            for (int i = 0; i < moduleNodes.getLength(); i++) {
                Node node = moduleNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String moduleName = node.getTextContent().trim();
                    if (!moduleName.isEmpty()) {
                        modules.add(moduleName);
                    }
                }
            }

        } catch (Exception e) {
            // Ignore parsing errors, return empty list
        }

        return modules;
    }

    // 辅助方法：获取直接子元素的值（不递归）
    private String getTagValue(Element parent, String tagName) {
        NodeList nodeList = parent.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element) {
                Element child = (Element) nodeList.item(i);
                if (child.getTagName().equals(tagName)) {
                    String value = child.getTextContent();
                    return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
                }
            }
        }
        return null;
    }

    // 辅助方法：获取第一个子元素
    private Element getChildElement(Element parent, String tagName) {
        NodeList nodeList = parent.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element) {
                Element child = (Element) nodeList.item(i);
                if (child.getTagName().equals(tagName)) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * 递归收集传递依赖
     * @param mavenDependencies Maven 依赖列表
     * @param result 收集结果
     * @param visited 已访问的依赖（防止循环）
     */
    private void collectDependencies(List<Dependency> mavenDependencies, List<String> result, Set<String> visited) {
        for (Dependency mavenDep : mavenDependencies) {
            // 跳过 provided, test, system 依赖
            String scope = mavenDep.getScope();
            if ("provided".equals(scope) || "test".equals(scope) || "system".equals(scope)) {
                continue;
            }

            String groupId = mavenDep.getGroupId();
            String artifactId = mavenDep.getArtifactId();
            String version = mavenDep.getVersion();
            String type = mavenDep.getType() != null ? mavenDep.getType() : "jar";

            // 构建依赖唯一标识
            String depKey = groupId + ":" + artifactId + ":" + version;

            // 防止循环依赖
            if (visited.contains(depKey)) {
                continue;
            }
            visited.add(depKey);

            // 构建 JAR 路径
            // Maven 本地仓库路径格式：~/.m2/repository/groupId/artifactId/version/artifactId-version.jar
            String groupPath = groupId.replace('.', '/');
            String jarName = artifactId + "-" + version + "." + type;

            Path jarPath = Paths.get(
                localRepo,
                groupPath,
                artifactId,
                version,
                jarName
            );

            // 检查 JAR 文件是否存在
            if (Files.exists(jarPath)) {
                result.add(jarPath.toString());
            } else {
                System.err.println("⚠️  Dependency JAR not found: " + jarPath);
            }
        }
    }
}

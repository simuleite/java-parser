package com.uniast.parser.serializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.uniast.parser.model.Repository;

/**
 * JSON 序列化器实现
 * 使用 Jackson 将 Repository 序列化为 UniAST JSON 格式
 */
public class JsonSerializer implements ISerializer {

    private final ObjectMapper objectMapper;

    public JsonSerializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.disable(SerializationFeature.INDENT_OUTPUT); // Compact JSON
        this.objectMapper.configure(MapperFeature.USE_ANNOTATIONS, true);
        this.objectMapper.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
        this.objectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, false);
    }

    /**
     * 序列化 Repository 为 JSON 字符串
     * @param repository 仓库模型
     * @return JSON 字符串
     */
    @Override
    public String serialize(Repository repository) {
        try {
            return objectMapper.writeValueAsString(repository);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize Repository to JSON", e);
        }
    }
}

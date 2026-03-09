package com.uniast.parser.serializer;

import com.uniast.parser.model.Repository;

/**
 * 序列化器接口
 */
public interface ISerializer {
    /**
     * 序列化 Repository 为 JSON 字符串
     * @param repository 仓库模型
     * @return JSON 字符串
     */
    String serialize(Repository repository);
}

package com.uniast.parser.parser;

import com.uniast.parser.model.Repository;
import com.uniast.parser.input.RepoInput;
import com.uniast.parser.config.ParseOptions;
import java.nio.file.Path;

/**
 * Java 解析器接口
 */
public interface IJavaParser {
    /**
     * 解析仓库
     * @param input 仓库输入
     * @param options 解析选项
     * @return Repository 模型
     */
    Repository parse(RepoInput input, ParseOptions options);
}

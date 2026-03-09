package com.example.util;

import com.example.model.User;
import com.example.enum_.Status;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 工具类 - 测试静态方法调用和函数式接口
 */
public class StringUtils {

    // 静态字段
    private static final String SEPARATOR = ",";

    /**
     * 测试静态方法调用
     */
    public static String joinUsers(User[] users) {
        // 静态方法调用
        StringBuilder sb = new StringBuilder();

        for (User user : users) {
            // 实例方法调用
            sb.append(user.getName());
            sb.append(SEPARATOR);
        }

        return sb.toString();
    }

    /**
     * 测试方法引用
     */
    public static void testMethodReference() {
        // 方法引用
        Function<String, Integer> lengthFn = String::length;
        Supplier<User> userSupplier = User::new;
    }

    /**
     * 测试泛型方法
     */
    public <T extends User> T createUser(Class<T> clazz) throws Exception {
        // 泛型类型实例化
        return clazz.newInstance();
    }

    /**
     * 测试复杂表达式
     */
    public String complexExpression(String input) {
        // 多层嵌套调用
        return input.trim()
            .toLowerCase()
            .replaceAll("\\s+", ".")
            .substring(0, Math.min(10, input.length()));
    }
}

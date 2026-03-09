package com.example.demo;

import com.example.model.User;
import com.example.enum_.Status;

/**
 * Demo: 验证依赖分析的数据流转
 *
 * depend (outgoing): Caller → Callee
 * refer (incoming): Callee ← Caller
 */
public class DependencyDemo {

    // Case 1: 构造函数调用
    public void testConstructor() {
        User user = new User();  // 依赖 User 构造函数
    }

    // Case 2: 方法调用
    public void testMethodCall() {
        User user = new User();
        user.setName("test");    // 依赖 User.setName 方法
    }

    // Case 3: 枚举访问
    public void testEnum() {
        Status s = Status.ACTIVE;  // 依赖 Status 枚举
    }

    // Case 4: 静态常量
    public void testStatic() {
        int max = com.example.constant.Constants.MAX_SIZE;  // 依赖 Constants.MAX_SIZE
    }

    // Case 5: 泛型实例化 (非标准库)
    public void testGeneric() {
        User user = new User();  // 依赖 User 构造函数
        user.setName("test");
    }

    // Case 6: 类型转换
    public void testCast() {
        Object obj = "test";
        String str = (String) obj;  // 依赖 String 类型
    }

    // Case 7: 方法链
    public void testChain() {
        String s = "hello";
        int len = s.trim().toLowerCase().length();  // 依赖 trim, toLowerCase, length
    }

    // Case 8: 注解
    @com.example.annotation.MyAnnotation(value = "test", priority = 1)
    public void testAnnotation() {
        // 依赖 @MyAnnotation 注解
    }
}

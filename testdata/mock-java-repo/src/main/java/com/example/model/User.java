package com.example.model;

import com.example.enum_.Status;
import com.example.constant.Constants;
import com.example.annotation.MyAnnotation;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * 用户模型 - 包含各种边界case
 */
public class User {
    private String name;
    private int age;
    private Status status;
    private List<String> tags;
    private Map<String, Object> metadata;

    // 字段类型引用枚举
    private Status currentStatus;

    // 字段类型引用外部类
    private com.example.constant.Constants constants;

    public User() {
        this.tags = new ArrayList<>();
        this.metadata = new HashMap<>();
        // 枚举访问
        this.status = Status.ACTIVE;
        // 静态常量访问
        this.age = Constants.MAX_SIZE;
    }

    // 注解使用
    @MyAnnotation(value = "test", priority = 1)
    public void doSomething() {
        // 枚举方法调用
        Status s = Status.fromString("ACTIVE");

        // 枚举访问
        Status localStatus = Status.PENDING;

        // 静态常量访问
        int max = Constants.MAX_SIZE;
        String defaultName = Constants.DEFAULT_NAME;
        Status constStatus = Constants.STATUS;

        // 泛型类型引用
        List<String> list = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();

        // 强制类型转换
        Object obj = "test";
        String str = (String) obj;

        // 链式调用
        list.add("item").toString();

        // 多层方法调用
        int hash = getValue().hashCode();

        // this引用
        this.name = "test";
        this.doInner();

        // 标准库过滤验证
        java.util.Collections.sort(list);
    }

    private String getValue() {
        return "value";
    }

    private void doInner() {
        // 内部类访问
        InnerClass inner = new InnerClass();
        inner.process();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public List<String> getTags() { return tags; }

    // 内部类
    public class InnerClass {
        public void process() {
            // 引用外部类
            User.this.doSomething();
        }
    }

    // 静态内部类
    public static class StaticInnerClass {
        public void process() {
            // 静态上下文访问
            int max = Constants.MAX_SIZE;
        }
    }
}

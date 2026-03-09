package com.example.service;

import com.example.model.User;
import com.example.model.Order;
import com.example.enum_.Status;
import com.example.constant.Constants;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;

/**
 * 复杂服务类 - 测试更多边界case
 */
public class ComplexService {

    // 数组类型
    private User[] users;
    private int[] numbers;

    // 多层泛型
    private List<Map<String, List<User>>> complexData;

    // 函数式接口
    private Callable<User> callable;
    private Future<User> future;

    public ComplexService() {
        this.users = new User[0];
        this.numbers = new int[10];
        this.complexData = new ArrayList<>();
    }

    /**
     * 测试try-with-resources
     */
    public void testTryWithResources() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader("test.txt"))) {
            String line = reader.readLine();
            // 依赖: BufferedReader, FileReader, Reader
        }
    }

    /**
     * 测试复杂泛型
     */
    public void testComplexGeneric() {
        // 多层嵌套泛型
        Map<String, List<Optional<User>>> complex =
            new HashMap<>();

        // 泛型方法调用
        List<String> list = Arrays.asList("a", "b", "c");

        // 通配符边界
        List<? extends Number> numList = new ArrayList<Integer>();
        List<? super Integer> intList = new ArrayList<Number>();
    }

    /**
     * 测试方法引用
     */
    public void testMethodReferences() {
        // 构造函数引用
        Supplier<User> s1 = User::new;

        // 实例方法引用
        Comparator<String> c1 = String::compareTo;

        // 静态方法引用
        Function<String, Integer> f1 = Integer::parseInt;
    }

    /**
     * 测试注解中的枚举
     */
    @Deprecated
    public void testAnnotationWithEnum() {
        Status s = Status.ACTIVE;
    }

    /**
     * 测试异常类型引用
     */
    public void testExceptionTypes() throws IOException, RuntimeException {
        // 异常类型引用
        throw new IOException("test");
    }

    /**
     * 测试条件表达式中的类型
     */
    public Object testConditional(boolean flag) {
        return flag ? new User() : new Order();
    }

    /**
     * 测试赋值表达式
     */
    public void testAssignment() {
        User u = null;
        u = new User();
        u = getUser();
    }

    /**
     * 测试增强for循环
     */
    public void testEnhancedFor() {
        for (User u : getUsers()) {
            Status s = u.getStatus();
        }

        // for each with map
        for (Map.Entry<String, User> entry : getUserMap().entrySet()) {
            String key = entry.getKey();
            User value = entry.getValue();
        }
    }

    private User getUser() { return new User(); }
    private List<User> getUsers() { return new ArrayList<>(); }
    private Map<String, User> getUserMap() { return new HashMap<>(); }
}

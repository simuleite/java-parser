package com.example.service;

import com.example.model.User;
import com.example.enum_.Status;
import com.example.constant.Constants;
import com.example.annotation.MyAnnotation;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;

/**
 * 用户服务 - 包含各种依赖边界case
 */
public class UserService {

    // 字段声明 - 多种类型引用
    private User user;
    private List<User> users;
    private Map<String, User> userMap;
    private Status status;
    private Class<?> clazz;

    public UserService() {
        this.users = new ArrayList<>();
        this.userMap = new HashMap<>();
    }

    /**
     * 测试各种依赖类型
     */
    @MyAnnotation(value = "createUser", priority = 5)
    public User createUser(String name, int age) {
        // 1. 构造函数调用 - 外部类
        User newUser = new User();

        // 2. 方法调用 - 外部类
        newUser.setName(name);
        newUser.setAge(age);

        // 3. 枚举访问
        Status s = Status.ACTIVE;
        Status fromString = Status.fromString("ACTIVE");

        // 4. 静态常量访问
        int max = Constants.MAX_SIZE;
        String defaultName = Constants.DEFAULT_NAME;
        Status constStatus = Constants.STATUS;

        // 5. 泛型类型引用
        List<String> list = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();

        // 6. 强制类型转换
        Object obj = "test";
        String str = (String) obj;

        // 7. 方法链式调用
        list.add("item").toString();

        // 8. Lambda表达式
        List<User> filtered = users.stream()
            .filter(u -> u.getAge() > 18)
            .collect(Collectors.toList());

        // 9. 匿名类
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // 匿名类中的依赖访问
                User u = new User();
                Status st = Status.ACTIVE;
            }
        };

        // 10. 标准库调用 (应该被过滤)
        try {
            File f = new File("test");
            f.exists();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 11. 多层方法调用
        String result = getUser().getName().toUpperCase();

        // 12. 三元运算符中的类型
        User u = users.isEmpty() ? new User() : users.get(0);

        return newUser;
    }

    /**
     * 测试方法返回类型
     */
    public List<User> getUsers() {
        return users;
    }

    public Map<String, User> getUserMap() {
        return userMap;
    }

    public Status getStatus() {
        return status;
    }

    private User getUser() {
        return user;
    }

    /**
     * 测试参数类型引用
     */
    public void processUsers(List<User> userList, Map<String, User> userDict) {
        for (User u : userList) {
            userDict.put(u.getName(), u);
        }
    }

    /**
     * 测试throws子句
     */
    public void riskyOperation() throws IOException, RuntimeException {
        throw new IOException("error");
    }
}

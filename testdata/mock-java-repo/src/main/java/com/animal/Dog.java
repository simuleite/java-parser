package com.animal;

import com.example.model.User;
import com.example.enum_.Status;
import com.example.constant.Constants;
import java.util.List;

/**
 * 跨包依赖测试 - 测试不同包之间的依赖关系
 */
public class Dog {

    private String name;
    private int age;

    // 跨包枚举引用
    private Status status;

    // 跨包常量引用
    private int maxAge = Constants.MAX_SIZE;

    public Dog() {
        // 跨包类实例化
        User user = new User();
    }

    public void bark() {
        // 跨包枚举方法调用
        Status s = Status.fromString("ACTIVE");

        // 跨包静态常量
        int max = Constants.MAX_SIZE;

        // 跨包方法调用
        processUser(new User());
    }

    private void processUser(User user) {
        // 跨包方法参数
        List<String> tags = user.getTags();
    }
}

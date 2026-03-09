package com.example.inner;

import com.example.model.User;
import com.example.enum_.Status;
import com.example.constant.Constants;

/**
 * 内部类测试 - 测试内部类和内部接口
 */
public class OuterClass {

    // 内部类声明
    private InnerClass inner;
    private StaticInnerClass staticInner;

    public OuterClass() {
        // 内部类实例化
        inner = new InnerClass();
        staticInner = new StaticInnerClass();
    }

    /**
     * 测试内部接口
     */
    public void testInnerInterface() {
        InnerInterface impl = new InnerInterface() {
            @Override
            public void process() {
                // 匿名类中的依赖
                User u = new User();
                Status s = Status.ACTIVE;
            }
        };
        impl.process();
    }

    /**
     * 测试本地类 (Local Class)
     */
    public void testLocalClass() {
        class LocalProcessor {
            private User user;

            public LocalProcessor() {
                this.user = new User();
            }

            public void process() {
                Status s = Status.ACTIVE;
                int max = Constants.MAX_SIZE;
            }
        }

        LocalProcessor processor = new LocalProcessor();
        processor.process();
    }

    /**
     * 测试 lambda 表达式中的依赖
     */
    public void testLambda() {
        Runnable r = () -> {
            User u = new User();
            Status s = Status.fromString("ACTIVE");
        };

        java.util.function.Consumer<String> c = s -> {
            int len = Constants.MAX_SIZE;
        };
    }

    // 普通内部类
    public class InnerClass {
        public void process() {
            // 引用外部类
            OuterClass.this.testInnerInterface();
        }
    }

    // 静态内部类
    public static class StaticInnerClass {
        public void process() {
            // 静态内部类只能访问外部类的静态成员
            int max = Constants.MAX_SIZE;
        }
    }

    // 内部接口
    public interface InnerInterface {
        void process();
    }
}

# Mock Java Repository - 测试边界 Case

本仓库用于测试 Java Parser 的依赖分析功能，包含各种边界情况。

## 目录结构

```
mock-java-repo/
├── src/main/java/
│   ├── com/example/
│   │   ├── enum_/Status.java          # 枚举类型
│   │   ├── constant/Constants.java    # 静态常量
│   │   ├── annotation/MyAnnotation.java # 自定义注解
│   │   ├── model/
│   │   │   ├── User.java             # 用户模型
│   │   │   └── Order.java           # 订单模型
│   │   ├── service/
│   │   │   ├── UserService.java      # 用户服务
│   │   │   ├── OrderService.java     # 订单服务
│   │   │   └── ComplexService.java   # 复杂服务
│   │   ├── util/StringUtils.java     # 工具类
│   │   └── inner/OuterClass.java      # 内部类测试
│   └── com/animal/
│       └── Dog.java                  # 跨包依赖测试
```

## 测试 Case 清单

### 1. 枚举依赖 (enum_/Status.java)

| Case | 位置 | 代码示例 | 预期捕获 |
|------|------|----------|----------|
| 枚举常量访问 | User.java:31 | `Status.ACTIVE` | ✅ Status |
| 枚举方法调用 | User.java:40 | `Status.fromString("ACTIVE")` | ✅ Status.fromString |
| 枚举字段声明 | User.java:17 | `private Status status` | ✅ Status |
| 跨包枚举 | Dog.java:18 | `private Status status` | ✅ Status |

### 2. 静态常量依赖 (constant/Constants.java)

| Case | 位置 | 代码示例 | 预期捕获 |
|------|------|----------|----------|
| 静态int常量 | User.java:33 | `Constants.MAX_SIZE` | ✅ Constants |
| 静态String常量 | User.java:47 | `Constants.DEFAULT_NAME` | ✅ Constants |
| 静态枚举常量 | User.java:48 | `Constants.STATUS` | ✅ Constants (枚举) |
| 静态集合常量 | Constants.java:11 | `Constants.EMPTY_LIST` | ✅ Constants |

### 3. 注解依赖 (annotation/MyAnnotation.java)

| Case | 位置 | 代码示例 | 预期捕获 |
|------|------|----------|----------|
| 方法注解 | User.java:37 | `@MyAnnotation(...)` | ✅ MyAnnotation |
| 注解属性 | User.java:37 | `value = "test", priority = 1` | ✅ MyAnnotation |

### 4. 泛型类型依赖

| Case | 位置 | 代码示例 | 预期捕获 |
|------|------|----------|----------|
| List泛型 | User.java:18 | `List<String> tags` | ✅ List |
| Map泛型 | User.java:19 | `Map<String, Object>` | ✅ Map |
| 泛型实例化 | User.java:51 | `new ArrayList<>()` | ✅ ArrayList |
| 嵌套泛型 | ComplexService.java:24 | `List<Map<String, List<User>>>` | ✅ List, Map |
| 泛型方法参数 | OrderService.java:26 | `List<User> userList` | ✅ List |
| 泛型边界 | ComplexService.java:44 | `List<? extends Number>` | ✅ List |
| 通配符 | ComplexService.java:45 | `List<? super Integer>` | ✅ List |

### 5. 强制类型转换

| Case | 位置 | 代码示例 | 预期捕获 |
|------|------|----------|----------|
| 简单类型转换 | User.java:56 | `(String) obj` | ✅ String |
| 嵌套类型转换 | Order.java:55 | `(String) obj` | ✅ String |
| 泛型强制转换 | Order.java:57 | `(List<?>) obj` | ✅ List |

### 6. 内部类依赖

| Case | 位置 | 代码示例 | 预期捕获 |
|------|------|----------|----------|
| 内部类实例化 | User.java:78 | `new InnerClass()` | ✅ InnerClass |
| 静态内部类 | User.java:91-96 | `StaticInnerClass` | ✅ StaticInnerClass |
| 内部接口 | OuterClass.java:48 | `InnerInterface` | ✅ InnerInterface |
| 匿名内部类 | UserService.java:56-61 | `new Runnable() {...}` | ✅ Runnable |
| 本地类 | OuterClass.java:26-38 | `class LocalProcessor` | ✅ LocalProcessor |

### 7. 跨包依赖

| Case | 位置 | 代码示例 | 预期捕获 |
|------|------|----------|----------|
| 跨包类引用 | Dog.java:13 | `User user` | ✅ User |
| 跨包枚举 | Dog.java:18 | `Status status` | ✅ Status |
| 跨包常量 | Dog.java:21 | `Constants.MAX_SIZE` | ✅ Constants |
| 跨包方法调用 | Dog.java:28 | `processUser(new User())` | ✅ User |

### 8. 方法调用链

| Case | 位置 | 代码示例 | 预期捕获 |
|------|------|----------|----------|
| 链式调用 | User.java:59 | `list.add("item").toString()` | ✅ add, toString |
| 多层调用 | User.java:62 | `getValue().hashCode()` | ✅ hashCode |
| 流式调用 | UserService.java:49-51 | `stream().filter().collect()` | ✅ stream, filter, collect |

### 9. Lambda表达式

| Case | 位置 | 代码示例 | 预期捕获 |
|------|------|----------|----------|
| Lambda基本 | UserService.java:49 | `u -> u.getAge() > 18` | ✅ getAge |
| Lambda块 | OuterClass.java:43-45 | `() -> { ... }` | ✅ User |
| 方法引用 | StringUtils.java:22-23 | `User::new` | ✅ User |
| 方法引用静态 | StringUtils.java:21 | `String::length` | ✅ String |

### 10. 数组类型

| Case | 位置 | 代码示例 | 预期捕获 |
|------|------|----------|----------|
| 数组字段 | ComplexService.java:17 | `User[] users` | ✅ User |
| 数组实例化 | ComplexService.java:18 | `new int[10]` | - (标准库) |
| 数组toArray | Order.java:38 | `toArray(new Item[0])` | ✅ Item |

### 11. 异常类型

| Case | 位置 | 代码示例 | 预期捕获 |
|------|------|----------|----------|
| throws子句 | UserService.java:80 | `throws IOException` | ✅ IOException |
| throw语句 | UserService.java:81 | `throw new RuntimeException` | - (标准库) |
| catch子句 | ComplexService.java:30 | `catch (Exception e)` | - (标准库) |

### 12. 条件表达式

| Case | 位置 | 代码示例 | 预期捕获 |
|------|------|----------|----------|
| 三元运算符 | UserService.java:64 | `? new User() : users.get(0)` | ✅ User |
| 三元枚举 | Order.java:29-31 | `? Status.INACTIVE : Status.ACTIVE` | ✅ Status |

### 13. 标准库过滤（应被过滤）

| Case | 位置 | 代码示例 | 预期捕获 |
|------|------|----------|----------|
| java.util.Collections | User.java:69 | `Collections.sort(list)` | ❌ 不应捕获 |
| java.io.File | UserService.java:75 | `new File("test")` | ❌ 不应捕获 |
| java.lang.RuntimeException | UserService.java:82 | `RuntimeException` | ❌ 不应捕获 |
| java.nio.file.Paths | - | `Paths.get(...)` | ❌ 不应捕获 |

## 运行测试

```bash
# 解析 mock-java-repo
java -jar target/uniast-java-parser-1.0.0-SNAPSHOT-jar-with-dependencies.jar testdata/mock-java-repo

# 或使用全局命令
java-parser testdata/mock-java-repo

# 验证结果
abcoder cli get_file_symbol 'mock-java-repo' 'src/main/java/com/example/model/User.java' 'User.doSomething'
```

## 验证脚本

```bash
# 验证枚举依赖
abcoder cli get_file_symbol 'mock-java-repo' 'src/main/java/com/example/model/User.java' 'User.doSomething' | jq '.node.dependencies[] | select(.file_path | contains("Status"))'

# 验证静态常量
abcoder cli get_file_symbol 'mock-java-repo' 'src/main/java/com/example/model/User.java' 'User.doSomething' | jq '.node.dependencies[] | select(.file_path | contains("Constants"))'

# 验证注解
abcoder cli get_file_symbol 'mock-java-repo' 'src/main/java/com/example/model/User.java' 'User.doSomething' | jq '.node.dependencies[] | select(.file_path | contains("MyAnnotation"))'
```

## 边界Case汇总

1. ✅ 枚举类型访问 (EnumType.VALUE)
2. ✅ 枚举方法调用 (EnumType.method())
3. ✅ 静态字段访问 (ClassName.STATIC_FIELD)
4. ✅ 泛型类型参数 (List<T>, Map<K,V>)
5. ✅ 强制类型转换 (Type) expr
6. ✅ 内部类实例化 outer.new InnerClass()
7. ✅ 匿名类 new Interface() {}
8. ✅ Lambda表达式 () -> {}
9. ✅ 方法引用 ClassName::method
10. ✅ 跨包依赖引用
11. ✅ 链式方法调用 obj.method1().method2()
12. ✅ 三元运算符中的类型 expr ? Type1 : Type2
13. ✅ 注解使用 @Annotation
14. ✅ 可变参数 method(Type... args)
15. ✅ 数组类型 Type[]
16. ✅ instanceof 检查
17. ❌ 标准库过滤 (java.*, javax.*)

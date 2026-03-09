package com.example.model;

import com.example.enum_.Status;
import com.example.constant.Constants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单模型 - 测试复杂依赖关系
 */
public class Order {
    private String id;
    private Status status;
    private double amount;
    private List<Item> items;
    private Map<String, String> attributes;

    public Order() {
        this.items = new ArrayList<>();
        this.attributes = new HashMap<>();
    }

    /**
     * 测试三元运算符中的类型依赖
     */
    public Status getEffectiveStatus() {
        return this.amount > Constants.MAX_SIZE
            ? Status.INACTIVE
            : Status.ACTIVE;
    }

    /**
     * 测试数组类型引用
     */
    public Item[] getItemsAsArray() {
        return items.toArray(new Item[0]);
    }

    /**
     * 测试可变参数
     */
    public void addItems(Item... items) {
        for (Item item : items) {
            this.items.add(item);
        }
    }

    /**
     * 测试instanceof和强制类型转换
     */
    public Object process(Object obj) {
        if (obj instanceof String) {
            return ((String) obj).toUpperCase();
        } else if (obj instanceof List) {
            return new ArrayList<>((List<?>) obj);
        }
        return obj;
    }

    public static class Item {
        private String name;
        private double price;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public List<Item> getItems() { return items; }
}

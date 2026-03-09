package com.example.service;

import com.example.model.User;
import com.example.model.Order;
import com.example.enum_.Status;
import java.util.List;

/**
 * 订单服务 - 测试继承和接口实现边界case
 */
public class OrderService implements IOrderService {

    private List<Order> orders;

    public OrderService() {
    }

    /**
     * 测试接口方法实现
     */
    @Override
    public Order createOrder(String id) {
        Order order = new Order();
        order.setId(id);
        return order;
    }

    /**
     * 测试泛型边界
     */
    public <T extends Order> T createGenericOrder(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试super边界
     */
    public void processOrders(List<? super Order> orderList) {
        orderList.add(new Order());
    }

    /**
     * 测试复杂泛型
     */
    public void complexGeneric(List<java.util.Map<String, ? extends Order>> maps) {
        for (java.util.Map<String, ?> map : maps) {
            System.out.println(map.size());
        }
    }
}

/**
 * 接口定义
 */
interface IOrderService {
    Order createOrder(String id);
}

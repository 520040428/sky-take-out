package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jing Beier
 * @version 1.0
 * @function
 * @date
 */

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //1.处理各种业务异常(地址簿为空，购物车数据为空)
        //查询当前订单中是否有地址
        AddressBook addressbook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressbook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //查询当前用户的购物车数据
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                                                .userId(userId)
                                                .build();
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        if(shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //2.向订单表插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressbook.getPhone());
        orders.setConsignee(addressbook.getConsignee());
        orders.setUserId(userId);

        orderMapper.insert(orders);

        //3.向订单明细表插入n条数据
        Long orderId = orders.getId();

        List<OrderDetail> orderDetails = new ArrayList<>();
        for(ShoppingCart cart: shoppingCarts){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orderId);
            orderDetails.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetails);

        //4.清空当前用户的购物车数据
        shoppingCartMapper.deleteByUserId(userId);

        //5.封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orderId)
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

        return orderSubmitVO;
    }
}

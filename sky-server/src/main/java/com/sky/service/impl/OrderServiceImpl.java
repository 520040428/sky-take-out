package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private UserMapper userMapper;

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

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

        //模拟支付成功
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        //支付成功后，根据订单号码orderNumber更新数据库订单状态——支付状态（已支付）、订单状态（待接单）、支付时间（赋值）
//        Orders orders = Orders.builder()
//                .number(ordersPaymentDTO.getOrderNumber())
//                .payStatus(Orders.PAID)
//                .status(Orders.TO_BE_CONFIRMED)
//                .checkoutTime(LocalDateTime.now())
//                .build();
//        orderMapper.updateByOrderNumber(orders);

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    @Override
    public PageResult pageQuery4User(int pageNum, int pageSize, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        //查询出我们该用户的历史订单
        Page<Orders> ordersPage = orderMapper.pageQuery(ordersPageQueryDTO);

        //历史订单List
        List<OrderVO> orderVOS = new ArrayList<>();

        if(ordersPage != null && ordersPage.getTotal() > 0){
            for(Orders orders : ordersPage){
                //得到该历史订单的orderDetail
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);
                orderVOS.add(orderVO);
            }
        }

        return new PageResult(ordersPage.getTotal(), orderVOS);
    }

    @Override
    public OrderVO details(Long orderId) {
        OrderVO orderVO = new OrderVO();

        //查询订单信息，并将orders封装到orderVO中
        Orders orders = orderMapper.getById(orderId);
        String adress = addressBookMapper.getById(orders.getAddressBookId()).getProvinceName()
                + addressBookMapper.getById(orders.getAddressBookId()).getCityName()
                + addressBookMapper.getById(orders.getAddressBookId()).getDistrictName()
                + addressBookMapper.getById(orders.getAddressBookId()).getDetail();

        orders.setAddress(adress);
        BeanUtils.copyProperties(orders, orderVO);

        //查询订单详情，把订单详情封装到orderVO中
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
        orderVO.setOrderDetailList(orderDetails);

        //这里的OrderVOextends了Order类，所以可以直接复制过去
        return orderVO;
    }

    /**
     * 再来一单
     * @param orderId
     */
    @Override
    public void repetition(Long orderId) {

        //查询当前用户id
        Long userId = BaseContext.getCurrentId();

        //根据订单id查询当前订单详情
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

        // 将订单详情对象转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetails.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());


        //将购物袋对象批量添加到数据库
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 取消订单
     * @param orderId
     */
    @Override
    public void userCancelById(Long orderId) {
        //查询订单信息
        Orders orders = orderMapper.getById(orderId);

        //校验订单是否存在
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //商家接单情况下，用户需电话沟通商家
        //派送状态下，用户取消订单需电话沟通商家
        if(orders.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //如果在待接单状态下取消订单，需要给用户退款
        if(orders.getPayStatus() == Orders.PAID){
            log.info("已退款");
            orders.setPayStatus(Orders.REFUND);
        }

        //待支付和待接单状态下，用户可以直接取消订单
        //取消订单后，需要将订单状态修改为已取消
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }


}

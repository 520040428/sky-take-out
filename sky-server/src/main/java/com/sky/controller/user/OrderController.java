package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Jing Beier
 * @version 1.0
 * @function
 * @date
 */

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){

        log.info("用户下单，参数：{}",ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);

        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);

        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());

        return Result.success(orderPaymentVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> list(int page, int pageSize, Integer status){
        log.info("历史订单查询，参数：{},{},{}",page, pageSize, status);

        PageResult pageResult = orderService.pageQuery4User(page, pageSize, status);
        return Result.success(pageResult);
    }


    @GetMapping("/orderDetail/{orderId}")
    @ApiOperation("订单详情")
    public Result<OrderVO> details(@PathVariable Long orderId){

        OrderVO orderVO = orderService.details(orderId);
        return Result.success(orderVO);
    }

    @PostMapping("/repetition/{orderId}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long orderId){
        log.info("再来一单，参数：{}", orderId);

        orderService.repetition(orderId);
        return Result.success();

    }

    @PutMapping("cancel/{orderId}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long orderId){
        log.info("取消订单，参数：{}", orderId);

        orderService.userCancelById(orderId);
        return Result.success();
    }
}

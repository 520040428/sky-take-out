package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * This class represents a controller for managing shop-related operations.
 * It handles requests related to the shop functionality and coordinates between
 * the model and view components of the application.
 *
 * @author Jing Beier
 * @version 1.0
 * @function Controls shop-related operations and request handling
 * @date [Creation date]
 */
@RestController("AdminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺相关接口")
public class ShopController {

    public final static String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    @PutMapping("/{status}")
    @ApiOperation("设置店铺的营业状态")
    public Result setStatus(@PathVariable Integer status){

        log.info("设置店铺的营业状态：{}",status == 1? "营业中": "已打烊");

        redisTemplate.opsForValue().set(KEY, status);

        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("获取店铺的营业状态")
    public Result<Integer> getStatus(){

        Integer status = (Integer)redisTemplate.opsForValue().get(KEY);

        log.info("店铺的营业状态为：{}",status == 1? "营业中": "已打烊");

        return Result.success(status);
    }
}

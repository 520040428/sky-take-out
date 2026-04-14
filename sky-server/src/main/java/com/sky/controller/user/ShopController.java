package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jing Beier
 * @version 1.0
 * @function
 * @date
 */

@RestController("UserShopController")
@RequestMapping("/user/shop")
@Api(tags = "用户查询店铺状态")
@Slf4j
public class ShopController {

    public final static String KEY = "SHOP_STATUS";

    //这里的Bean对象一定不能冲突，与admin相同
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/status")
    @ApiOperation("获取店铺的营业状态")
    public Result<Integer> getStatus(){

        Integer status = (Integer)redisTemplate.opsForValue().get(KEY);

        log.info("店铺的营业状态为：{}",status == 1? "营业中": "已打烊");

        return Result.success(status);
    }
}

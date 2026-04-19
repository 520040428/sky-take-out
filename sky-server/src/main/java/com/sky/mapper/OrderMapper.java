package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Orders;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jing Beier
 * @version 1.0
 * @function
 * @date
 */

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */

    void insert(Orders orders);
}

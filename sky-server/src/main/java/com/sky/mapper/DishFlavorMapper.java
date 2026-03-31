package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Jing Beier
 * @version 1.0
 * @function 菜品口味持久层
 * @date 2026/3/31 21:41
 */
@Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入口味数据
     * @param flavors
     */
    void insertBath(List<DishFlavor> flavors);
}

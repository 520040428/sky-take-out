package com.sky.service;

import com.sky.dto.DishDTO;
import org.springframework.stereotype.Service;

/**
 * @author Jing Beier
 * @version 1.0
 * @function 菜品管理业务层
 * @date 2026/3/31 19:30
 */
@Service
public interface DishService {

    /**
     * 新增菜品和对应的口味数据
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);
}

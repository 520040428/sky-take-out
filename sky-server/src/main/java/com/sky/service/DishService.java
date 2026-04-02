package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;

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

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 修改菜品
     * @param dishDTO
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    DishVO getByIdWithFlavor(Long id);
}

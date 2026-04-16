package com.sky.service.impl;

/**
 * @author Jing Beier
 * @version 1.0
 * @function
 * @date
 */

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //插入套餐
        setmealMapper.insert(setmeal);

        //获取生成的套餐id
        Long setmealId = setmeal.getId();

        //把DTO中的菜品拿出来
        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();

        setmealDishList.forEach(setmealDish ->{
            setmealDish.setSetmealId(setmealId);
        });

        //插入套餐和菜品之间的关系，处理setmealDishMapper
        setmealDishMapper.insertBatch(setmealDishList);

    }

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
       int pageNum = setmealPageQueryDTO.getPage();
       int pageSize = setmealPageQueryDTO.getPageSize();

       //这里自动计算分页
       PageHelper.startPage(pageNum, pageSize);

       //这里用Page对象保存返回的查询对象
       Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);

       return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    //批量删除一定是个事务3，要么同时成功要么同时失败
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //首先判断是否起售，起售的套餐不能删除
        ids.forEach(id ->{
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus() == StatusConstant.ENABLE){

                //起售中的套餐不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        //删除套餐
        ids.forEach(id -> {
            //删除套餐中的菜品
            setmealDishMapper.deleteBySetmealId(id);

            //删除套餐
            setmealMapper.deleteById(id);
        });
    }

    /**
     * 根据id查询套餐和套餐菜品关系
     * @param id
     * @return
     */
    public SetmealVO getByIdWithDish(Long id) {
        //根据id查询套菜
        Setmeal setmeal = setmealMapper.getById(id);

        //创建一个SetmealVO对象
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);

        //根据套餐id查询相应菜品
        List<SetmealDish> setmealDishs = setmealDishMapper.getBySetmealId(id);

        setmealVO.setSetmealDishes(setmealDishs);

        return setmealVO;
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    //事务，要么同时完成，要么同时失败
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //1、先修改套餐
        setmealMapper.update(setmeal);

        //套餐id
        Long setmealId = setmealDTO.getId();

        //2、删除套餐和菜品的关联关系，操作setmeal_dish表，执行delete
        setmealDishMapper.deleteBySetmealId(setmealId);

        //得到新的setmealDish,并将setmealId重置一下
        List<SetmealDish> setmealDishs = setmealDTO.getSetmealDishes();

        setmealDishs.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });

        //3、重新插入套餐和菜品的关联关系，操作setmeal_dish表，执行insert
        setmealDishMapper.insertBatch(setmealDishs);

    }

    /**
     * 套餐起售、停售
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        //1、若是起售套餐，先判断其中是否有菜品停售有的话就返回
        if(status == StatusConstant.ENABLE) {
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            if (dishList != null && dishList.size() > 0) {
                dishList.forEach(dish -> {
                    if (dish.getStatus() == StatusConstant.DISABLE) {
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }

        Setmeal setmeal  = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }


    @Override
    public List<Setmeal> list(Setmeal setmeal) {

        List<Setmeal> list = setmealMapper.list(setmeal);

        return list;
    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {

        return setmealMapper.getDishItemById(id);
    }
}
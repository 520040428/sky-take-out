package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Jing Beier
 * @version 1.0
 * @function
 * @date
 */

@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套菜相关接口")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmealCache", key = "#sermealDTO.categoryId")  // key:sermealCache::category
    public Result save(@RequestBody SetmealDTO setmealDTO) {

        setmealService.saveWithDish(setmealDTO);

        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询套餐")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);

        return Result.success(pageResult);
    }


    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)  // key:sermealCache::category
    public Result delete(@RequestParam List<Long> ids){

        setmealService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 根据id查询套餐，并回显
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id){

        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }


    @PutMapping
    @ApiOperation("修改套餐")
    public Result update(@RequestBody SetmealDTO setmealDTO){
        setmealService.update(setmealDTO);

        return Result.success();
    }


    @PostMapping("/status/{status}")
    @ApiOperation("起售或停售")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)  // key:sermealCache::category
    public Result startOrStop(@PathVariable Integer status, Long id){
        setmealService.startOrStop(status, id);

        return Result.success();
    }

}

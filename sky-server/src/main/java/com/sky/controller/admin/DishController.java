package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品管理")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("查询菜品")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("查询菜品：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        String key = "dish_" + dishPageQueryDTO.getCategoryId();
        deleteRedisCache(key);
        return Result.success(pageResult);
    }
    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result delete(@RequestParam List<Long> ids){
        log.info("删除菜品：{}", ids);
        dishService.deleteBatch(ids);

        deleteRedisCache("dish_*");
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("查询菜品详情")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("查询菜品详情：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        deleteRedisCache("dish_*");
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("修改菜品状态")
    public Result updateStatus(@RequestParam Long id, @PathVariable Integer status){
        log.info("修改菜品状态：{}", id);
        dishService.updateStatus(id, status);
        return Result.success();
    }
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        List<DishVO> dishVOList = dishService.listWithFlavor(dish);
        return Result.success(dishVOList);
    }

    private void deleteRedisCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()){
            redisTemplate.delete(keys);
        }
    }
}

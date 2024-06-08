package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    void saveWithFlavor(DishDTO dishDTO);
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);
    void deleteBatch(List<Long> ids);
    DishVO getByIdWithFlavor(Long id);
    List<DishVO> listWithFlavor(Dish dish);
    void updateWithFlavor(DishDTO dishDTO);

    void updateStatus(Long id, Integer status);
}

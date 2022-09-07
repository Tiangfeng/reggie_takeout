package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Resource
    private SetmealDishService setmealDishService;

    @Override
    @Transactional
    public void saveWithDishes(SetmealDto setmealDto) {
        this.save(setmealDto);
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();
        setmealDishList.forEach(setmealDish -> setmealDish.setSetmealId(setmealDto.getId()));
        setmealDishService.saveBatch(setmealDishList);
    }

    @Override
    @Transactional
    public void removeWithDishes(List<Long> ids) {
        // 查询套餐状态, 确认是否可以删除。
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);

        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("套餐正在售卖中, 不能删除。");
        }

        // 先删setmeal表
        this.removeByIds(ids);

        // 再删setmeal_dish表。
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);

        setmealDishService.remove(lambdaQueryWrapper);
    }
}

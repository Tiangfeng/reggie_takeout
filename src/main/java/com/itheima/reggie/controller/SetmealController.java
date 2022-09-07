package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

// 套餐管理。
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Resource
    private CategoryService categoryService;

    @Resource
    private SetmealService setmealService;

    @Resource
    private SetmealDishService setmealDishService;

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息: {}", setmealDto);
        setmealService.saveWithDishes(setmealDto);
        return R.success("新增套餐成功！");
    }

    // 套餐分页查询。
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> pageDtoInfo = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo, queryWrapper);

        BeanUtils.copyProperties(pageInfo, pageDtoInfo, "records");
        List<Setmeal> pageInfoRecords = pageInfo.getRecords();
        List<SetmealDto> setmealDtoList = pageInfoRecords.stream().map(record -> {
            SetmealDto setmealDto = new SetmealDto();

            Long categoryId = record.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            BeanUtils.copyProperties(record, setmealDto);
            return setmealDto;
        }).collect(Collectors.toList());

        pageDtoInfo.setRecords(setmealDtoList);
        return R.success(pageDtoInfo);
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        setmealService.removeWithDishes(ids);
        return R.success("套餐数据删除成功！");
    }

    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> setmealList = setmealService.list(queryWrapper);

        return R.success(setmealList);
    }

}

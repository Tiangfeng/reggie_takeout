package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Resource
    private DishService dishService;

    @Resource
    private DishFlavorService dishFlavorService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 新增菜品
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("dishDto: {}", dishDto);

        dishService.saveWithFlavor(dishDto);

        String key = "dish_" + dishDto.getCategoryId() + "_1";

        redisTemplate.delete(key);

        return R.success("新增菜品成功。");
    }

    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> pageDtoInfo = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo, queryWrapper);

        BeanUtils.copyProperties(pageInfo, pageDtoInfo, "records");

        List<Dish> dishList = pageInfo.getRecords();
        List<DishDto> dishDtoList;
        dishDtoList = dishList.stream().map(dish -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);

            Long categoryId = dish.getCategoryId();
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        pageDtoInfo.setRecords(dishDtoList);
        return R.success(pageDtoInfo);
    }

    @GetMapping("/{id}")
    public R<DishDto> getDishInfo (@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        String key = "dish_" + dishDto.getCategoryId() + "_1";

        redisTemplate.delete(key);

        return R.success("修改成功！");
    }

//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish) {
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
//        // 查询状态是1的。
//        queryWrapper.eq(Dish::getStatus, 1);
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> dishList = dishService.list(queryWrapper);
//
//        return R.success(dishList);
//    }

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        // 检查数据是否已经在缓存中。
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        List<DishDto> dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList != null) {
            return R.success(dishDtoList);
        }

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // 查询状态是1的。
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> dishList = dishService.list(queryWrapper);

        dishDtoList = new ArrayList<>();

        for (Dish d : dishList) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(d, dishDto);
            Long id = d.getId();
            LambdaQueryWrapper<DishFlavor> q = new LambdaQueryWrapper<>();
            q.eq(DishFlavor::getDishId, id);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(q);
            dishDto.setFlavors(dishFlavorList);
            dishDtoList.add(dishDto);
        }

        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }
}

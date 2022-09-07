package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Resource
    private CategoryService categoryService;

    // 新增分类。
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("category: {}", category);
        categoryService.save(category);
        return R.success("新增分类成功！");
    }

    @GetMapping("/page")
    public R<Page<Category>> page(int page, int pageSize) {
        Page<Category> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);

        categoryService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    @DeleteMapping
    public R<String> remove(Long ids) {
        log.info("删除分类, id为: {}", ids);
        categoryService.remove(ids);
        return R.success("分类信息删除成功。");
    }

    // 根据id修改分类信息。
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info("修改分类信息: {}", category);
        categoryService.updateById(category);
        return R.success("修改分类信息成功。");
    }

    // 根据条件查询分类数据。
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> categoryList = categoryService.list(queryWrapper);

        return R.success(categoryList);
    }
}

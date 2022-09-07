package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Resource
    private HttpServletRequest request;
    // 插入操作自动填充。
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充[insert]");
        log.info(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());

        Long id = BaseContext.getCurrentId();
        metaObject.setValue("createUser", id);
        metaObject.setValue("updateUser", id);
    }

    // 更新操作自动填充。
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共自动自动填充[update]");
        log.info(metaObject.toString());

        Long id = BaseContext.getCurrentId();
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", id);
    }
}

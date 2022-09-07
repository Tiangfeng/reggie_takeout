package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.util.SMSUtils;
import com.itheima.reggie.util.ValidateCodeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> setMsg(@RequestBody User user, HttpSession session) {
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)) {
            String code = ValidateCodeUtils.generateValidateCode4String(4);
            SMSUtils.sendMessage("阿里云短信测试", "SMS_154950909", "17372786602", code);
            session.setAttribute(phone, code);

            return R.success("验证发发送成功！");
        }

        return R.error("短信发送失败！");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> user, HttpSession session) {
        String phone = user.get("phone");

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);

        User u = userService.getOne(queryWrapper);
        // 若u为空, 则说明它是个新用户。
        if (u == null) {
            u = new User();
            u.setPhone(phone);
            userService.save(u);
        }
        session.setAttribute("user", u.getId());
        return R.success(u);
    }
}

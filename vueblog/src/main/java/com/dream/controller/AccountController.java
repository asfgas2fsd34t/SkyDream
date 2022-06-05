package com.dream.controller;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dream.lang.Result;
import com.dream.pojo.LoginDto;
import com.dream.pojo.MUser;
import com.dream.service.MUserService;
import com.dream.shiro.JwtToken;
import com.dream.util.JwtUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class AccountController {
    @Autowired
    MUserService userService;
    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result login(@RequestBody LoginDto loginDto, HttpServletResponse response){
        MUser user = userService.getOne(new QueryWrapper<MUser>().eq("username", loginDto.getUsername()));
        if(StringUtils.isEmpty(user)){
            throw new ShiroException("用户不存在");
        }
        if(!user.getPassword().equals(SecureUtil.md5(loginDto.getPassword()))){
            //密码不同则抛出异常
            throw new ShiroException("密码错误");
        }
        if(user.getStatus()==1){
            throw new ShiroException("用户被锁定，无法登录");
        }
        String s = jwtUtils.generateToken(user.getId());
        JwtToken jwtToken = new JwtToken(s);
        try {
            SecurityUtils.getSubject().login(jwtToken);
            response.setHeader("Authorization",s);
            response.setHeader("Access-control-Expose-Headers","Authorization");
            return Result.success(user);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @RequiresAuthentication
    @RequestMapping("/logout")
    public Result logout() {
        //退出登录
        SecurityUtils.getSubject().logout();
        return new Result(200,"退出成功",null);
    }

}

package com.dream.controller;


import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dream.lang.Result;
import com.dream.pojo.MUser;
import com.dream.pojo.UserInfo;
import com.dream.service.MUserService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zwq
 * @since 2022-04-08
 */
@RestController
@RequestMapping("/user")
public class MUserController {
    @Autowired
    MUserService userService;
    /**
     * 分页查询用户
     */
    @RequiresRoles("admin")
    @RequiresAuthentication
    @GetMapping("/list")
    public Result userList(@RequestParam(defaultValue = "1") Integer currentPage, @RequestParam(defaultValue = "10") Integer pageSize) {
        List<UserInfo> list = userService.getUserInfoList();
        int size = list.size();
        Page<UserInfo> page = new Page<>(currentPage,pageSize);
        if (pageSize > size) {
            pageSize = size;
        }
        // 求出最大页数，防止currentPage越界
        int maxPage = size % pageSize == 0 ? size / pageSize : size / pageSize + 1;
        if (currentPage > maxPage) {
            currentPage = maxPage;
        }
        // 当前页第一条数据的下标
        int curIdx = currentPage > 1 ? (currentPage - 1) * pageSize : 0;
        List<UserInfo> pageList = new ArrayList<>();
        // 将当前页的数据放进pageList
        for (int i = 0; i < pageSize && curIdx + i < size; i++) {
            pageList.add(list.get(curIdx + i));
        }
        page.setTotal(list.size()).setRecords(pageList);
        return Result.success(page);
    }

    /**
     * 创建用户
     */
    @RequiresRoles("admin")
    @RequiresAuthentication
    @PostMapping("/create")
    public Result createUser(@Validated @RequestBody MUser user){
        if(user==null){
            return Result.fail("不能为空");
        }
        else{
            if(user.getRole().contains("admin")){
                return Result.fail("禁止设置root用户");
            }
            user.setUpdateTime(new Date());
            user.setCreated(new Date());
            user.setPassword(SecureUtil.md5(user.getPassword()));
            userService.saveOrUpdate(user);
        }
        return Result.success(null);
    }


    /**
     * 修改用户信息
     */
    @RequiresRoles("admin")
    @RequiresAuthentication
    @PostMapping("/update")
    public Result updateUser(@Validated @RequestBody MUser user){
        if(user==null){
            return Result.fail("不能为空");
        }
        else{
            user.setUpdateTime(new Date());
            MUser subUser = userService.getOne(new QueryWrapper<MUser>().eq("username",user.getUsername()));
            if(subUser.getRole().equals("admin")){
                return Result.fail("禁止修改此用户");
            }
            //未修改密码
            if(user.getPassword().equals("")){
                user.setPassword(subUser.getPassword());
            }
            else{
                //存储在数据中的密码为md5加密后的
                user.setPassword(SecureUtil.md5(user.getPassword()));
            }
            userService.saveOrUpdate(user);
        }
        return Result.success(null);
    }

    /**
     * 删除用户
     */
    @RequiresRoles("admin")
    @RequiresAuthentication
    @GetMapping("/delete/{id}")
    public Result delete(@PathVariable(name = "id") Long id) {
        MUser user = userService.getById(id);
        if(user.getRole().equals("admin")){
            return Result.fail("禁止删除此用户");
        }
        if (userService.removeById(id)) {
            return Result.success(null);
        } else {
            return Result.fail("删除失败");
        }

    }

    /**
     * 修改用户的状态
     */
    @RequiresRoles("admin")
    @RequiresAuthentication
    @RequestMapping("/publish/{id}")
    public Result publish(@PathVariable(name = "id")Long id){
        MUser user = userService.getById(id);
        if(user.getRole().equals("admin")){
            return Result.fail("禁止禁用此用户");
        }
        if (user.getStatus()==0)
        {
            user.setStatus(1);
        }
        else {
            user.setStatus(0);
        }
        userService.saveOrUpdate(user);
        return Result.success(null);
    }

}

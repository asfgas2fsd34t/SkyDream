package com.dream.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dream.config.RedisKeyConfig;
import com.dream.lang.Result;
import com.dream.pojo.AccountProfile;
import com.dream.pojo.MBlog;
import com.dream.pojo.MFriends;
import com.dream.service.MFriendsService;
import com.dream.service.RedisService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zwq
 * @since 2022-05-07
 */
@RestController
public class MFriendsController {
    @Autowired
    MFriendsService friendsService;
    @Autowired
    RedisService redisService;

    @PostMapping("/friendsList")
    public Result friendsList(){
        if(redisService.hasHashKey(RedisKeyConfig.FRIEND_BLOG_CACHE,RedisKeyConfig.FRIEND_INFO_CACHE)){
            return Result.success(redisService.getValueByHashKey(RedisKeyConfig.FRIEND_BLOG_CACHE,RedisKeyConfig.FRIEND_INFO_CACHE));
        }
        List<MFriends> list = friendsService.list();
        redisService.saveKVToHash(RedisKeyConfig.FRIEND_BLOG_CACHE,RedisKeyConfig.FRIEND_INFO_CACHE,list);
        return Result.success(list);
    }

    @PostMapping("/friendsPage")
    public Result list(@RequestParam(defaultValue = "1") Integer currentPage){
        if(redisService.hasHashKey(RedisKeyConfig.FRIEND_BLOG_CACHE,currentPage)){
            return Result.success(redisService.getValueByHashKey(RedisKeyConfig.FRIEND_BLOG_CACHE,currentPage));
        }
        Page<MFriends> page = new Page<>(currentPage, 5);
        QueryWrapper<MFriends> qw = new QueryWrapper<>();
        qw.orderByDesc("created");
        IPage<MFriends> pageData = friendsService.page(page,qw);
        redisService.saveKVToHash(RedisKeyConfig.FRIEND_BLOG_CACHE,currentPage,pageData);
        return Result.success(pageData);
    }

    @RequiresAuthentication  //需要认证之后才能操作
    @RequiresRoles(value = "admin")
    @PostMapping("/addFriend")
    public Result addFriend(@RequestBody MFriends friend){
        friendsService.saveOrUpdate(friend);
        redisService.deleteCacheByKey(RedisKeyConfig.FRIEND_BLOG_CACHE);
        return Result.success("添加成功！");
    }

    @RequiresAuthentication  //需要认证之后才能操作
    @RequiresRoles(value = "admin")
    @PostMapping("/delFriend/{id}")
    public Result delFriend(@PathVariable Long id){
        friendsService.removeById(id);
        redisService.deleteCacheByKey(RedisKeyConfig.FRIEND_BLOG_CACHE);
        return Result.success("删除成功！");
    }

    @GetMapping("/friends/{id}")
    public Result getFriend(@PathVariable Integer id){
        MFriends friends = friendsService.getById(id);
        if(friends==null){
            throw new RuntimeException("朋友已经离开！");
        }
        return Result.success(friends);
    }
    /**
     * 批量删除朋友
     */
    @RequiresAuthentication
    @RequiresRoles(value = "admin")
    @PostMapping("/FriendsMulti")
    public Result BlogDelMulti(@RequestParam List<Long> ids){
        if(friendsService.removeByIds(ids)){
            redisService.deleteCacheByKey(RedisKeyConfig.FRIEND_BLOG_CACHE);
            return Result.success("删除成功！");
        }else{
            return Result.fail("删除失败！");
        }
    }
}

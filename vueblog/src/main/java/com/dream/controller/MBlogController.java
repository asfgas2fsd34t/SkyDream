package com.dream.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dream.config.RedisKeyConfig;
import com.dream.lang.Result;
import com.dream.pojo.AccountProfile;
import com.dream.pojo.MBlog;
import com.dream.pojo.MComment;
import com.dream.service.MBlogService;
import com.dream.service.MCommentService;
import com.dream.service.RedisService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zwq
 * @since 2022-04-08
 */
@RestController
public class MBlogController {
    @Autowired
    MBlogService mBlogService;
    @Autowired
    RedisService redisService;
    @Autowired
    MCommentService commentService;

    /**
     * 访问首页
     */
    @PostMapping("/blogs")
    public Result list(@RequestParam(defaultValue = "1") Integer currentPage){
        if(redisService.hasHashKey(RedisKeyConfig.BLOG_INFO_CACHE,currentPage)){
            return Result.success(redisService.getValueByHashKey(RedisKeyConfig.BLOG_INFO_CACHE,currentPage));
        }
        Page<MBlog> page = new Page<>(currentPage, 8);
        QueryWrapper<MBlog> qw = new QueryWrapper<>();
        qw.ne("title","关于我！！！");
        qw.ne("title","友情链接");
        qw.orderByDesc("created");
        IPage<MBlog> pageData = mBlogService.page(page,qw);
        redisService.saveKVToHash(RedisKeyConfig.BLOG_INFO_CACHE,currentPage,pageData);
        return Result.success(pageData);
    }
    /*
        获取所有博客
     */
    @GetMapping("/blog/all")
    @RequiresAuthentication
    @RequiresRoles("admin")
    public Result list(){
        List<MBlog> list = mBlogService.list();
        return Result.success(list);
    }
    /*
       访问归档
     */
    @GetMapping("/blog/archives")
    public Result getBlogsArchives(@RequestParam(defaultValue = "1") Integer  currentPage) {
        if(redisService.hasHashKey(RedisKeyConfig.ARCHIVE_INFO_CACHE,currentPage)){
            return   Result.success(redisService.getValueByHashKey(RedisKeyConfig.ARCHIVE_INFO_CACHE,currentPage));
        }
        int pageSize = 10;
        Page<MBlog> page = new Page<>(currentPage, pageSize);
        QueryWrapper<MBlog> qw = new QueryWrapper<>();
        qw.ne("title","关于我！！！");
        qw.orderByDesc("created");
        IPage<MBlog> pageData = mBlogService.page(page, qw);
        //进行缓存
        redisService.saveKVToHash(RedisKeyConfig.ARCHIVE_INFO_CACHE, currentPage,pageData);
        return Result.success(pageData);
    }
    /*
        访问关于我
     */
    @GetMapping("/about")
    public Result about(){
        if(redisService.hasHashKey(RedisKeyConfig.BLOG_INFO_CACHE,RedisKeyConfig.ABOUT_INFO_CACHE)){
            return Result.success(redisService.getValueByHashKey(RedisKeyConfig.BLOG_INFO_CACHE,RedisKeyConfig.ABOUT_INFO_CACHE));
        }
        List<MBlog> list = mBlogService.lambdaQuery().eq(MBlog::getTitle, "关于我！！！").list();
        redisService.saveKVToHash(RedisKeyConfig.BLOG_INFO_CACHE,RedisKeyConfig.ABOUT_INFO_CACHE, list.get(0));
        return Result.success(list.get(0));
    }
    /**
     * 访问博客详情
     */
    @PostMapping("/blog/{id}")
    public Result detail(@PathVariable Long id){
        MBlog blog = mBlogService.getById(id);
        if(blog==null){
            throw new RuntimeException("博客已被删除");
        }
        //访问量是否加一
        Map<Object, Object> mapByHash = redisService.getMapByHash(RedisKeyConfig.BLOG_VIEWS_MAP);
        int key=0;
        for (Object o : mapByHash.keySet()) {
            if(o.toString().equals(id.toString())){
                key=1;
                redisService.incrementByHashKey(RedisKeyConfig.BLOG_VIEWS_MAP, id, 1);
            }
        }
        if(key==0){
            redisService.saveKVToHash(RedisKeyConfig.BLOG_VIEWS_MAP, id, blog.getViews()+1);
        }
        return Result.success(blog);
    }

    /**
     * 编辑博客
     */
    @RequiresAuthentication  //需要认证之后才能操作
    @RequiresRoles(value = "admin")
    @PostMapping("/blog/edit")
    public Result edit(@RequestBody MBlog blog){
        //一个空对象用于赋值
        MBlog temp = null;
        //如果有id则是编辑
        AccountProfile principal = (AccountProfile) (SecurityUtils.getSubject().getPrincipal());
        if(blog.getId() != null){
            temp = mBlogService.getById(blog.getId());//将数据库的内容传递给temp
            //只能编辑自己的文章
            if(!temp.getUserId().equals(principal.getId())){
                throw new RuntimeException("你没有权限编辑");
            }
            blog.setUpdateTime(new Date());
            BeanUtil.copyProperties(blog,temp,"id","userId","created","status");
        } else {
            temp = new MBlog();
            temp.setUserId(principal.getId());
            temp.setStatus(0);
            temp.setComments(0);
            blog.setUpdateTime(new Date());
            BeanUtil.copyProperties(blog,temp,"id","userId","created","status","comments");
        }
        //将blog的值赋给temp 忽略 id userid created status 引用于hutool

        mBlogService.saveOrUpdate(temp);
        redisService.deleteCacheByKey(RedisKeyConfig.BLOG_INFO_CACHE);
        redisService.deleteCacheByKey(RedisKeyConfig.ARCHIVE_INFO_CACHE);
        redisService.deleteCacheByKey(RedisKeyConfig.CATEGORY_BLOG_CACHE);
        redisService.deleteCacheByKey(RedisKeyConfig.BLOG_VIEWS_MAP);
        return Result.success(null);
    }

    /**
     * 根据内容搜索公开博客
     */
    @GetMapping("/search")
    public Result search(@RequestParam String queryString,@RequestParam(defaultValue = "1") Integer  currentPage) {
        Page<MBlog> page = new Page<>(currentPage, 8);
        IPage<MBlog> pageData = mBlogService.page(page, new QueryWrapper<MBlog>().like("title", queryString).eq("status", 0).orderByDesc("created"));
        return Result.success(pageData);
    }

    /**
     * 删除博客
     */
    @RequiresAuthentication
    @RequiresRoles(value = "admin")
    @PostMapping("/blogdel/{id}")
    public Result del(@PathVariable Long id){
        boolean b = mBlogService.removeById(id);
        commentService.remove(new QueryWrapper<MComment>().eq("blogId",id));
        //判断是否为空 为空则断言异常
        if(b){
            redisService.deleteCacheByKey(RedisKeyConfig.BLOG_INFO_CACHE);
            redisService.deleteCacheByKey(RedisKeyConfig.ARCHIVE_INFO_CACHE);
            redisService.deleteCacheByKey(RedisKeyConfig.CATEGORY_BLOG_CACHE);
            redisService.deleteCacheByKey(RedisKeyConfig.BLOG_VIEWS_MAP);
            return Result.success("文章删除成功");
        }else{
            return Result.fail("文章删除失败");
        }
    }

    /**
     * 批量删除博客
     */
    @RequiresAuthentication
    @RequiresRoles(value = "admin")
    @PostMapping("/BlogDelMulti")
    public Result BlogDelMulti(@RequestParam List<Long> ids){
        if(mBlogService.removeByIds(ids)){
            redisService.deleteCacheByKey(RedisKeyConfig.BLOG_INFO_CACHE);
            redisService.deleteCacheByKey(RedisKeyConfig.ARCHIVE_INFO_CACHE);
            redisService.deleteCacheByKey(RedisKeyConfig.CATEGORY_BLOG_CACHE);
            redisService.deleteCacheByKey(RedisKeyConfig.BLOG_VIEWS_MAP);
            return Result.success("删除成功！");
        }else{
            return Result.fail("删除失败！");
        }
    }
}

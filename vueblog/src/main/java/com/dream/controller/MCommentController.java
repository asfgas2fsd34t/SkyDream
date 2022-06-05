package com.dream.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dream.config.RedisKeyConfig;
import com.dream.lang.Result;
import com.dream.pojo.MBlog;
import com.dream.pojo.MComment;
import com.dream.service.MBlogService;
import com.dream.service.MCommentService;
import com.dream.service.RedisService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zwq
 * @since 2022-05-26
 */
@RestController
public class MCommentController {
    @Autowired
    MCommentService commentService;
    @Autowired
    MBlogService blogService;
    @Autowired
    RedisService redisService;
    /**
     * 分页查询所有评论
     */
    @GetMapping("/commentList")
    public Result getCommentListByPage(@RequestParam(defaultValue = "1") Integer currentPage, @RequestParam(defaultValue = "10") Integer pageSize ) {
        Page<MComment> page = new Page<>(currentPage, pageSize);
        IPage<MComment> pageData = commentService.page(page, new QueryWrapper<MComment>().orderByDesc("created"));
        return Result.success(pageData);
    }


    /**
     * 分页查询某个博客下的根评论
     */
    @RequiresAuthentication
    @GetMapping("/comment/detail")
    public Result getCommentListByPageId(@RequestParam(defaultValue = "1") Long blogId, @RequestParam(defaultValue = "1") Integer currentPage,@RequestParam(defaultValue = "10") Integer pageSize ) {
        Page<MComment> page = new Page<>(currentPage, pageSize);
        IPage<MComment> pageData =null;
        if(blogId==1){
            pageData=commentService.page(page, new QueryWrapper<MComment>().orderByDesc("created"));
        }else{
            pageData = commentService.page(page, new QueryWrapper<MComment>().eq("blogId",blogId).orderByDesc("created"));
        }

        return Result.success(pageData);

    }


    /**
     * 获取某个博客下的所有根评论
     */
    @GetMapping("/comment/{blogId}")
    public Result getCommentByBlogId(@PathVariable(name = "blogId") Long blogId) {

        List<MComment> pageMComments = commentService.list(new QueryWrapper<MComment>().eq("blogId",blogId).eq("parentCommentId",-1));
//        System.out.println(pageMComments);
        for (MComment pageMComment : pageMComments) {
            List<MComment> reply = commentService.list(new QueryWrapper<MComment>().eq("parentCommentId", pageMComment.getId()));
            pageMComment.setReplyComments(reply);
        }
        return Result.success(pageMComments);

    }


    /**
     * 修改评论
     */
    @RequiresAuthentication
    @RequiresRoles("admin")
    @RequestMapping("comment/update")
    public Result updateById(@RequestBody MComment MComment){
        if(MComment ==null){
            return Result.fail("不能为空");
        }

        commentService.saveOrUpdate(MComment);
        return Result.success(null);

    }

    /**
     * 删除评论
     */
    @RequiresRoles("admin")
    @RequiresAuthentication
    @RequestMapping("comment/delete/{id}")
    public Result delete(@PathVariable(name = "id")String id){
        MComment comment = commentService.getById(id);
        MBlog byId = blogService.getById(comment.getBlogId());
        byId.setComments(byId.getComments()-1);
        blogService.saveOrUpdate(byId);
        if (commentService.removeById(id)) {
            return Result.success(null);
        } else {
            return Result.fail("删除失败");
        }

    }

    /**
     * 提交评论
     */
    @PostMapping("/comment/add")
    public Result edit(@RequestBody MComment MComment, HttpServletRequest request) {
        if (MComment.getContent().contains("<script>") || MComment.getEmail().contains("<script>") || MComment.getNickname().contains("<script>")) {
            return Result.fail("非法输入");
        }

        MBlog blog = blogService.getById(MComment.getBlogId());
        blog.setComments(blog.getComments()+1);
        blogService.saveOrUpdate(blog);
        redisService.deleteCacheByKey(RedisKeyConfig.BLOG_INFO_CACHE);
        redisService.deleteCacheByKey(RedisKeyConfig.ARCHIVE_INFO_CACHE);
        redisService.deleteCacheByKey(RedisKeyConfig.CATEGORY_BLOG_CACHE);
        com.dream.pojo.MComment temp = new MComment();
//        temp.setIp(request.getRemoteAddr());
        temp.setIp(request.getHeader("x-forwarded-for"));
        BeanUtil.copyProperties(MComment, temp, "id", "ip", "created");

        commentService.saveOrUpdate(temp);
        return Result.success(null);


//        //博主的回复向被回复者发送提示邮件
//        if(comment.getIsAdminComment()==1&&comment.getParentCommentId()!=-1){
//            Comment parentComment = commentService.getOne(new QueryWrapper<Comment>().eq("nickname", comment.getParentCommentNickname()));
//            String regex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
//            if (parentComment.getEmail().matches(regex)) {
//                mailService.sendSimpleMail(parentComment.getEmail(), "Skymo博客评论回复", "您的的评论："+parentComment.getContent()+"\n博主回复内容："+comment.getContent());
//                logger.info("邮件发送成功");
//            }
//
//        }

    }
}

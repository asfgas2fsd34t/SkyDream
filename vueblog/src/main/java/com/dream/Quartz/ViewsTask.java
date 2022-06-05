package com.dream.Quartz;

import com.dream.config.RedisKeyConfig;
import com.dream.pojo.MBlog;
import com.dream.service.MBlogService;
import com.dream.service.RedisService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.io.Serializable;
import java.util.Map;

public class ViewsTask extends QuartzJobBean {
    @Autowired
    RedisService redisService;
    @Autowired
    MBlogService blogService;
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Map<Object, Object> mapByHash = redisService.getMapByHash(RedisKeyConfig.BLOG_VIEWS_MAP);
        for (Object o : mapByHash.keySet()) {

            MBlog blog = blogService.getById((Integer)o);
            System.out.println(blog.getViews());
            blog.setViews((Integer) redisService.getValueByHashKey(RedisKeyConfig.BLOG_VIEWS_MAP,o));
            blogService.updateById(blog);
            redisService.deleteCacheByKey(RedisKeyConfig.BLOG_INFO_CACHE);
        }
    }
}

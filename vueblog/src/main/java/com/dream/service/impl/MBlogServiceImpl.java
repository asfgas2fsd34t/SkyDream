package com.dream.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dream.Mapper.MUserMapper;
import com.dream.lang.Result;
import com.dream.pojo.MBlog;
import com.dream.Mapper.MBlogMapper;
import com.dream.service.MBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dream.service.MUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zwq
 * @since 2022-04-08
 */
@Service
public class MBlogServiceImpl extends ServiceImpl<MBlogMapper, MBlog> implements MBlogService {

}

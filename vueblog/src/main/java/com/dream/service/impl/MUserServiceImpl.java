package com.dream.service.impl;

import com.dream.pojo.MUser;
import com.dream.Mapper.MUserMapper;
import com.dream.pojo.UserInfo;
import com.dream.service.MUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zwq
 * @since 2022-04-08
 */
@Service
public class MUserServiceImpl extends ServiceImpl<MUserMapper, MUser> implements MUserService {
    @Autowired
    MUserMapper userMapper;

    /**
     * 查询所有用户（只含有部分信息）
     *
     * @return 用户（只含有部分信息）list
     */
    @Override
    public List<UserInfo> getUserInfoList(){
        return userMapper.getUserInfo();
    }
}

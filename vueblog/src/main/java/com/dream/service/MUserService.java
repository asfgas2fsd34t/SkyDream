package com.dream.service;

import com.dream.pojo.MUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dream.pojo.UserInfo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zwq
 * @since 2022-04-08
 */
public interface MUserService extends IService<MUser> {
        /**
         * 查询所有用户（只含有部分信息）
         *
         * @return 用户（只含有部分信息）list
         */
        List<UserInfo> getUserInfoList();
}

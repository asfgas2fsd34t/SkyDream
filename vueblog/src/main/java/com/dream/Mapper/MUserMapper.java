package com.dream.Mapper;

import com.dream.pojo.MUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dream.pojo.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zwq
 * @since 2022-04-08
 */
@Mapper
public interface MUserMapper extends BaseMapper<MUser> {
    /**
     * 获取用户部分信息list
     */
    @Select("select id, nickname, username, avatar, email, status, created, update_time, role  from m_user order by created desc")
    List<UserInfo> getUserInfo();
}

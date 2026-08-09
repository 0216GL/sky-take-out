package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据openid查询用户
     */
    User getByOpenid(String openid);
}

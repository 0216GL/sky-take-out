package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据微信 openid 查询用户
     */
    @Select("select * from `user` where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 统计指定时间段内注册的用户数（用于工作台新增用户）
     */
    @Select("select count(id) from `user` where create_time between #{begin} and #{end}")
    Integer countByTime(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);
}

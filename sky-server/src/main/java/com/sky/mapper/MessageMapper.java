package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 消息 Mapper：操作 message 表
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 把指定 id 的消息标记为已读
     */
    @Update("update message set is_read = 1 where id = #{id}")
    void markAsRead(Long id);

    /**
     * 把指定 id 列表的消息全部标记为已读
     */
    @Update("<script>" +
            "update message set is_read = 1 where id in " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    void batchMarkAsRead(java.util.List<Long> ids);

    /**
     * 删除所有已读消息
     */
    @Update("delete from message where is_read = 1")
    void deleteRead();
}

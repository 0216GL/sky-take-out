package com.sky.service;

import com.sky.dto.MessagePageQueryDTO;
import com.sky.entity.Message;
import com.sky.result.PageResult;

import java.util.List;

/**
 * 消息服务：管理端"消息中心"的查询与已读操作。
 */
public interface MessageService {

    /**
     * 分页查询消息（可按已读/未读筛选）
     */
    PageResult pageQuery(MessagePageQueryDTO messagePageQueryDTO);

    /**
     * 查询未读消息数量
     */
    Integer countUnread();

    /**
     * 批量标记已读
     */
    void batchMarkAsRead(List<Long> ids);

    /**
     * 单条标记已读
     */
    void markAsRead(Long id);

    /**
     * 删除所有已读消息
     */
    void deleteRead();
}

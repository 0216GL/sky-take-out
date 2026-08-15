package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.MessagePageQueryDTO;
import com.sky.entity.Message;
import com.sky.mapper.MessageMapper;
import com.sky.result.PageResult;
import com.sky.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息服务实现
 */
@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    /**
     * 分页查询消息
     * status：1 未读，2 已读；不传则查全部
     */
    @Override
    public PageResult pageQuery(MessagePageQueryDTO messagePageQueryDTO) {
        Page<Message> pageParam = new Page<>(messagePageQueryDTO.getPageNum(), messagePageQueryDTO.getPageSize());

        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        // 按已读状态筛选
        if (messagePageQueryDTO.getStatus() != null) {
            int isRead = messagePageQueryDTO.getStatus() == 1 ? 0 : 1;
            queryWrapper.eq(Message::getIsRead, isRead);
        }
        // 最新的消息排前面
        queryWrapper.orderByDesc(Message::getCreateTime);

        // MyBatis-Plus 分页查询：自动带 total 和记录列表
        Page<Message> page = messageMapper.selectPage(pageParam, queryWrapper);
        return new PageResult(page.getTotal(), page.getRecords());
    }

    /**
     * 统计未读消息数量
     */
    @Override
    public Integer countUnread() {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getIsRead, 0);
        return Math.toIntExact(messageMapper.selectCount(queryWrapper));
    }

    /**
     * 批量标记已读
     */
    @Override
    public void batchMarkAsRead(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            messageMapper.batchMarkAsRead(ids);
        }
    }

    /**
     * 单条标记已读
     */
    @Override
    public void markAsRead(Long id) {
        messageMapper.markAsRead(id);
    }

    /**
     * 删除所有已读消息
     */
    @Override
    public void deleteRead() {
        messageMapper.deleteRead();
    }
}

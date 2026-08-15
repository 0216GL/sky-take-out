package com.sky.controller.admin;

import com.sky.dto.MessagePageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器：管理端"消息中心"的接口。
 */
@RestController("adminMessageController")
@RequestMapping("/admin/messages")
@Slf4j
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 分页查询消息（status：1 未读，2 已读）
     */
    @GetMapping("/page")
    public Result<PageResult> page(MessagePageQueryDTO messagePageQueryDTO) {
        return Result.success(messageService.pageQuery(messagePageQueryDTO));
    }

    /**
     * 查询未读消息数量
     */
    @GetMapping("/countUnread")
    public Result<Integer> countUnread() {
        return Result.success(messageService.countUnread());
    }

    /**
     * 批量标记已读（请求体传 id 数组）
     */
    @PutMapping("/batch")
    public Result batch(@RequestBody List<Long> ids) {
        messageService.batchMarkAsRead(ids);
        return Result.success();
    }

    /**
     * 单条标记已读
     */
    @PutMapping("/{id}")
    public Result setStatus(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.success();
    }

    /**
     * 删除所有已读消息
     */
    @DeleteMapping("/deleteRead")
    public Result deleteRead() {
        messageService.deleteRead();
        return Result.success();
    }
}

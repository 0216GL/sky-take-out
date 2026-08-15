package com.sky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息实体：管理端"消息中心"的通知消息。
 * 类型说明：1 待接单，2 急单待接单，3 待派送，4 催单，5 今日数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息内容（前端会按空格拆分展示，如"您有新的待接单订单, 订单号, 请及时处理"） */
    private String content;

    /** 消息详情（JSON 字符串，如催单时的订单信息、今日数据时的统计信息） */
    private String details;

    /** 消息类型：1 待接单 2 急单待接单 3 待派送 4 催单 5 今日数据 */
    private Integer type;

    /** 是否已读：0 未读，1 已读 */
    private Integer isRead;

    /** 消息创建时间 */
    private LocalDateTime createTime;
}

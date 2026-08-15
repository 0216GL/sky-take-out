package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息分页查询 DTO
 */
@Data
public class MessagePageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 页码，从 1 开始 */
    private int pageNum = 1;

    /** 每页条数 */
    private int pageSize = 10;

    /** 消息状态：1 未读，2 已读 */
    private Integer status;
}

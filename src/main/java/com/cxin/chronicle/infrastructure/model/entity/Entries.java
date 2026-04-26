package com.cxin.chronicle.infrastructure.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 记录表
 * </p>
 *
 * @author Charles Chen
 * @since 2026-04-25
 */
@Getter
@Setter
@ToString
@TableName("entries")
public class Entries implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     */
    @TableField("userId")
    private String userId;

    /**
     * 记录内容
     */
    @TableField("content")
    private String content;

    /**
     * 记录类型
     */
    @TableField("entryType")
    private String entryType;

    /**
     * 是否勾选
     */
    @TableField("checked")
    private Byte checked;

    /**
     * 创建时间
     */
    @TableField("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField("isDelete")
    private Byte isDelete;
}

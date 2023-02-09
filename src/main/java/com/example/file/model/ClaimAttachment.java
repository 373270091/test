package com.example.file.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 附件表2.0
 * </p>
 *
 * @author xp
 * @since 2022-05-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("claim_attachment")
public class ClaimAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long claimPrimaryId;

    private Byte type;

    private String path;

    private Byte deleteFlag;

    private Byte supplyFlag;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String pageId;

    private String no;

    private Long damageId;
}

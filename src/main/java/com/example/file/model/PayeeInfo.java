package com.example.file.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 领款人表2.0
 * </p>
 *
 * @author xp
 * @since 2022-05-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("payee_info")
public class PayeeInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long masterId;

    private String payeeAccountBank;

    private String payeeAccountName;

    private String idCard;

    private String payeeAccountId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private LocalDateTime updateTime;

    private Byte deleteFlag;

    private String bankImg;

    private String no;
}

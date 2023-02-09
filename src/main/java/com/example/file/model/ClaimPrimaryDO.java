package com.example.file.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 案件信息2.0
 * </p>
 *
 * @author xp
 * @since 2022-06-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("claim_primary")
public class ClaimPrimaryDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String applyNo;

    private String registNo;

    private String compensateNo;

    private Byte applyType;

    private Long damageId;

    private LocalDateTime accidentTime;

    private String accidentCourse;

    private String productName;

    private String policyNo;

    private Long policyId;

    private BigDecimal applyAmount;

    private BigDecimal actualAmount;

    private String remark;

    private Byte status;

    private LocalDateTime cancelTime;

    private LocalDateTime acceptTime;

    private LocalDateTime closeTime;

    private Byte modType;

    private Byte deleteFlag;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;

    private String damageName;

    private String damageCareNo;

    private Byte damageCardType;

    private Byte relationship;

    private Long payeeInfoId;

    private String payeeBank;

    private String payeeName;

    private String payeeBankCard;

    private String payeeIdCard;

    private Byte payType;

    private Byte damageReason;

    private String diseaseReason;

    private String damageAreaCode;

    private String damageAddress;

    private String claimNo;

    private Long applyId;

    private String thirdClaimNo;

    private Byte pushTag;
}

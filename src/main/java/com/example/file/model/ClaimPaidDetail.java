package com.example.file.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Builder
public class ClaimPaidDetail implements Serializable {

    private static final long serialVersionUID = -2208710689334473674L;
    /**
     * 收付系统键值
     */
    private String keyFromINTEGRAL;
    /**
     * 保单号
     */
    private String policyNum;
    /**
     * 赔案号
     */
    private String claimNum;
    /**
     * 赔付次数
     */
    private Integer lossNum;
    /**
     * 序号
     */
    private Integer seq;
    /**
     * 任务号
     */
    private Integer claimPaymentId;
    /**
     * 领款人代码
     */
    private String payeeCode;
    /**
     * 领款人名称
     */
    private String payeeName;
    /**
     * 费用类型
     */
    private String feeType;
    /**
     * 应付币种
     */
    private String payableCurrency;
    /**
     * 应付金额
     */
    private BigDecimal payableAmount;
    /**
     * 实付币种
     */
    private String paidCurrency;
    /**
     * 实付币种
     */
    private BigDecimal paidAmount;
    /**
     * 付款方式
     */
    private String payMethod;
    /**
     * 我方银行账号
     */
    private String bankAccount;
    /**
     * 对方银行账号
     */
    private String payeeBankAccount;
    /**
     * 对方银行名称
     */
    private String payorBankName;
    /**
     * 付款时间
     */
    private String paidDate;
    /**
     * 共保人代码
     */
    private String coinNum;
    /**
     * 平台失败标志
     */
    private Character platformFailFlag;
    /**
     * 平台错误信息
     */
    private String platformErrorMessage;
    /**
     * 收付状态
     * SUCC-收付成功；FAIL-支付失败；UNPS-未处理；PSNG-已处理，NA-失效；N-接口未处理；E-接口处理失败
     */
    private String status;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 应收应付挂账日期
     */
    private Date accountingDate;
    /**
     * 渠道编码
     */
    private String channel;
    /**
     * 交易方银行编码
     */
    private String oppBankCode;
    /**
     * 交易方区域编码
     */
    private String oppAreaCode;
    /**
     * 交易方区域编码
     */
    private Integer cNAPSCode;
    /**
     * 加急标志
     */
    private Integer FastFlag;
    /**
     * 用途
     */
    private String purpose;
    /**
     * 卡折类型
     */
    private String cardType;
    /**
     * 结算单号
     */
    private String balanceNumber;
    /**
     * 理赔接口序列号
     */
    private String externalClaimNumber;
    /**
     * 赔案接口序列号
     */
    private String claimInterfaceSeqNo;

}

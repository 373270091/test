package com.example.file.model;

import lombok.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @description: 支付状态查询请求体
 * "输入查询条件
 * 最小查询条件为“保单号/赔案号”，其他条件均可为空（即至少具备“保单号/赔案号”不为空，作为查询条件）。
 * INTEGRAL在处理为空的查询条件时，应当忽略该条件。PaidQueryData"
 * @author xupeng
 * @date 2022/6/27 17:54
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "PaidQueryData")
public class PaidQueryData implements Serializable {

    private static final long serialVersionUID = -948021387270192600L;
    /**
     * 保单号
     */
    @XmlElement(name = "PolicyNum")
    private String PolicyNum;
    /**
     * 赔案号
     */
    private String ClaimNum;
    /**
     * 赔案结算单号
     */
    private String BalanceNum;
    /**
     * 赔付次数
     */
    private String LossNum;
    /**
     * 赔案类型
     * 0-预付、1-已决、2-垫付、3-追偿
     */
    private String ClaimType;
    /**
     * 收付状态
     * SUCC-支付成功；FAIL-支付失败；UNPS-未处理；PSNG-已处理；
     */
    private String Status;
    /**
     * 共保人代码
     */
    private String CoinNum;
    /**
     * 应付金额
     */
    private String PayableAmount;
    /**
     * 产品分类
     * 空：所有产品  1：车险产品  非空非1：非车险的其他所有产品
     */
    private String Prodkind;
    /**
     * 请求系统:
     * "CICP 信保系统
     * reinsure 再保系统
     * yxwyx 永鑫微营销
     * VHL 车核心
     * V7_NVHL 非车新核心
     * V5 老非车系统
     * Life 意健险
     * FT 车理赔
     * NCLAIM 非车理赔  LifeClaim 意健险理赔 OTHER 其他"
     */
    @XmlElement(name = "RequestSystem")
    private String RequestSystem = "LifeClaim";
}

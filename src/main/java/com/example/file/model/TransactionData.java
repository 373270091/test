package com.example.file.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @description: 支付状态查询封装
 * @author xupeng
 * @date 2022/6/27 17:55
 */
@XmlRootElement(name = "TransactionData")
public class TransactionData implements Serializable {
    private static final long serialVersionUID = -798557075586628150L;
    /**
     * 请求头
     */
    private PaidQueryHeader PaidQueryHeader;
    /**
     * 请求体
     */
    private PaidQueryData PaidQueryData;

    /**
     * 响应头
     */
    private PaidQueryResultHeader paidQueryResultHeader;
    /**
     * 响应体
     */
    private PaidQueryResultData paidQueryResultData;

    @XmlElement(name = "PaidQueryHeader")
    public com.example.file.model.PaidQueryHeader getPaidQueryHeader() {
        return PaidQueryHeader;
    }

    public void setPaidQueryHeader(com.example.file.model.PaidQueryHeader paidQueryHeader) {
        PaidQueryHeader = paidQueryHeader;
    }

    @XmlElement(name = "PaidQueryData")
    public com.example.file.model.PaidQueryData getPaidQueryData() {
        return PaidQueryData;
    }

    public void setPaidQueryData(com.example.file.model.PaidQueryData paidQueryData) {
        PaidQueryData = paidQueryData;
    }

    public PaidQueryResultHeader getPaidQueryResultHeader() {
        return paidQueryResultHeader;
    }

    public void setPaidQueryResultHeader(PaidQueryResultHeader paidQueryResultHeader) {
        this.paidQueryResultHeader = paidQueryResultHeader;
    }

    public PaidQueryResultData getPaidQueryResultData() {
        return paidQueryResultData;
    }

    public void setPaidQueryResultData(PaidQueryResultData paidQueryResultData) {
        this.paidQueryResultData = paidQueryResultData;
    }
}

package com.example.file.model;

import lombok.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 * @description: 支付状态查询请求头
 * @author xupeng
 * @date 2022/6/27 17:53
 */
@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "PaidQueryHeader")
public class PaidQueryHeader implements Serializable {

    private static final long serialVersionUID = -9145837586106377163L;
    @XmlElement(name = "Entity")
    private String Entity = "QR";
    @XmlElement(name = "TransactionType")
    private String TransactionType = "Q_C_P_Q";
    @XmlElement(name = "Deploy")
    private String Deploy = "R";
    @XmlElement(name = "CreatTime")
    private String CreatTime;
    @XmlElement(name = "CreatedBy")
    private String CreatedBy = "0";
    @XmlElement(name = "PostTime")
    private String PostTime;
    @XmlElement(name = "PostedBy")
    private String PostedBy = "0";
    @XmlElement(name = "UpdateBy")
    private String UpdateBy = "0";
    @XmlElement(name = "UpdateTime")
    private String UpdateTime;
}

package com.example.file.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 响应头
 * @author xupeng
 * @date 2022/6/27 18:00
 */

@Getter
@Setter
@Builder
public class PaidQueryResultHeader implements Serializable {

    private static final long serialVersionUID = -5861077153419045296L;
    private String entity;
    private String transactionType;
    private String deploy;
    private Date creatTime;
    private String createdBy;
    private Date postTime;
    private String postedBy;
    private String updateBy;
    private Date updateTime;
}

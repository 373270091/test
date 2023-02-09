package com.example.file.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
public class PaidQueryResultData implements Serializable {
    private static final long serialVersionUID = -7317521305740984425L;
    /**
     * 记录笔数
     */
    private String count;
    /**
     * 应付合计
     */
    private String sumofPayable;
    /**
     * 实付合计
     */
    private String sumofPaid;
    private List<ClaimPaidDetail> claimPaidDetail;
}

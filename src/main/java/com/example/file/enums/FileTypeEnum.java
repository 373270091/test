package com.example.file.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 好管家映射影像件的文件类型
 */
@Getter
@AllArgsConstructor
public enum FileTypeEnum {

    /**
     * hgj：发票
     * yxj：医疗费结算单
     */
    EXPENSE_STATEMENT((byte)1,"CL4404"),
    /**
     * hgj:就诊
     * yxj：病历
     */
    MEDICAL_RECORD((byte)2,"CL4403"),
    /**
     * hxj：其他
     * yxj：其他
     */
    OTHER((byte)3,"CL4409"),
    /**
     * hxj：理赔申请书
     * yxj：申请书
     */
    CLAIM_PDF((byte)4,"CL4101"),
    /**
     * hxj：银行卡
     * yxj：收款人身份证明
     */
    BANK_IMG((byte)6,"CL4503"),
    /**
     * hxj：身份证正面照
     * yxj：被保险人身份证明
     */
    ID_CARD_FACE((byte)7,"CL4112"),
    /**
     * hxj：身份证反面照
     * yxj：被保险人身份证明
     */
    ID_CARD_BACK((byte)8,"CL4112"),
    /**
     * hxj：签名
     * yxj：其他
     */
    SIGNATURE((byte)9,"CL4409"),
    /**
     * hxj：个人常用资料
     * yxj：其他证明材料
     */
    COMMON_DATA((byte)10,"CL4504"),
    /**
     * hxj：授权委托书
     * yxj：受益人与被保险人关系证明
     */
    AUTHORIZATION((byte)11,"CL4114");

    private byte hgj;
    private String yxj;

    public static String getYxjByHgj(byte hgj) {
        for (FileTypeEnum item : FileTypeEnum.values()) {
            if (item.getHgj() == hgj) {
                return item.getYxj();
            }
        }
        return OTHER.getYxj();
    }
}

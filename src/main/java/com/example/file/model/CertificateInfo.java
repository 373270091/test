package com.example.file.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 人员对应的证件照
 * </p>
 *
 * @author xp
 * @since 2022-06-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@TableName("certificate_info")
public class CertificateInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long insuPersonFamilyId;

    private String faceImgPath;

    private String backImgPath;

    private Byte deleteFlag;

    private String activeBegin;

    private String activeEnd;
}

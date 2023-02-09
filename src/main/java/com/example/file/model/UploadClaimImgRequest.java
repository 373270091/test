package com.example.file.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class UploadClaimImgRequest {

    /**
     * 影像件地址
     */
    private Map<String,Byte> fileMap;
    /**
     * 报案号
     */
    private String registNo;
}

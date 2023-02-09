package com.example.file.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExcelVO {

    private long id;

    private String path;

    private int type;

    private String registNo;

    private String newPath;
}

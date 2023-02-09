package com.example.file.service;


import com.example.file.model.ClaimAttachment;

import java.util.List;

public interface FileMigrationService {

    void fileMigration(List<ClaimAttachment> list);
}

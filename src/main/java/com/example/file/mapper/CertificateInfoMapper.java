package com.example.file.mapper;

import com.example.file.config.MyBaseMapper;
import com.example.file.model.CertificateInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 人员对应的证件照 Mapper 接口
 * </p>
 *
 * @author xp
 * @since 2022-06-06
 */
@Mapper
public interface CertificateInfoMapper extends MyBaseMapper<CertificateInfo> {

}

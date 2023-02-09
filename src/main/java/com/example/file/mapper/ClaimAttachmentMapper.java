package com.example.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.file.config.MyBaseMapper;
import com.example.file.model.ClaimAttachment;
import org.apache.cxf.security.claims.authorization.Claim;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.annotation.MapperScan;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 附件表2.0 Mapper 接口
 * </p>
 *
 * @author xp
 * @since 2022-05-26
 */
@MapperScan
@Mapper
public interface ClaimAttachmentMapper extends MyBaseMapper<ClaimAttachment> {

    List<ClaimAttachment> getList(@Param("no")String no);

    List<ClaimAttachment> recover(@Param("id")String id);

    String getIdCardPath(@Param("path")String path,@Param("id")long id ,@Param("fileName")String fileName);

    String getPath(@Param("id")long id ,@Param("fileName")String fileName);

    void updateState(@Param("id")Long id,@Param("state")Byte state);

    long getDamageId(@Param("registNo")String registNo);

    List<ClaimAttachment> sx();

    List<ClaimAttachment> selectClaimId(@Param("id")Long id);

    List<Map<String,String>> selectMapList(@Param("list") List<Long> list);

    List<Map<String,Object>> selectIdList(@Param("list") List<Long> list,@Param("fieldStr")String fieldStr);

    List<Map<String,String>> selectIdCard(@Param("id") Long id);

    List<Map<String,String>> selectPayee(@Param("id") String id);

    List<ClaimAttachment> selectOther();

    List<String> selectRegistNoByDamageId(@Param("id") Long id);

    List<ClaimAttachment> selectMigrationData(@Param("id")long id);


    List<ClaimAttachment> selectSupplyFlag();
}

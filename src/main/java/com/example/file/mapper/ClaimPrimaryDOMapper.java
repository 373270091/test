package com.example.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.file.model.ClaimPrimaryDO;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 案件信息2.0 Mapper 接口
 * </p>
 *
 * @author xp
 * @since 2022-05-26
 */
@Repository
public interface ClaimPrimaryDOMapper extends BaseMapper<ClaimPrimaryDO> {

    /**
     * 根据申请编号修改状态
     * @param list      申请编号
     * @param state     目标状态
     * @param remark    备注
     */
    void updateStateByApplyNo(@Param("list") List<String> list,@Param("state")Byte state,@Param("remark")String remark);

    /**
     * 模拟核心修改数据
     * @param map
     * @return
     */
    int testData(@Param("map") Map<String,Object> map);

    /**
     * 根据报案号修改状态
     * @param claimPrimaryDO
     * @return
     */
    int updateStateByRegistNo(@Param("data")ClaimPrimaryDO claimPrimaryDO);

    /**
     * 根据当前登录id获取客户上一个理赔案件中的出险地址
     * @param userId
     * @return
     */
    String getAddressById(@Param("userId")Integer userId);
}

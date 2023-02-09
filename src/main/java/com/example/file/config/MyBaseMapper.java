package com.example.file.config;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * @description: 自定义方法
 * @author xupeng
 * @date 2022/5/31 10:50
 * @param <T>
 */
public interface MyBaseMapper<T> extends BaseMapper<T> {

    /**
     * 根据主键id修改非空的字段
     * @param entity
     * @return
     */
    int updateByIdWithNotNull(@Param(Constants.ENTITY) T entity);

    /**
     * 批量插入
     * @param entityList
     * @return
     */
    int insertBatchSomeColumn(Collection<T> entityList);

    int alwaysUpdateSomeColumnById(Collection<T> entityList);
}

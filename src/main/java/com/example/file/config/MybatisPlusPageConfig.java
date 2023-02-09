package com.example.file.config;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.injector.methods.AlwaysUpdateSomeColumnById;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @description:
 * @author: JessonCoder
 * @time: 2021/5/4
 */
@Configuration
public class MybatisPlusPageConfig {

    @Bean
    public DefaultSqlInjector logicSqlInjector() {
        return new DefaultSqlInjector(){
            public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo){
                List<AbstractMethod> methodList = super.getMethodList(mapperClass,tableInfo);
                methodList.add(new AlwaysUpdateSomeColumnById());
                return methodList;
            }
        };
    }
}

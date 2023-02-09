package com.example.file.controller;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.example.file.mapper.ClaimAttachmentMapper;
import com.example.file.model.ClaimAttachment;
import com.example.file.utils.FileMigrationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

@RestController
@Slf4j
@RequestMapping(value = "/fileMigration")
public class FileMigrationController {

    private static final String FILE_PATH = "/data/ycgjfile/ycgjupload/file";
    @Resource
    private ClaimAttachmentMapper claimAttachmentMapper;

    /**
     * 只迁移100个影像件
     */
    @GetMapping("/test/start")
    public void start100() {
        List<ClaimAttachment> claimAttachmentList = claimAttachmentMapper.selectMigrationData(0);
        List<ClaimAttachment> claimAttachments = FileMigrationUtils.fileMigration(claimAttachmentList);
        for (ClaimAttachment item : claimAttachments) {
            claimAttachmentMapper.updateById(item);
        }
    }

    /**
     * 根据指定的理赔id测试迁移影像件
     * @param id
     */
    @GetMapping("/test/{id}")
    public void test(@PathVariable("id") Long id) {
        List<ClaimAttachment> claimAttachmentList = claimAttachmentMapper.selectMigrationData(id);
        List<ClaimAttachment> claimAttachments = FileMigrationUtils.fileMigration(claimAttachmentList);
        for (ClaimAttachment item : claimAttachments) {
            claimAttachmentMapper.updateById(item);
        }
    }

    /**
     * 全量的影像迁移
     */
    @GetMapping("/start")
    public void start() {
        log.info("删除出错的文件夹");

        log.info("删除文件夹完成");
        int count = 0;
        while (true) {
            List<ClaimAttachment> claimAttachmentList = claimAttachmentMapper.selectMigrationData(0);
            if (claimAttachmentList.size() == 0) {
                log.info("所有数据更新完成");
                break;
            }
            count += 1;
            try {
                List<ClaimAttachment> claimAttachments = FileMigrationUtils.fileMigration(claimAttachmentList);
                if (CollectionUtils.isEmpty(claimAttachments)) {

                    continue;
                }
                log.info("开始{}次更新数据", count);
                for (ClaimAttachment item : claimAttachments) {
                    claimAttachmentMapper.updateById(item);
                }
            } catch (Exception e) {
                log.error("报错了报错了", e);
            }
        }
        log.info("结束更新");
    }

    /**
     * 删除 /data/ycgjfile/temp 目录下程序出错未删除的文件
     */
    @GetMapping("/delError")
    public void deleteErr(){
        File file = new File(FileMigrationUtils.basePath);
        File[] files = file.listFiles();
        int length = files.length;
        for(File item : files){
            String name = item.getName();
            if(FileUtil.del(item)){
                log.info("[{}]删除成功",name);
                length -= 1;
            }else {
                log.info("[{}]删除失败",name);
            }
        }
        log.info("还剩{}个文件",length);
    }

    /**
     * 测试删除
     */
    @GetMapping("/test/delError")
    public void testDelErr(){
        File file = new File(FileMigrationUtils.basePath);
        File[] files = file.listFiles();
        int length = files.length;
        for(File item : files){
            String name = item.getName();
            log.info("[{}]删除成功",name);
            length -= 1;
        }
        log.info("还剩{}个文件",length);
    }

    /**
     * 全量删除
     */
    @GetMapping("/delete")
    public void delete(){
        while (true){
            List<ClaimAttachment> claimAttachments = claimAttachmentMapper.selectSupplyFlag();
            if(CollectionUtils.isEmpty(claimAttachments)){
                log.info("没有需要删除的数据");
                break;
            }
            doDelete(claimAttachments);
        }
    }

    /**
     * 测试删除，删除100个
     */
    @GetMapping("/test/delete")
    public void testDel(){
        List<ClaimAttachment> claimAttachments = claimAttachmentMapper.selectSupplyFlag();
        doDelete(claimAttachments);
    }

    private void doDelete(List<ClaimAttachment> claimAttachments){

        for (ClaimAttachment item : claimAttachments){
            String pageId = item.getPageId();
            if(StringUtils.isBlank(pageId) || !pageId.contains("fileName=")){
                log.info("这数据有问题:{}", JSON.toJSONString(item));
                continue;
            }
            String[] split = pageId.split("fileName=");
            String path = FILE_PATH + "/" + split[1];
            if(FileUtil.del(path)){
                claimAttachmentMapper.updateState(item.getId(),(byte)12);
            }else {
                log.info("删除失败,id:{},path:{}",item.getId(),item.getPageId());
            }
        }
    }

    public static void main(String[] args) {
        String a = "https://hgj.yongcheng.com/ycgjsys/api//file/download?fileName=invoice/1656560662073becf99c78e37794985e8df3047b05426083e.png";
        String[] split = a.split("fileName=");
        String path = FILE_PATH + "/" + split[1];
        System.out.println(path);
    }


}

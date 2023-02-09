package com.example.file.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.sax.handler.RowHandler;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.file.job.TestJob;
import com.example.file.mapper.ClaimAttachmentMapper;
import com.example.file.mapper.ClaimPrimaryDOMapper;
import com.example.file.model.ClaimAttachment;
import com.example.file.model.ClaimPrimaryDO;
import com.example.file.model.DataReult;
import com.example.file.model.ExcelVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class ChangePathController {

    @Resource
    private ClaimAttachmentMapper claimAttachmentMapper;

    @GetMapping("/readExcel/{count}")
    public void repairByExcel(@PathVariable("count") int count){
        ExcelReader reader = ExcelUtil.getReader("/data/ycgjfile/ycgjupload/5.xlsx");
        List<ExcelVO> excelVOS = reader.readAll(ExcelVO.class);
        if(count == 1){
            ExcelVO excelVO = excelVOS.get(0);
            doDownload(Collections.singletonList(excelVO));
        }else {
            doDownload(excelVOS);
        }
    }

    public void doDownload(List<ExcelVO> list){
        for(ExcelVO item :list){
            String failPath = item.getPath();
            String newPath = item.getNewPath();

            String fileName = null;
            try {
                fileName = FilenameUtils.getName(new URL(failPath).getPath());
            }catch (Exception e){
                log.info("获取文件名错误",e);
            }

            int type = item.getType();
            String path = "/data/ycgjfile/ycgjupload/file";
            long damageId = 0L;
                    switch (type){
                case 6:
                    path = path + File.separator + "bankcard" + File.separator + fileName;
                    break;
                case 7:
                    damageId = getDamageId(item.getRegistNo());
                    if(damageId == 0){
                        log.info("出险人查询失败");
                        continue;
                    }
                    String face_img_path = getIdCardPathFail("face_img_path", damageId, fileName);
                    path = path + File.separator + face_img_path;
                    break;
                case 8:
                    damageId = getDamageId(item.getRegistNo());
                    if(damageId == 0){
                        log.info("出险人查询失败");
                        continue;
                    }
                    String back_img_path = getIdCardPathFail("back_img_path", damageId, fileName);
                    path = path + File.separator + back_img_path;
                    break;

            }
            HttpResponse response = HttpRequest.get(newPath).
                    setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.11.177.200", 8888))).execute();
            if (!response.isOk()) {
                log.info("响应状态不对：{}，oss地址:{}，跳过",response.getStatus(),newPath);
                continue;
            }
            if(response.bodyBytes().length < 1024){
                claimAttachmentMapper.updateState(item.getId(),(byte)6);
                log.info("oss下载的大小不正确,{}", newPath);
                continue;
            }

            log.info("生成的文件路径为：{}", path);
            File file = new File(path);

            long l = response.writeBody(file, null);
            if (l != 0 && file.exists()) {
                log.info("下载成功,本地路径:{}", path);
                claimAttachmentMapper.updateState(item.getId(),(byte)4);
            } else {
                if(file.exists()){
                    FileUtil.del(file);
                }
                log.info("下载失败,本地路径:{}", path);
            }

        }
    }

    public long getDamageId(String regisnNo){
       return claimAttachmentMapper.getDamageId(regisnNo);
    }


    @GetMapping("/startdDown")
    public int startdDown() {
        TestJob.down_flag = !TestJob.down_flag;
        return 0;
    }

    @GetMapping("/downLoad/{id}")
    public void downLoad(@PathVariable("id") String id) {
        List<ClaimAttachment> attList = claimAttachmentMapper.recover(id);
        for (ClaimAttachment item : attList) {
            String ossPath = item.getPath();
            String fileName = null;
            try {
                fileName = FilenameUtils.getName(new URL(ossPath).getPath());
            }catch (Exception e){
                log.info("获取文件名错误",e);
            }
            if(StringUtils.isBlank(fileName)){
                continue;
            }

            Byte type = item.getType();
            String path = "/data/ycgjfile/ycgjupload/file";
            switch (type) {
                case 6:
                    path = path + File.separator + "bankcard";
                    break;
                case 7:
                    String face = getIdCardPath("face_img_path",item.getDamageId(),fileName);
                    path = path + File.separator + face;
                    log.info("正面照：{}",path);
                    break;
                case 8:
                    String back = getIdCardPath("back_img_path",item.getDamageId(),fileName);
                    path = path + File.separator + back;
                    log.info("反面照：{}",path);
                    break;
                case 10:
                    String otherPath = getPath(item.getDamageId(),fileName);
                    path = path + File.separator + otherPath;
                    break;
            }

            log.info("附件oss:{}",ossPath);

            HttpResponse response = HttpRequest.get(ossPath).
                    setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.11.177.200", 8888))).execute();
            if (!response.isOk()) {
                log.info("响应状态不对：{}，oss地址:{}，跳过",response.getStatus(),ossPath);
                continue;
            }
            if(response.bodyBytes().length < 1024){
                claimAttachmentMapper.updateState(item.getId(),(byte)5);
                log.info("oss下载的大小不正确,{}", ossPath);
                continue;
            }



            log.info("生成的文件路径为：{}", path);
            File file = new File(path);

            long l = response.writeBody(file, null);
            if (l != 0 && file.exists()) {
                log.info("下载成功,本地路径:{}", path);

                claimAttachmentMapper.updateState(item.getId(),(byte)4);
            } else {
                if(file.exists()){
                    FileUtil.del(file);
                }
                log.info("下载失败,本地路径:{}", path);
            }
        }
    }


    public String getIdCardPathFail(String path,Long id,String fileName) {
        String idCardPath = claimAttachmentMapper.getIdCardPath(path,id,fileName);
        return idCardPath.substring(idCardPath.indexOf("=") + 1);
    }

    public String getPath(Long id,String fileName) {
        String path = claimAttachmentMapper.getPath(id,fileName);
        String tempPath = path.substring(path.indexOf("=") + 1);
        if(tempPath.contains("/")){
            return tempPath;
        }
        return fileName;
    }

    public String getIdCardPath(String path,Long id,String fileName) {
        String idCardPath = claimAttachmentMapper.getIdCardPath(path,id,fileName);
        String tempPath = idCardPath.substring(idCardPath.indexOf("=") + 1);
        if(tempPath.contains("/")){
            return tempPath;
        }
        return fileName;
    }


    @GetMapping("/change/{no}")
    public void changePath(@PathVariable("no") String no) {

        Map<String, String> map = new HashMap<>();
        List<ClaimAttachment> attList = claimAttachmentMapper.getList(no);
        for (ClaimAttachment attItem : attList) {
            String oldPath = attItem.getPath();

            map.put("path", oldPath);
            String s = HttpRequest.post("http://115.29.111.94:8080/TBTPAProxy/tpa/test").
                    setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.11.177.200", 8888))).
                    body(JSON.toJSONString(map)).execute().body();

            if (StringUtils.isBlank(s) || !s.contains("oss")) {
                continue;
            }
            DataReult dataReult = JSON.parseObject(s, DataReult.class);
            if (dataReult.getCode() != 0) {
                log.info("响应异常：{}", s);
                continue;
            }

            String[] split = oldPath.split("fileName=");
            String fileName = split[1];
            String filePath = "/data/ycgjfile/ycgjupload/file" + File.separator + fileName;

            if (!FileUtil.exist(filePath)) {
                attItem.setSupplyFlag((byte) 3);

                claimAttachmentMapper.updateById(attItem);
                log.info("文件不存在：{}", filePath);
                continue;
            }

            if (FileUtil.del(filePath)) {
                log.info("{} 删除成功", filePath);
            } else {
                log.info("{} 删除失败", filePath);
                continue;
            }
            attItem.setPath(dataReult.getPath());
            attItem.setSupplyFlag((byte) 2);

            claimAttachmentMapper.updateById(attItem);
        }
    }

    @GetMapping("/start")
    public int start() {
        TestJob.flag = !TestJob.flag;
        return 0;
    }
}

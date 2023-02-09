package com.example.file.controller;


import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.example.file.mapper.ClaimAttachmentMapper;
import com.example.file.model.ClaimAttachment;
import com.example.file.model.ExcelVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Collections;
import java.util.List;


@RestController
@Slf4j
public class SxController {

    @Resource
    private ClaimAttachmentMapper claimAttachmentMapper;


    @GetMapping("/simple/{count}")
    public void repairByExcel(@PathVariable("count") int count){
        ExcelReader reader = ExcelUtil.getReader("/data/ycgjfile/ycgjupload/6.xlsx");
        List<ExcelVO> excelVOS = reader.readAll(ExcelVO.class);
        if(count == 1){
            ExcelVO excelVO = excelVOS.get(0);
            doSimple(Collections.singletonList(excelVO));
        }else {
            doSimple(excelVOS);
        }
    }

    public void doSimple(List<ExcelVO> list){
        for(ExcelVO item :list){
            String failPath = item.getPath();
            String newPath = item.getNewPath();

            String fileName = failPath.substring(failPath.indexOf("=") + 1);


            String path = "/data/ycgjfile/ycgjupload/file" + File.separator + fileName;
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


    @GetMapping("/deal/{count}")
    public void deal1000(@PathVariable("count")int count){
        List<ClaimAttachment> sx = claimAttachmentMapper.sx();
        if(count == 1){
            List<ClaimAttachment> claimAttachments = Collections.singletonList(sx.get(0));
            doData(claimAttachments);
        }else {
            doData(sx);
        }
    }

    private void doData(List<ClaimAttachment> list){
        for(ClaimAttachment item : list){
            Byte type = item.getType();

            String path = "/data/ycgjfile/ycgjupload/file";
            String ossPath = item.getPath();

            switch (type){
                case 6:
                    path = path + File.separator + "bankcard" +getPath(ossPath);
                    break;
                case 7:
                    String face_img_path = getIdCard("face_img_path", item.getDamageId(), getAPath(ossPath));
                    path = path + File.separator + face_img_path;
                    break;
                case 8:
                    String back_img_path = getIdCard("back_img_path", item.getDamageId(), getAPath(ossPath));
                    path = path + File.separator + back_img_path;
                    break;
            }
            log.info("生成的本地路径：{}",path);
            File file = new File(path);

            log.info("从oss下载地址:{}",ossPath);
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

    private String getAPath(String ossPath){
        String path = getPath(ossPath);
        if(path.contains("/")){
            return path.substring(path.lastIndexOf("/") +1);
        }
        return  path;
    }

    private String getIdCard(String field,long damageId,String fileName){
        String idCardPath = claimAttachmentMapper.getIdCardPath(field, damageId, fileName);
        return getPath(idCardPath);
    }

    private String getPath(String ossPath){
        return ossPath.substring(ossPath.indexOf("=") + 1);
    }

}

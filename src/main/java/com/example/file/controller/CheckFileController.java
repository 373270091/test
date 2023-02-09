package com.example.file.controller;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.core.text.csv.*;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.file.mapper.ClaimAttachmentMapper;
import com.example.file.model.ClaimAttachment;
import com.example.file.model.PreviewDO;
import com.example.file.model.TempBean;
import com.example.file.utils.SunICMSXmlUtil;
import com.sunyard.insurance.encode.client.EncodeAccessParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@Slf4j
public class CheckFileController {

    @Resource
    private ClaimAttachmentMapper claimAttachmentMapper;
    static String basePath = "/data/ycgjfile/ycgjupload/file";

    @GetMapping("/preview/{no}/{pageId}")
    public void preview(@PathVariable("no")String no,@PathVariable("pageId")String pageId){
        List<String> pageIds = Collections.singletonList(pageId);

        String s = doDownload(pageIds, no);
        log.info("预览影像服务响应:{}",s);
        List<Map<String, String>> maps = SunICMSXmlUtil.analysisDownloadResponse(s, false, null);

        if(CollectionUtils.isEmpty(maps)){
            return;
        }
        for(Map<String,String> item : maps){
            log.info("pageId:{}",item.get("pageId"));
            log.info("pageUrl:{}",item.get("pageUrl"));
            log.info("fileName:{}",item.get("fileName"));
        }
    }

    @GetMapping("/preview")
    public void preview(){
        List<String> list = new ArrayList<>();
        String a ="/data/ycgjfile/a.xlsx";
        ExcelReader reader = ExcelUtil.getReader(a);
        List<PreviewDO> previewDOS = reader.readAll(PreviewDO.class);
        Map<String, List<PreviewDO>> collect = previewDOS.stream().collect(Collectors.groupingBy(PreviewDO::getNo));
        collect.forEach((k,v)->{
            List<String> collect1 = v.stream().map(PreviewDO::getPageId).distinct().collect(Collectors.toList());

            String s = doDownload(collect1, k);
            List<Map<String, String>> maps = SunICMSXmlUtil.analysisDownloadResponse(s, false, null);

            for(Map<String,String> item : maps){
                list.add(item.get("pageUrl"));
            }
        });



    }



    private String doDownload(List<String> pageIds,String no) {
        // 创建xml
        String params = "format=xml&code=ECM0010&xml=" + SunICMSXmlUtil.buildQueryImgXml(pageIds, no);
        String encodeParam = "";
        try {
            log.info("待加密的数据：{}", params);
            encodeParam = EncodeAccessParam.getEncodeParam(params, 60 * 60 * 2, "P3eio8WF2AwXMs5XuQrDckrV");
            log.info("加密后的数据：{}", encodeParam);
        } catch (Exception e) {
            log.error("加密失败");
        }

        String result = "";
        PostMethod postMethod = null;
        HttpClient httpClient = null;
        try {
            postMethod = new PostMethod("http://10.11.66.206:28001/SunICMS/servlet/RouterServlet");
            // 设置格式
            postMethod.getParams().setContentCharset("UTF-8");
            // 请求参数
            postMethod.setParameter("data", encodeParam);
            postMethod.setRequestHeader("Referer", "hgj");
            httpClient = new HttpClient();
            httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            // 执行postMethod
            int statusCode = httpClient.executeMethod(postMethod);
            log.info("调用核心接口响应code:{}", statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                byte[] bodydata = postMethod.getResponseBody();
                //取得返回值
                result = new String(bodydata, StandardCharsets.UTF_8);
            }
            log.info("影像件响应:{}", new String(postMethod.getResponseBody(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("调用核心影像件接口失败", e);
        } finally {
            if (postMethod != null) {
                try {
                    postMethod.releaseConnection();
                } catch (Exception e) {
                    log.error("释放连接失败", e);
                }
            }
            if (httpClient != null) {
                try {
                    ((SimpleHttpConnectionManager) httpClient.getHttpConnectionManager()).shutdown();
                } catch (Exception e) {
                    log.error("关闭资源失败", e);
                }
            }
        }
        return result;
    }


    @GetMapping("/delete/all")
    public void deletePdf(){

        CsvReader reader = CsvUtil.getReader();
        //从文件中读取CSV数据
        CsvData data = reader.read(FileUtil.file("/data/ycgjfile/ycgjupload/a.csv"));
        List<CsvRow> rows = data.getRows();
        for(CsvRow item : rows){
            String s = item.get(0);
            String filePath = basePath + File.separator + "pdf" + File.separator + s;
            if(!FileUtil.exist(filePath)){
                log.info("文件不存在:{}",filePath);
                continue;
            }

            if(!FileUtil.del(filePath)){
                log.info("删除失败:{}",filePath);
            }
        }
    }



    @GetMapping("/check/other")
    public void other(){
        List<ClaimAttachment> otherList = claimAttachmentMapper.selectOther();
        Map<Long, List<ClaimAttachment>> mapData = otherList.stream().collect(Collectors.groupingBy(ClaimAttachment::getClaimPrimaryId));


        List<TempBean> list = new ArrayList<>();
        mapData.forEach((k,v)->{
            for(ClaimAttachment item : v){
                String path = item.getPath();
                if(checkFile(item.getPath())){
                    continue;
                }
                List<String> registNo = claimAttachmentMapper.selectRegistNoByDamageId(k);
                if(CollectionUtils.isEmpty(registNo)){
                    continue;
                }
                TempBean tempBean = new TempBean();
                if(registNo.size() == 1){
                    tempBean.setRegistNo(registNo.get(0));
                }else {
                    tempBean.setRegistNoList(JSON.toJSONString(registNo));
                    tempBean.setRegistNo(registNo.get(0));
                }

                tempBean.setOtherPath(path);
                list.add(tempBean);
            }
        });

        ExcelWriter writer = ExcelUtil.getWriter("/data/ycgjfile/ycgjupload" + File.separator + "就诊人其他材料.xlsx");
        writer.write(list, true);
        writer.close();
    }



    @GetMapping("/check/all")
    public void checkAall() {
        List<TempBean> result = new ArrayList<>();
        long id = 0;
        while (true) {
            List<ClaimAttachment> claimAttachments = claimAttachmentMapper.selectClaimId(id);
            if (CollectionUtils.isEmpty(claimAttachments)) {
                break;
            }

            id = claimAttachments.get(claimAttachments.size() - 1).getId();
            List<Long> idList = claimAttachments.stream().map(ClaimAttachment::getClaimPrimaryId).distinct().collect(Collectors.toList());
            getData(idList,result);
        }

        ExcelWriter writer = ExcelUtil.getWriter("/data/ycgjfile/ycgjupload" + File.separator + "文件不存在.xlsx");
        writer.write(result, true);
        writer.close();

    }

    public void getData(List<Long> idList,List<TempBean> result){

        List<Map<String,Object>> damageIdList = claimAttachmentMapper.selectIdList(idList, "damage_id");
        for (Map<String,Object> item : damageIdList){
            if (item == null){
                continue;
            }
            Long damage_id = Long.valueOf(String.valueOf(item.get("damage_id")));
            String regist_no = String.valueOf(item.get("regist_no"));
            String damage_care_no = String.valueOf(item.get("damage_care_no"));
            List<Map<String, String>> idCardMaps = claimAttachmentMapper.selectIdCard(damage_id);
            for(Map<String,String> idCardMap : idCardMaps){
                String face_img_path = idCardMap.get("face_img_path");
                String back_img_path = idCardMap.get("back_img_path");
                boolean b1 = checkFile(face_img_path);
                boolean b2 = checkFile(back_img_path);
                if(b1 && b2){
                    continue;
                }
                TempBean tempBean = new TempBean();
                tempBean.setRegistNo(regist_no);
                tempBean.setIdCard(damage_care_no);
                if(!b1){
                    tempBean.setFaceImg(face_img_path);
                }
                if(!b2){
                    tempBean.setBackImg(back_img_path);
                }
                result.add(tempBean);
            }
        }

        /*List<Map<String,Object>> PayeeList = claimAttachmentMapper.selectIdList(idList, "payee_info_id");
        for (Map<String,Object> item : PayeeList){
            if (item == null){
                continue;
            }
            String payee_info_id = String.valueOf(item.get("payee_info_id"));
            String regist_no = String.valueOf(item.get("regist_no"));
            List<Map<String, String>> idCardMaps = claimAttachmentMapper.selectPayee(payee_info_id);
            for(Map<String,String> idCardMap : idCardMaps){
                String face_img_path = idCardMap.get("bank_img");
                if(checkFile(face_img_path)){
                    continue;
                }
                TempBean tempBean = new TempBean();
                tempBean.setRegistNo(regist_no);
                tempBean.setBankImg(face_img_path);
                tempBean.setIdCard(idCardMap.get("id_card"));
                tempBean.setBanCard(idCardMap.get("payee_account_id"));
                result.add(tempBean);
            }
        }*/
    }

    public static boolean checkFile(String path){
        if(StringUtils.isBlank(path) || !path.contains("=")){
            return false;
        }
        String filePath = basePath + File.separator + path.substring(path.indexOf("=") + 1);
        File file = new File(filePath);
        if(!file.exists()){
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("no","7fb72e5db277f44611987ffa6feeb2739c82");
        List<String> list = Stream.of("c360b80e7e5f427a8f592a246d427553", "b").collect(Collectors.toList());
        map.put("pageIds", list);
        map.put("flag",true);
        String s = JSON.toJSONString(map);
        System.out.println(s);
        String post = HttpUtil.post("https://hgj.yongcheng.com/ycgjsys/api/file/SunICMS/preview",s );
        System.out.println(post);
    }



}

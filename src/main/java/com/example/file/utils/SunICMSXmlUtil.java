package com.example.file.utils;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.core.util.XmlUtil;
import com.alibaba.fastjson.JSON;
import com.example.file.model.ImgServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.*;

@Slf4j
public class SunICMSXmlUtil {

    /**
     * 解析下载的xml响应，获取pageId对应的内网地址.并且根据flag，替换成外网可访问的地址
     * @param response          响应xml
     * @param imgReplaceUrl     外网地址
     * @return              map，k：pageId,v：内网url
     */
    public static List<Map<String,String>> analysisDownloadResponse(String response,boolean flag,String imgReplaceUrl){
        Document document = XmlUtil.parseXml(response);
        Element rootElement = XmlUtil.getRootElement(document);
        String responseCode = XmlUtil.getElement(rootElement, "RESPONSE_CODE").getTextContent();
        // 判断响应码
        if(StringUtils.isBlank(responseCode)){
            return null;
        }
        if(!responseCode.equals("200") && !responseCode.equals("1")){
            return null;
        }
        String responseMsg = XmlUtil.getElement(rootElement, "RESPONSE_MSG").getTextContent();
        if(responseMsg.contains("记录不存在")){
            return null;
        }

        List<Map<String,String>> list = new ArrayList<>();
        // 获取返回的url,通过page_id进行匹配
        Element page = XmlUtil.getElement(rootElement, "PAGES");
        List<Element> page1 = XmlUtil.getElements(page, "PAGE");
        for(Element element : page1){
            String pageId = element.getAttribute("PAGEID");
            String pageUrl = element.getAttribute("PAGE_URL");
            String fileName = element.getAttribute("FILE_NAME");
            if(flag){
                String authority = URLUtil.url(pageUrl).getAuthority();
                pageUrl = pageUrl.replace(authority,imgReplaceUrl);
            }
            Map<String, String> map = new HashMap<>();
            map.put("pageId",pageId);
            map.put("pageUrl",pageUrl);
            map.put("fileName",fileName);

            list.add(map);
        }
        return list;
    }

    /**
     * 创建根据pageId查询的xml
     * @param pageIdList        pageId集合
     * @return                  xml字符串
     */
    public static String buildQueryImgXml(List<String> pageIdList,String no) {
        Document document = XmlUtil.createXml();
        Element root = document.createElement("root");
        Element base_data = document.createElement("BASE_DATA");
        Element user_code = document.createElement("USER_CODE");
        user_code.setTextContent("hgj");
        Element user_name = document.createElement("USER_NAME");
        user_name.setTextContent("hgj");
        Element org_code = document.createElement("ORG_CODE");
        org_code.setTextContent("hgj");
        Element com_code1 = document.createElement("COM_CODE");
        com_code1.setTextContent("hgj");
        Element org_name1 = document.createElement("ORG_NAME");
        org_name1.setTextContent("ychgj");
        Element role_code = document.createElement("ROLE_CODE");
        role_code.setTextContent("CL401");
        base_data.appendChild(user_code);
        base_data.appendChild(user_name);
        base_data.appendChild(org_code);
        base_data.appendChild(com_code1);
        base_data.appendChild(org_name1);
        base_data.appendChild(role_code);

        Element meta_data = document.createElement("META_DATA");
        Element batch = document.createElement("BATCH");
        Element app_code = document.createElement("APP_CODE");
        app_code.setTextContent("CL_4");
        Element app_name = document.createElement("APP_NAME");
        app_name.setTextContent("claim");
        Element busi_no = document.createElement("BUSI_NO");
        busi_no.setTextContent(no);

        Element pageIds = document.createElement("PAGEIDS");
        for (String item : pageIdList) {
            Element pageid = document.createElement("PAGEID");
            pageid.setTextContent(item);
            pageIds.appendChild(pageid);
        }

        batch.appendChild(app_code);
        batch.appendChild(app_name);
        batch.appendChild(busi_no);

        meta_data.appendChild(batch);
        meta_data.appendChild(pageIds);
        root.appendChild(base_data);
        root.appendChild(meta_data);
        document.appendChild(root);
        return XmlUtil.toStr(document);
    }

    /**
     * 解析响应的xml字符串，返回pageId
     *
     * @param response 响应
     * @return pageId
     */
    public static List<ImgServerResponse> analysisUploadResponse(String response) {
        Document document = XmlUtil.parseXml(response);
        Element rootElement = XmlUtil.getRootElement(document);
        String responseCode = XmlUtil.getElement(rootElement, "RESPONSE_CODE").getTextContent();

        if (responseCode.equals("200") || responseCode.equals("1")) {
            Element page = XmlUtil.getElement(rootElement, "PAGES");
            List<Element> page1 = XmlUtil.getElements(page, "PAGE");
            List<ImgServerResponse> list = new ArrayList<ImgServerResponse>(page1.size());
            for(Element item : page1){
                ImgServerResponse serverResponse = new ImgServerResponse();
                serverResponse.setFileName(item.getAttribute("FILE_NAME"));
                serverResponse.setPageId(item.getAttribute("PAGEID"));

                list.add(serverResponse);
            }
            return list;
        }
        return null;
    }

    /**
     * 构建上传的xml
     *
     * @param no            业务编号
     * @param map           文件名称
     * @param targetXmlPath 输出xml文件路径
     * @return
     */
    public static File buildUploadXml(String no, Map<String, List<String>> map, String targetXmlPath) {
        Document document = XmlUtil.createXml();
        Element root = document.createElement("root");
        Element base_data = document.createElement("BASE_DATA");
        Element user_code = document.createElement("USER_CODE");
        user_code.setTextContent("hgj");
        Element user_name = document.createElement("USER_NAME");
        user_name.setTextContent("好管家");
        Element org_code = document.createElement("ORG_CODE");
        org_code.setTextContent("hgj");
        Element com_code1 = document.createElement("COM_CODE");
        com_code1.setTextContent("hgj");
        Element org_name1 = document.createElement("ORG_NAME");
        org_name1.setTextContent("永诚好管家");
        Element role_code = document.createElement("ROLE_CODE");
        role_code.setTextContent("CL401");
        base_data.appendChild(user_code);
        base_data.appendChild(user_name);
        base_data.appendChild(org_code);
        base_data.appendChild(com_code1);
        base_data.appendChild(org_name1);
        base_data.appendChild(role_code);

        Element meta_data = document.createElement("META_DATA");
        Element batch = document.createElement("BATCH");
        Element app_code = document.createElement("APP_CODE");
        app_code.setTextContent("CL_4");
        Element app_name = document.createElement("APP_NAME");
        app_name.setTextContent("理赔");
        Element busi_no = document.createElement("BUSI_NO");
        busi_no.setTextContent(no);
        Element pages = document.createElement("PAGES");

        String now = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");

        map.forEach((k,v)->{
            Element node = document.createElement("NODE");
            node.setAttribute("ID",k);
            node.setAttribute("ACTION","ADD");
            pages.appendChild(node);
            for(String name : v){
                Element page = document.createElement("PAGE");
                page.setAttribute("UP_TIME",now);
                page.setAttribute("UP_ORGNAME","永诚好管家");
                page.setAttribute("UP_USER_NAME","hgj");
                page.setAttribute("UP_USER","hgj");
                page.setAttribute("FILE_NAME",name);
                node.appendChild(page);
            }
        });

        Element com_code = document.createElement("COM_CODE");
        com_code.setTextContent("hgj");
        Element org_name = document.createElement("ORG_NAME");
        org_name.setTextContent("永诚好管家");
        batch.appendChild(app_code);
        batch.appendChild(app_name);
        batch.appendChild(busi_no);
        batch.appendChild(org_name);
        batch.appendChild(com_code);
        batch.appendChild(pages);
        meta_data.appendChild(batch);
        root.appendChild(base_data);
        root.appendChild(meta_data);
        document.appendChild(root);
        //写出到文件
        XmlUtil.toFile(document, targetXmlPath);
        log.info("业务编号：{}生成的XML文件内容：{}", no, JSON.toJSONString(document));
        return new File(targetXmlPath);
    }

}

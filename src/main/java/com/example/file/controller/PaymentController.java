package com.example.file.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.XmlUtil;
import com.alibaba.fastjson.JSON;
import com.example.file.enums.FileTypeEnum;
import com.example.file.job.TestJob;
import com.example.file.model.UploadClaimImgRequest;
import com.sunyard.insurance.ecm.socket.client.AutoScanApi;
import csc.integral.cp.face.webservice.InterfaceWebService;
import csc.integral.cp.face.webservice.InterfacewebServiceManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@Slf4j
public class PaymentController {

    @Value("${tempFilePath:/home/admin/data/temp}")
    private String tempFilePath;
    @Value("${yc.fileServer.ip:10.1.84.40}")
    private String fileServerIp;
    @Value("${yc.fileServer.port:8088}")
    private String fileServerPort;
    @Value("${yc.fileServer.license:SunICMS#uroahf63n59ch6s8bn5m58sg}")
    private String fileServerLicense;
    @Value("${yc.fileServer.appCode:CL_4}")
    private String fileServerAppCode;

    @PostMapping("/a")
    public String a(String data){
        return "测试成功~~~~~~~~~~~~";
    }

    @GetMapping("/test")
    public void test(@RequestParam("fileName") String fileName, HttpServletResponse response){
        String file = "C:\\Users\\37327\\Desktop\\123.jpeg";
        try {
            FileInputStream inputStream = new FileInputStream(file);
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            String diskfilename = "final.jpeg";
            response.setContentType("application/x-download");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + diskfilename + "\"");
            System.out.println("data.length " + data.length);
            response.setContentLength(data.length);
            response.setHeader("Content-Range", "" + Integer.valueOf(data.length - 1));
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Etag", "W/\"9767057-1323779115364\"");
            OutputStream os = response.getOutputStream();

            os.write(data);
            //先声明的流后关掉！
            os.flush();
            os.close();
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @GetMapping("/pay")
    public String pay(String p,String c){
        String xmlString = TestJob.convertToXml(p, c);

        InterfaceWebService webService = new InterfaceWebService();
        InterfacewebServiceManager interfacewebServiceManagerImplPort = webService.getInterfacewebServiceManagerImplPort();
        String process = interfacewebServiceManagerImplPort.process(xmlString);
        log.info("响应结果："+process);
        return process;
    }
    @GetMapping("/uploadzip")
    public String uploadzip(String path){
        String localPath = "/app/deploy";
        String zip = localPath+File.separator+ path;


        AutoScanApi autoScanApi = new AutoScanApi("10.1.84.40",8088,"SunICMS#uroahf63n59ch6s8bn5m58sg");
        String response;
        try {
            response = autoScanApi.ScanImageFile("CL_4", zip);
            log.info("上传至影像件服务器响应：{}",response);
        } catch (Exception e) {
            log.error("上传至影像件服务器异常");
            throw new RuntimeException(e);
        }
        return response;
    }

    @GetMapping("/upload")
    public String upload(String str){
        UploadClaimImgRequest request = JSON.parseObject(str, UploadClaimImgRequest.class);

        Map<String,Byte> map = request.getFileMap();
        String registNo = request.getRegistNo();
        if(map == null || map.size() == 0 || StringUtils.isBlank(registNo) || StringUtils.isBlank(registNo)){
            log.warn("影像件地址或者报案号为空，无法上传影像件");
            return "影像件地址或者报案号为空，无法上传影像件";
        }

        Map<String, List<String>> fileTypeMap = new HashMap<>();
        // 下载至本地
        List<File> files = downLoadUrl2Local(map, registNo,fileTypeMap);
        if(CollectionUtils.isEmpty(files)){
            log.warn("下载失败，报案号：{}",registNo);
            return "下载失败";
        }
        // 生成xml文件路径
        String targetXmlPath = tempFilePath + File.separator + registNo + File.separator + "busi.xml";
        // 生成xml文件
        File xmlFile = buildXMLFile(registNo, targetXmlPath,fileTypeMap);
        if(!xmlFile.exists()){
            log.error("XML文件不存在。");
            return "XML文件不存在。";
        }
        files.add(xmlFile);
        // 生成zip的路径
        String targetZipPath = tempFilePath + File.separator + registNo + File.separator + registNo + ".zip";
        // 目标zip
        File targetZip = new File(targetZipPath);
        // 把影像件，xml文件进行压缩
        toZip(files,targetZip);
        if(!targetZip.exists()){
            log.warn("没有生成对应的zip文件，报案号：{}",registNo);
            return "压缩文件失败";
        }
        if(targetZip.length() == 0){
            log.warn("生成的影像件zip大小为0，报案号：{}",registNo);
            return "生成的影像件zip大小为0";
        }
        return doUploadSocket(targetZipPath);
    }

    public static void toZip(List<File> files, File zipFile)throws RuntimeException {
        ZipOutputStream zos = null ;
        FileOutputStream out;
        try {
            out = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(out);
            for (File srcFile : files) {

                zos.putNextEntry(new ZipEntry(srcFile.getName()));
                FileInputStream in = new FileInputStream(srcFile);
                IOUtils.copy(in,zos);
                zos.closeEntry();
                in.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils",e);
        }finally{
            if(zos != null){
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<File> downLoadUrl2Local(Map<String,Byte> map,String registNo,Map<String, List<String>> fileTypeMap){
        String targetParentPath = tempFilePath + File.separator + registNo;
        File targetParentFile = new File(targetParentPath);
        if(!targetParentFile.exists()){
            targetParentFile.mkdirs();
        }
        map.forEach((k,v)->{
            try {
                URL url = new URL(k);
                String fileName = FilenameUtils.getName(url.getFile());
                String targetPath = targetParentPath + File.separator + fileName;
                FileUtils.copyURLToFile(url,new File(targetPath));

                // 文件进行分类
                String yxj = FileTypeEnum.getYxjByHgj(v);
                if(StringUtils.isBlank(yxj)){
                    return;
                }
                List<String> fileList = fileTypeMap.get(yxj);
                if(CollectionUtils.isEmpty(fileList)){
                    fileList = new ArrayList<>();
                    fileTypeMap.put(yxj,fileList);
                }
                fileList.add(fileName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return new ArrayList<>(Arrays.asList(Objects.requireNonNull(targetParentFile.listFiles())));
    }

    private File buildXMLFile(String registNo, String targetXmlPath, Map<String, List<String>> map){
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
        busi_no.setTextContent(registNo);
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
        XmlUtil.toFile(document,targetXmlPath);
        return new File(targetXmlPath);
    }

    private String doUploadSocket(String  zipPath) {
        AutoScanApi autoScanApi = new AutoScanApi(fileServerIp,Integer.parseInt(fileServerPort),fileServerLicense);
        String response = null;
        try {
            response = autoScanApi.ScanImageFile(fileServerAppCode, zipPath);
            log.info("上传至影像件服务器响应：{}",response);
        } catch (Exception e) {
            log.error("上传至影像件服务器异常");
            throw new RuntimeException(e);
        }finally {
            File file = new File(zipPath);
            File parentFile = file.getParentFile();
            FileUtil.del(parentFile);
        }
            return response;
    }

    public static void main(String[] args) {
        String s = "https://hgj.yongcheng.com/ycgjsys/api//file/download?fileName=other/16602910916061c182fcc6629b147a3f8ecba7d04565949d3.png";
        String substring = s.substring(s.indexOf("?")+1);
        System.out.println(substring.substring(substring.indexOf("/")));
    }
}

package com.example.file.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.file.enums.FileTypeEnum;
import com.example.file.mapper.CertificateInfoMapper;
import com.example.file.mapper.ClaimAttachmentMapper;
import com.example.file.mapper.ClaimPrimaryDOMapper;
import com.example.file.mapper.PayeeInfoMapper;
import com.example.file.model.*;
import com.sunyard.insurance.ecm.socket.client.AutoScanApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FileMigrationUtils implements ApplicationContextAware {

//    static final String basePath = "/app/temp";
    public static final String basePath = "/data/ycgjfile/temp";

    private static ApplicationContext applicationContext;


    private static String fileServerIp;
    private static String fileServerPort;
    private static String fileServerLicense;
    private static String fileServerAppCode;

    @Value("${yc.fileServer.ip:10.1.84.40}")
    public void initIp(String initIp) {
        fileServerIp = initIp;
    }

    @Value("${yc.fileServer.port:8088}")
    public void initPort(String initPort) {
        fileServerPort = initPort;
    }

    @Value("${yc.fileServer.license:SunICMS#uroahf63n59ch6s8bn5m58sg}")
    public void initLicense(String initLicense) {
        fileServerLicense = initLicense;
    }

    @Value("${yc.fileServer.appCode:CL_4}")
    public void initAppCode(String initAppCode) {
        fileServerAppCode = initAppCode;
    }

    public static List<ClaimAttachment> fileMigration(List<ClaimAttachment> list) {
        if(StringUtils.isBlank(fileServerIp)){
            log.info("??????????????????");
            return null;
        }
        log.info("????????????????????????:{}",list.size());
        Map<Long, List<ClaimAttachment>> map = list.stream().collect(Collectors.groupingBy(ClaimAttachment::getClaimPrimaryId));

        List<ClaimAttachment> result = new ArrayList<>();

        Set<Long> keySet = map.keySet();
        for (Long claimId : keySet) {
            List<ClaimAttachment> claimAttachmentList = map.get(claimId);

            // ????????????????????????????????????????????????????????????
            getIdCardAndBank(claimAttachmentList);

            String tempPath = basePath + File.separator + claimId;
            Map<String, List<String>> fileTypeMap = downLoadFile(claimAttachmentList, tempPath);
            if (CollectionUtil.isEmpty(fileTypeMap)) {
                log.info("????????????????????????,claim_primary_id:{}", claimId);
                continue;
            }

            String no = IdUtil.objectId();
            getXmlStr(no, tempPath, fileTypeMap);
            String zipFilePath = zipFile(tempPath, no,claimId);
            if (!FileUtil.exist(zipFilePath)) {
                log.info("zip??????????????????,{}", zipFilePath);
                continue;
            }
            String response = doUploadSocket(zipFilePath);
            if(StringUtils.isBlank(response)){
                log.info("??????????????????");
                continue;
            }
            List<ImgServerResponse> responseList = SunICMSXmlUtil.analysisUploadResponse(response);
            if (CollectionUtil.isEmpty(responseList)) {
                log.info("??????????????????,claim_primary_id:{}", claimId);
                continue;
            }
            result.addAll(overwriteData(claimAttachmentList, responseList, no));
        }
        return result;
    }

    /**
     * ????????????????????????????????????
     * ?????????????????????id???????????????claim_primary????????????????????????????????????????????????id????????????certificate_info??????payee_info??????
     * ???????????????????????????????????????????????????????????????
     * @param claimAttachmentList
     */
    public static void getIdCardAndBank(List<ClaimAttachment> claimAttachmentList){
        List<ClaimAttachment> collect = claimAttachmentList.stream().filter(item -> 6 == item.getType() || 7 == item.getType() || 8 == item.getType()).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(collect)){
            return;
        }
        Long claimPrimaryId = collect.get(0).getClaimPrimaryId();

        ClaimPrimaryDOMapper claimPrimaryDOMapper = FileMigrationUtils.getBean(ClaimPrimaryDOMapper.class);
        ClaimPrimaryDO claimPrimaryDO = claimPrimaryDOMapper.selectById(claimPrimaryId);

        // ?????????
        CertificateInfoMapper certificateInfoMapper = FileMigrationUtils.getBean(CertificateInfoMapper.class);
        QueryWrapper<CertificateInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("insu_person_family_id",claimPrimaryDO.getDamageId()).eq("delete_flag",0);
        List<CertificateInfo> certificateInfos = certificateInfoMapper.selectList(queryWrapper);
        if(CollectionUtil.isNotEmpty(certificateInfos)){
            CertificateInfo certificateInfo = certificateInfos.get(0);

            for(ClaimAttachment item : collect){
                Byte type = item.getType();
                if(7 == type){
                    item.setPath(certificateInfo.getFaceImgPath());
                }
                if(8 == type){
                    item.setPath(certificateInfo.getBackImgPath());
                }
            }
        }

        PayeeInfoMapper payeeInfoMapper = FileMigrationUtils.getBean(PayeeInfoMapper.class);
        QueryWrapper<PayeeInfo> payeeInfoQuery = new QueryWrapper<>();
        payeeInfoQuery.eq("id",claimPrimaryDO.getPayeeInfoId()).eq("delete_flag",0);
        List<PayeeInfo> payeeInfos = payeeInfoMapper.selectList(payeeInfoQuery);
        if(CollectionUtil.isNotEmpty(payeeInfos)){
            PayeeInfo payeeInfo = payeeInfos.get(0);
            for(ClaimAttachment item : collect){
                Byte type = item.getType();
                if(6 == type && payeeInfo.getBankImg().toUpperCase().startsWith("HTTP")){
                    item.setPath(payeeInfo.getBankImg());
                }
            }
        }
    }

    /**
     * ??????path???????????????????????????????????????page???pageId???no??????????????????objectId
     *
     * @param claimAttachmentList
     * @param responseList
     * @param no
     */
    public static List<ClaimAttachment> overwriteData(List<ClaimAttachment> claimAttachmentList, List<ImgServerResponse> responseList, String no) {
        Map<String, String> map = responseList.stream().collect(Collectors.toMap(ImgServerResponse::getFileName, ImgServerResponse::getPageId,(k1,k2)->k1));

        List<ClaimAttachment> updateList = new ArrayList<>();
        map.forEach((k, v) -> {
            for (ClaimAttachment item : claimAttachmentList) {
                if (item.getPath().contains(k)) {
                    item.setPageId(item.getPath());
                    item.setPath(v);
                    item.setNo(no);
                    item.setSupplyFlag((byte) 10);

                    updateList.add(item);
                }
            }
        });
        return updateList;
    }

    /**
     * ???????????????????????????????????????????????????????????????
     *
     * @param claimAttachmentList ???????????????
     * @param tempPath            ??????????????????
     * @return
     */
    public static Map<String, List<String>> downLoadFile(List<ClaimAttachment> claimAttachmentList, String tempPath) {
        Map<String, List<String>> fileTypeMap = new HashMap<>(claimAttachmentList.size());
        for (ClaimAttachment attachment : claimAttachmentList) {
            String path = attachment.getPath();
            if (!path.contains("?fileName=")) {
                log.info("url??????????????????{}", path);
                continue;
            }

            String fileName = donwnLoadFile2Local(path, tempPath);
            if (StringUtils.isBlank(fileName)) {
                log.info("?????????????????????");
                ClaimAttachmentMapper claimAttachmentMapper = FileMigrationUtils.getBean(ClaimAttachmentMapper.class);
                claimAttachmentMapper.updateState(attachment.getId(),(byte) 11);
                continue;
            }

            // ??????????????????
            String yxj = FileTypeEnum.getYxjByHgj(attachment.getType());
            List<String> fileList = fileTypeMap.get(yxj);
            if (CollectionUtils.isEmpty(fileList)) {
                fileList = new ArrayList<>();
                fileTypeMap.put(yxj, fileList);
            }
            fileList.add(fileName);
        }
        return fileTypeMap;
    }

    /*public static String doUploadSocket(String zipPath) {
        String a ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<root>" +
                "    <PAGES>" +
                "        <PAGE PAGEID=\"22596d003658410fabd9b71044b506a4\" FILE_NAME=\"165719223973472399f2f3daa13454ff8d10e7327d71de8c6.png\" NODE_ID=\"CL4503\" NODE_NAME=\"????????????\"/>" +
                "        <PAGE PAGEID=\"a7254ac7d67f4eaba38e3848c4665b24\" FILE_NAME=\"16716142121616bc711662137b94b9d5b62985ca214e93a80.png\" NODE_ID=\"CL4404\" NODE_NAME=\"???????????????????????????\"/>" +
                "        <PAGE PAGEID=\"f85bc3e5d9f8423a8e8dc1ada7631ac0\" FILE_NAME=\"1671614227119.jpg\" NODE_ID=\"CL4114\" NODE_NAME=\"????????????????????????????????????\"/>" +
                "        <PAGE PAGEID=\"57b41c06f5b449ba91f3f27675979daa\" FILE_NAME=\"166002957514074e4c293c29e3647afe8eb3146376e4dc86f.png\" NODE_ID=\"CL4112\" NODE_NAME=\"????????????????????????\"/>" +
                "        <PAGE PAGEID=\"18f5a372fc154f91998671c4751afe0d\" FILE_NAME=\"16600295848538c12a819f4ba65413298701fe4f9c1eaa1aa.png\" NODE_ID=\"CL4112\" NODE_NAME=\"????????????????????????\"/>" +
                "        <PAGE PAGEID=\"8656f0e6c34b4e09b207b1aef9fab21a\" FILE_NAME=\"1671614220108.jpg\" NODE_ID=\"CL4101\" NODE_NAME=\"???????????????\"/>" +
                "        <PAGE PAGEID=\"b427885d8f02437794051da30abf6e92\" FILE_NAME=\"signature-1671614219504.png\" NODE_ID=\"CL4409\" NODE_NAME=\"????????????????????????\"/>" +
                "    </PAGES>" +
                "    <RESPONSE_CODE>1</RESPONSE_CODE>" +
                "    <RESPONSE_MSG>????????????</RESPONSE_MSG>" +
                "</root>";
        return a;
    }*/

    public static String doUploadSocket(String zipPath) {
        log.info("????????????");
        String response = "";
        try {
            AutoScanApi autoScanApi = new AutoScanApi(fileServerIp, Integer.parseInt(fileServerPort), fileServerLicense);
            autoScanApi.setFormat("xml");
            response = autoScanApi.ScanImageFile(fileServerAppCode, zipPath);
            log.info("????????????????????????????????????{}", response);
        }catch (NoClassDefFoundError e){
            log.error("?????????jar???????????????",e);
        }catch (Exception e) {
            log.error("?????????????????????????????????", e);
            e.printStackTrace();
        } finally {
            File file = new File(zipPath);
            File parentFile = file.getParentFile();
            FileUtil.del(parentFile);
        }

        return response;
    }

    /**
     * ??????????????????xml???zip
     *
     * @param tempPath ????????????
     * @param no       ????????????
     */
    public static String zipFile(String tempPath, String no,Long claimId) {
        // ??????zip?????????
        String targetZipPath = tempPath + File.separator + no + ".zip";
        String regex = File.separator + claimId.toString();
        String replacement = File.separator + "zip" + regex;
        targetZipPath = targetZipPath.replace(regex,replacement);
        ZipUtil.zip(tempPath, targetZipPath);
        // ?????????????????????????????????
        FileUtil.del(tempPath);

        return targetZipPath;
    }

    /**
     * ??????xml??????
     *
     * @param no          ????????????
     * @param tempPath    ????????????
     * @param fileTypeMap ????????????????????????
     * @return
     */
    public static String getXmlStr(String no, String tempPath, Map<String, List<String>> fileTypeMap) {

        // ??????xml????????????
        String targetXmlPath = tempPath + File.separator + "busi.xml";
        File file = SunICMSXmlUtil.buildUploadXml(no, fileTypeMap, targetXmlPath);

        return targetXmlPath;
    }

    /**
     * ?????????????????????
     *
     * @param path     url??????
     * @param tempPath ????????????
     * @return
     */
    public static String donwnLoadFile2Local(String path, String tempPath) {
        String filePath = path.substring(path.indexOf("=") + 1);

        // ??????????????????
        String originalFileName = filePath.substring(filePath.indexOf(File.separator) + 1);
        tempPath = tempPath + File.separator + originalFileName;
        if(path.contains("https://hgj.yongcheng.com/ycgjsys/api//")){
            path = path.replace("https://hgj.yongcheng.com/ycgjsys/api//","http://localhost:8087/");
        }
        if(path.contains("https://hgj.yongcheng.com/ycgjsys/api/")){
            path = path.replace("https://hgj.yongcheng.com/ycgjsys/api/","http://localhost:8087/");
        }

        log.info("???????????????:{}",path);
        long len = HttpUtil.downloadFile(path, tempPath);

        if (len < 100) {
            log.info("????????????:{}",len);
            return StringUtils.EMPTY;
        }
        log.info("?????????????????????:{}",tempPath);
        return originalFileName;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        FileMigrationUtils.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

}


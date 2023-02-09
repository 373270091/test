package com.example.file.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.example.file.controller.ChangePathController;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfButtonFormField;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.PdfTextFormField;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.sunyard.insurance.ecm.socket.client.AutoScanApi;
import csc.integral.cp.face.webservice.InterfaceWebService;
import csc.integral.cp.face.webservice.InterfacewebServiceManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import sun.misc.BASE64Encoder;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
@Slf4j
public class TestJob {

    public static Boolean flag = false;
    public static Boolean down_flag = false;

    @Resource
    private ChangePathController changePathController;

//    @Scheduled(fixedDelay = 1000 )
    public void downLoad(){
        if(down_flag){
            changePathController.downLoad(null);
        }else {
            log.info("下载定时任务跳过，标记flag为false");
        }
    }

//    @Scheduled(fixedDelay = 1000 * 3 )
    public void changePath(){
        if(flag){
            changePathController.changePath(null);
        }else {
            log.info("标记flag为false");
        }
    }


//    @Scheduled(fixedDelay = 1000 * 60 )
    public void pdf(){
        ClassPathResource classPathResource = new ClassPathResource("template/template.pdf");
        String path = classPathResource.getPath();

        String targetPath = "/home/admin/data/pdf";
        File file = new File(targetPath);
        if(!file.exists()){
            file.mkdirs();
        }
        String jsonStr = "{\"accidentCourse\":\"噶啥的啊\",\"accidentTime\":\"2022-06-14\",\"applicant\":\"百度熊\",\"applyType\":\"0\",\"day\":\"13\",\"insurerAge\":\"32\",\"insurerIdCard\":\"211324198908314414\",\"insurerName\":\"花生奶\",\"insurerSex\":\"1\",\"month\":\"7\",\"payeeBank\":\"中国工商银行\",\"payeeBankCard\":\"6212262201023557228\",\"payeeName\":\"还好\",\"payeeType\":\"1\",\"phoneNumber\":\"18110517697\",\"relation\":1,\"signPath\":\"http://ycgj-v2.dev2.pukangbao.com/ycgjsys/api//file/download?fileName=signature/signature-1655342956502.png\",\"year\":\"2022\"}";
        Map<String, String> dataMap = JSON.parseObject(jsonStr, new TypeReference<Map<String, String>>() {});
        String target = StringUtils.join(targetPath, File.separator, System.currentTimeMillis(), ".pdf");

        if(createPdf(dataMap, path, target)){
            log.info("pdf生成完成");
            log.info("pdf图片路径："+pdf2Img(target));
        }else {
            log.info("pdf生成失败");
        }

    }

    public static Boolean createPdf(Map<String, String> dataMap, String template, String target) {
        boolean flag = false;
        log.info("模板路径 {}，本地目标路径{}",template,target);
        try {
            PdfReader pdfReader = new PdfReader(template);
            PdfWriter writer = new PdfWriter(target);

            ClassPathResource classPathResource = new ClassPathResource("template/simsun.ttc,1");
            String fontPath = classPathResource.getPath();

            PdfFont font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H);
//            PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
            PdfDocument pdfDocument = new PdfDocument(pdfReader, writer);
            pdfDocument.addFont(font);
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(pdfDocument, false);
            Map<String, PdfFormField> formFields = acroForm.getFormFields();

            formFields.forEach((k, v) -> {
                if(v instanceof PdfButtonFormField){
                    PdfButtonFormField btn = (PdfButtonFormField) v;
                    String values = dataMap.getOrDefault(k, StringUtils.EMPTY);
                    if(k.equals("signPath")){
                        btn.setValue(imageUrlToBase64(values));
                    }else {
                        btn.setValue(values);
                    }
                }
                if(v instanceof PdfTextFormField){
                    PdfTextFormField text = (PdfTextFormField)v;
                    text.setValue(dataMap.getOrDefault(k, "")).setFont(font);
                }
            });

            acroForm.flattenFields();
            pdfDocument.close();

            flag = true;
        } catch (IOException e) {
            File file = new File(target);

            if (file.exists()) {
                file.delete();
                log.error("生成PDF失败，删除本地文件{}",target);
            }
            e.printStackTrace();
        }
        return flag;
    }

//    @Scheduled(fixedDelay = 1000 * 60 )
    public void test(){
        String zipPath = "/app/deploy/R06A40679202200000014.zip";
        log.info("定时任务启动");

        AutoScanApi autoScanApi = new AutoScanApi("10.1.84.40",8088,"SunICMS#uroahf63n59ch6s8bn5m58sg");
        String response;
        try {
            response = autoScanApi.ScanImageFile("CL_4", zipPath);
            log.info("上传至影像件服务器响应：{}",response);
        } catch (Exception e) {
            log.error("上传至影像件服务器异常");
            throw new RuntimeException(e);
        }
    }

//    @Scheduled(fixedDelay = 1000 * 60 )
    public void payment(){
        String xmlString = convertToXml("1037706782017000127","80503262022000019");
        InterfaceWebService webService = new InterfaceWebService();
        InterfacewebServiceManager interfacewebServiceManagerImplPort = webService.getInterfacewebServiceManagerImplPort();
        String process = interfacewebServiceManagerImplPort.process(xmlString);
        log.info("响应结果："+process);

        /*JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        String response;
        try {
            Client client = dcf.createClient("http://10.1.5.192:8001/services/InterfaceWebService?wsdl");
            QName name = new QName("http://webservice.face.cp.integral.csc/", "InterfaceWebService");
            Object[] resultClass = client.invoke(name,xmlString);
            response = resultClass[0].toString();
            log.info("WebService响应：{}",response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/

    }


    public static String convertToXml(String policyNum,String claimNum){
        /*String now = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        TransactionData root = new TransactionData();
        PaidQueryHeader header = new PaidQueryHeader();
        header.setCreatTime(now);
        header.setPostTime(now);
        header.setUpdateTime(now);

        PaidQueryData data = new PaidQueryData();
        data.setPolicyNum(policyNum);
        data.setClaimNum(claimNum);

        root.setPaidQueryHeader(header);
        root.setPaidQueryData(data);

        StringWriter sw = new StringWriter();
        try {
            JAXBContext context = JAXBContext.newInstance(root.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(root, sw);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        log.info(sw.toString());*/
//        return sw.toString();
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<TransactionData>\n    <PaidQueryData>\n        <PolicyNum>125A41244202200000004</PolicyNum>\n        <ClaimNum>A25A41244202200000001</ClaimNum>\n        <RequestSystem>LifeClaim</RequestSystem>\n    </PaidQueryData>\n    <PaidQueryHeader>\n        <Entity>QR</Entity>\n        <TransactionType>Q_C_P_Q</TransactionType>\n        <Deploy>R</Deploy>\n        <CreatTime>2022-07-19 21:57:43</CreatTime>\n        <CreatedBy>0</CreatedBy>\n        <PostTime>2022-07-19 21:57:43</PostTime>\n        <PostedBy>0</PostedBy>\n        <UpdateBy>0</UpdateBy>\n        <UpdateTime>2022-07-19 21:57:43</UpdateTime>\n    </PaidQueryHeader>\n</TransactionData>";
        return s;
    }

    public static String imageUrlToBase64(String imgUrl) {
        URL url = null;
        InputStream is = null;
        ByteArrayOutputStream outStream = null;
        HttpURLConnection httpUrl = null;

        try {
            url = new URL(imgUrl);
            httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.connect();
            httpUrl.getInputStream();

            is = httpUrl.getInputStream();
            outStream = new ByteArrayOutputStream();

            FileCopyUtils.copy(is,outStream);
            // 对字节数组Base64编码
            return encode(outStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(is != null) {
                    is.close();
                }
                if(outStream != null) {
                    outStream.close();
                }
                if(httpUrl != null) {
                    httpUrl.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String encode(byte[] image){
        BASE64Encoder decoder = new BASE64Encoder();
        return replaceEnter(decoder.encode(image));
    }

    /**
     * 字符替换
     * @param str 字符串
     * @return 替换后的字符串
     */
    public static String replaceEnter(String str){
        String reg ="[\n-\r]";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(str);
        return m.replaceAll("");
    }

    public static String pdf2Img(String pdf){
        // 将pdf装图片 并且自定义图片得格式大小
        File file = new File(pdf);
        File img = new File(pdf.replace(".pdf", ".jpg"));
        try {
            PDDocument doc = PDDocument.load(file);
            PDFRenderer renderer = new PDFRenderer(doc);
            int pageCount = doc.getNumberOfPages();
            for (int i = 0; i < pageCount; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 144); // Windows native DPI
                // BufferedImage srcImage = resize(image, 240, 240);//产生缩略图
                ImageIO.write(image, "jpg", img);
            }
            log.info("pdf{}转换图片完成",pdf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img.getPath();
    }

}

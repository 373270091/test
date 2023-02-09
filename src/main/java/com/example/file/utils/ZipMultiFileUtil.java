package com.example.file.utils;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author xupeng
 * @description: 图片打包成zip
 * @date 2022/6/30 20:49
 */
public class ZipMultiFileUtil {

    public static void zipFiles(File[] srcFiles, File zipFile) {
        if (srcFiles.length == 0) {
            return;
        }
        // 创建 FileInputStream 对象
        FileInputStream fileInputStream = null;
        // 实例化 FileOutputStream 对象
        FileOutputStream fileOutputStream = null;
        // 实例化 ZipOutputStream 对象
        ZipOutputStream zipOutputStream = null;
        try {
            // 判断压缩后的文件存在不，不存在则创建
            if (!zipFile.exists()) {
                zipFile.createNewFile();
            } else {
                zipFile.delete();
                zipFile.createNewFile();
            }
            fileOutputStream = new FileOutputStream(zipFile);
            zipOutputStream = new ZipOutputStream(fileOutputStream);
            // 创建 ZipEntry 对象
            ZipEntry zipEntry = null;
            // 遍历源文件数组
            for (File srcFile : srcFiles) {
                fileInputStream = new FileInputStream(srcFile);
                zipEntry = new ZipEntry(srcFile.getName());
                zipOutputStream.putNextEntry(zipEntry);
                IOUtils.copy(fileInputStream, zipOutputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (zipOutputStream != null) {
                    zipOutputStream.closeEntry();
                    zipOutputStream.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 压缩成ZIP 方法2

     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
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

    /**
     * 将存放在sourceFilePath目录下的源文件，打包成fileName名称的zip文件，并存放到zipFilePath路径下
     *
     * @param sourceFilePath :待压缩的文件路径
     * @param zipFilePath    :压缩后存放路径
     * @param fileName       :压缩后文件的名称
     * @return
     */
    public static boolean fileToZip(String sourceFilePath, String zipFilePath, String fileName) {
        boolean flag = false;
        File sourceFile = new File(sourceFilePath);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;

        if (!sourceFile.exists()) {
            System.out.println("待压缩的文件目录：" + sourceFilePath + "不存在");
            sourceFile.mkdir(); // 新建目录
        }
        try {
            File zipFile = new File(zipFilePath + "/" + fileName);
            if (zipFile.exists()) {
                System.out.println(zipFilePath + "目录下存在名字为:" + fileName + ".zip" + "打包文件.");
            } else {
                File[] sourceFiles = sourceFile.listFiles();
                if (null == sourceFiles || sourceFiles.length < 1) {
                    System.out.println("待压缩的文件目录：" + sourceFilePath + "里面不存在文件，无需压缩.");
                } else {
                    fos = new FileOutputStream(zipFile);
                    zos = new ZipOutputStream(new BufferedOutputStream(fos));
                    byte[] bufs = new byte[1024 * 10];
                    for (int i = 0; i < sourceFiles.length; i++) {
                        //创建ZIP实体，并添加进压缩包
                        ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
                        zos.putNextEntry(zipEntry);
                        //读取待压缩的文件并写进压缩包里
                        fis = new FileInputStream(sourceFiles[i]);
                        bis = new BufferedInputStream(fis, 1024 * 10);
                        int read = 0;
                        while ((read = bis.read(bufs, 0, 1024 * 10)) != -1) {
                            zos.write(bufs, 0, read);
                        }
                    }
                    flag = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            //关闭流
            try {
                if (null != bis) bis.close();
                if (null != zos) zos.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return flag;
    }
}

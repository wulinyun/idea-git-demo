package com.landasoft.demo.idea.git.ideagitdemo.util;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Author wulinyun
 * @Version 1.0
 * @JdkVesion 1.7
 * @Description SHA256文件hash值計算
 * @Date 2020/4/21 16:55
 */
public class SHAFile {
    /// 计算文件的 SHA256 值
    ///
    /// 要计算 SHA256 值的文件名和路径
    /// SHA256值16进制字符串
    public static String SHA256File(String fileName) throws IOException {
        return HashFile(fileName, "SHA-256");
    }


    ///
    /// 计算文件的哈希值
    ///

    /// 要计算哈希值的文件名和路径
    /// 算法:sha1,md5
    /// 哈希值16进制字符串
    private static String HashFile(String fileName, String algName) throws IOException {
        if (!new File(fileName).exists())
        {
            return "";
        }

        FileInputStream fs = new FileInputStream(fileName);
        byte[] hashBytes = HashData(fs, algName);
        fs.close();
        return ByteArrayToHexString(hashBytes);
    }




    ///
    /// 字节数组转换为16进制表示的字符串
    ///


    private static String ByteArrayToHexString(byte[] buf)
    {
        StringBuffer sb = new StringBuffer(buf.length);
        String sTmp;

        for (int i = 0; i < buf.length; i++) {
            sTmp = Integer.toHexString(0xFF & buf[i]);
            if (sTmp.length() < 2){
                sb.append(0);
            }
            sb.append(sTmp.toUpperCase());
        }
        return sb.toString();
    }
    ///
    /// 计算哈希值
    ///

    /// 要计算哈希值的 Stream
    /// 算法:sha1,md5
    /// 哈希值字节数组
    private static byte[] HashData(InputStream stream, String algName){
        //拿到一个MD5转换器,如果想使用SHA-1或SHA-256，则传入SHA-1,SHA-256
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algName);
            //分多次将一个文件读入，对于大型文件而言，比较推荐这种方式，占用内存比较少
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = stream.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, length);
            }
            stream.close();
            byte[] md5Bytes  = md.digest();
            return md5Bytes;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void main(String[] args) throws IOException {
        String filePath = "C:\\Users\\wulinyun\\Desktop\\img\\filea.jpg";
        String sha256 = SHA256File(filePath);
        System.out.println(sha256);
    }
}

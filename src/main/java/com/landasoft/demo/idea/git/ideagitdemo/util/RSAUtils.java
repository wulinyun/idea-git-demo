package com.landasoft.demo.idea.git.ideagitdemo.util;
import org.apache.commons.codec.binary.Base64;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;


/**
 * @Author wulinyun
 * @Version 1.0
 * @JdkVesion 1.7
 * @Description SHA256withRSA 签名验签工具
 * @Date 2020/4/21 14:57
 */
public class RSAUtils {
    /**
     * 私钥签名字符串为字节数组
     * @param data 源字符串
     * @param priKey 私钥字符串
     * @return
     * @throws Exception
     */
    public static byte[] signRSA(String data, String priKey) throws Exception {
        //签名的类型
        Signature sign = Signature.getInstance("SHA256withRSA");

        //读取商户私钥,该方法传入商户私钥证书的内容即可

        byte[] keyBytes = java.util.Base64.getDecoder().decode(priKey);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        sign.initSign(privateKey);
        sign.update(data.getBytes("UTF-8"));

        return sign.sign();

    }
    public static String  signRSAToSting(String data, String priKey) throws Exception {
        //签名的类型
        Signature sign = Signature.getInstance("SHA256withRSA");
        //读取商户私钥,该方法传入商户私钥证书的内容即可
        //byte[] keyBytes = Base64.decodeBase64(priKey);
        byte[] keyBytes = java.util.Base64.getDecoder().decode(priKey);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        sign.initSign(privateKey);
        sign.update(data.getBytes("utf-8"));

        return java.util.Base64.getEncoder().encodeToString(sign.sign());

    }

    /**
     * 待签名字符串，已经签名字节数组通过公钥进行验签
     * @param data 待签名字符串
     * @param sign  已经签名字节数组
     * @param filename 证书文件路径
     * @return
     * @throws Exception
     */
    public static boolean verifyRSA(String data, byte[] sign, String filename) throws Exception{
        if(data == null || sign == null || filename == null){
            return false;
        }
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        FileInputStream in = new FileInputStream(filename);
        Certificate c = cf.generateCertificate(in);
        in.close();
        PublicKey publicKey = c.getPublicKey();
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes("UTF-8"));
        return signature.verify(sign);
    }

    /**
     * 通过证书文件获取公钥对象
     * @param filename 证书文件路径
     * @return
     * @throws Exception
     */
    public static PublicKey getPublicKey(String filename) throws Exception{
        if(filename == null){
            return null;
        }
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        FileInputStream in = new FileInputStream(filename);
        Certificate c = cf.generateCertificate(in);
        in.close();
        PublicKey publicKey = c.getPublicKey();
        return publicKey;
    }

    /**
     * 待签名字符串，已经签名字节数组通过公钥进行验签
     * @param data 待签名字符串
     * @param sign 已经签名字节数组
     * @param publicKey 公钥
     * @return
     * @throws Exception
     */
    public static boolean verifyRSA(String data, byte[] sign, PublicKey publicKey) throws Exception{
        if(data == null || sign == null || publicKey == null){
            return false;
        }
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes("UTF-8"));
        return signature.verify(sign);
    }
    /**
     * 通过私钥文件获取私钥对象
     * @param filename 私钥文件路径  (required)
     * @return 私钥对象
     */
    public static PrivateKey getPrivateKey(String filename) throws IOException {

        String content = new String(Files.readAllBytes(Paths.get(filename)), "utf-8");
        try {
            String privateKey = content.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(
                    new PKCS8EncodedKeySpec(java.util.Base64.getDecoder().decode(privateKey)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前Java环境不支持RSA", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("无效的密钥格式");
        }
    }

    /**
     * 通过私钥文件路径获取私钥字符串
     * @param filename 私钥文件路径
     * @return
     * @throws IOException IO异常
     */
    public static String getPrivateKeyString(String filename) throws IOException {

        String content = new String(Files.readAllBytes(Paths.get(filename)), "utf-8");
        String privateKey = content.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
        return privateKey;

    }
    /**
     * 将字符串中的中文进行编码
     * @param s
     * @return 返回字符串中汉字编码后的字符串
     */
    public static String cnToEncode(String s ){
        char[] ch = s.toCharArray();
        String result = "";
        for(int i=0;i<ch.length;i++){
            char temp = ch[i];
            if(isChinese(temp)){
                try {
                    String encode = URLEncoder.encode(String.valueOf(temp), "utf-8");
                    result = result + encode;
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }else{
                result = result+temp;
            }
        }
        return result;
    }

    /**
     * 判断字符是否为汉字
     * @param c
     * @return
     */
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }
}

package com.landasoft.demo.idea.git.ideagitdemo.config;

import com.landasoft.demo.idea.git.ideagitdemo.util.RSAUtils;

import java.io.IOException;
import java.security.PublicKey;

/**
 * @Author wulinyun
 * @Version 1.0
 * @JdkVesion 1.7
 * @Description 微信APIV3配置类
 * @Date 2020/4/24 10:03
 */
public class WxAPIV3Config {
    /**
     * 私钥文件路径
     */
    public static String rsaPrivateKeyFilePath = "D:\\develop\\program\\idea\\sagesoft\\idea-git-demo\\src\\main\\resources\\cert\\apiclient_key.pem";
    /**
     * 微信支付平台公钥 openssl x509 -in apiclient_cert_v3.pem -pubkey -noout > apiclient_cert_v3_pub.pem
     */
    public static String rsaPublicKeyFile ="D:\\develop\\program\\idea\\sagesoft\\idea-git-demo\\src\\main\\resources\\cert\\apiclient_cert_v3.pem";
    /**
     * 公众账号ID
     */
    public static String appid = "wxfa8b1fef701daf4e";
    /**
     * 商户号
     */
    public static String mchid ="";
    /**
     * 证书序列号
     */
    public static String serial_no ="";
    public static String wx_api3_key = "";
    /**
     * 商户私钥字符串（拷贝apiclient_key.pem文件里-----BEGIN PRIVATE KEY-----和-----END PRIVATE KEY-----之间的内容）
     */
    public static  String rsaPrivateKey;

    static {
        try {
            rsaPrivateKey = RSAUtils.getPrivateKeyString(rsaPrivateKeyFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 公钥
     */
    public static PublicKey publicKey;

    static {
        try {
            publicKey = RSAUtils.getPublicKey(rsaPublicKeyFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

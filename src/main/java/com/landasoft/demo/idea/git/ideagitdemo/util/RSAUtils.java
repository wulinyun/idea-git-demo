package com.landasoft.demo.idea.git.ideagitdemo.util;

import org.apache.commons.codec.binary.Base64;

import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * @Author wulinyun
 * @Version 1.0
 * @JdkVesion 1.7
 * @Description SHA256withRSA 签名验签工具
 * @Date 2020/4/21 14:57
 */
public class RSAUtils {
    public static byte[] signRSA(String data, String priKey) throws Exception {
        //签名的类型
        Signature sign = Signature.getInstance("SHA256withRSA");

        //读取商户私钥,该方法传入商户私钥证书的内容即可

        byte[] keyBytes = Base64.decodeBase64(priKey);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        sign.initSign(privateKey);

        sign.update(data.getBytes("UTF-8"));

        return sign.sign();

    }
    public static boolean verifyRSA(String data, byte[] sign, String pubKey) throws Exception{
        if(data == null || sign == null || pubKey == null){
            return false;
        }
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        FileInputStream in = new FileInputStream(pubKey);
        Certificate c = cf.generateCertificate(in);
        in.close();
        PublicKey publicKey = c.getPublicKey();
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes("UTF-8"));
        return signature.verify(sign);
    }
}

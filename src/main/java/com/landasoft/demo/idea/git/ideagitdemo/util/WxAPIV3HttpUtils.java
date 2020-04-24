package com.landasoft.demo.idea.git.ideagitdemo.util;

import com.landasoft.demo.idea.git.ideagitdemo.config.WxAPIV3Config;
import okhttp3.HttpUrl;
import org.apache.commons.codec.binary.Base64;

/**
 * @Author wulinyun
 * @Version 1.0
 * @JdkVesion 1.7
 * @Description 针对微信API V3的Http工具类
 * @Date 2020/4/24 10:41
 */
public class WxAPIV3HttpUtils {
    /**
     * 获取接口调用认证头Authorization
     * @param method 请求方法
     * @param url url地址
     * @param body 请求体
     * @return String Authorization
     * @throws Exception
     */
    public String getAuthorization(String method, String url, String body) throws Exception {
        String nonceStr =Long.toString(System.currentTimeMillis());;
        long timestamp = System.currentTimeMillis() / 1000;
        String message = buildMessage(method, url, timestamp, nonceStr, body);
        String signature =new String(Base64.encodeBase64(RSAUtils.signRSA(message, WxAPIV3Config.rsaPrivateKey)));;
        return "WECHATPAY2-SHA256-RSA2048 mchid=\"" + WxAPIV3Config.mchid + "\","
                + "nonce_str=\"" + nonceStr + "\","
                + "timestamp=\"" + timestamp + "\","
                + "serial_no=\"" + WxAPIV3Config.serial_no + "\","
                + "signature=\"" + signature + "\"";
    }

    /**
     * 拼接待签名串
     * @param method 请求方法
     * @param url  url地址
     * @param timestamp 时间戳
     * @param nonceStr 随机串
     * @param body 请求体
     * @return String 待签名串
     */
    public String buildMessage(String method, String url, long timestamp, String nonceStr, String body) {
        //拼签名串
        // String message = method + "\n"
        //+ canonicalUrl + "\n"
        //+ timeStamp + "\n"
        //+ nonceStr + "\n"
        //+ data + "\n";
        HttpUrl httpurl = HttpUrl.parse(url);
        String canonicalUrl = httpurl.encodedPath();
        if (httpurl.encodedQuery() != null) {
            canonicalUrl += "?" + httpurl.encodedQuery();
        }
        return method + "\n"
                + canonicalUrl + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + body + "\n";
    }
}

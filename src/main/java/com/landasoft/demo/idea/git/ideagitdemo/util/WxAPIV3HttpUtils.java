package com.landasoft.demo.idea.git.ideagitdemo.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.landasoft.demo.idea.git.ideagitdemo.config.WxAPIV3Config;
import com.landasoft.demo.idea.git.ideagitdemo.response.WxAPIv3HttpContent;
import okhttp3.HttpUrl;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.nio.charset.Charset;

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
    public static String getAuthorization(String method, String url, String body) throws Exception {
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
    public static String buildMessage(String method, String url, long timestamp, String nonceStr, String body) {
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

    /**
     * 微信响应验签
     * @param response CloseableHttpResponse响应对象
     * @return Boolean 验签结果
     */
    public static WxAPIv3HttpContent verifyResponseRSA(CloseableHttpResponse response) throws Exception {
        HttpEntity httpEntity = response.getEntity();
        String resContent =EntityUtils.toString(httpEntity);
        System.out.println("返回内容:" + resContent);
        WxAPIv3HttpContent wxAPIv3HttpContent = new WxAPIv3HttpContent(resContent);
        //获取返回的http header
        Header headers[] = response.getAllHeaders();
        int i =0;
        while (i < headers.length) {
            System.out.println(headers[i].getName() +":  " + headers[i].getValue());
            i++;
        }
        //验证微信支付返回签名
        String Wtimestamp = response.getHeaders("Wechatpay-Timestamp")[0].getValue();
        String Wnonce = response.getHeaders("Wechatpay-Nonce")[0].getValue();
        String Wsign = response.getHeaders("Wechatpay-Signature")[0].getValue();
        //拼装待签名串
        StringBuffer ss =new StringBuffer();
        ss.append(Wtimestamp).append("\n");
        ss.append(Wnonce).append("\n");
        ss.append(resContent).append("\n");
        //验证签名
        if(RSAUtils.verifyRSA(ss.toString(), Base64.decodeBase64(Wsign.getBytes()), WxAPIV3Config.publicKey)) {
            System.out.println("签名验证成功");
            wxAPIv3HttpContent.setVerify(true);
        }else {
            System.out.println("签名验证失败");
            wxAPIv3HttpContent.setVerify(false);
        }
        EntityUtils.consume(httpEntity);
        response.close();
        return wxAPIv3HttpContent;
    }
    /**
     * 调用WxAPIv3接口获取内容
     * @param method 请求方法，暂时支持GET、POST
     * @param url 接口完整地址
     * @param body 请求体
     * @return 响应值
     */
    public static WxAPIv3HttpContent getWxAPIv3HttpContent(String method, String url, String body) throws Exception {
        //拼装http头的Authorization内容
        String authorization = WxAPIV3HttpUtils.getAuthorization(method,url,body);
        System.out.println("authorization值:"+authorization);

        //接口URL
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //获取返回内容
        CloseableHttpResponse response = null;
        if(RequestMethod.GET.equals(method)){
            HttpGet httpGet =new HttpGet(url);
            //设置头部
            httpGet.addHeader("Accept","application/json");
            httpGet.addHeader("Authorization", authorization);
            if(!StringUtils.isEmpty(body)){

            }
            response = httpclient.execute(httpGet);

        }else if(RequestMethod.POST.equals(method)){
            HttpPost httpPost =new HttpPost(url);
            //设置头部
            httpPost.addHeader("Accept","application/json");
            httpPost.addHeader("Authorization", authorization);
            if(!StringUtils.isEmpty(body)){
                httpPost.setEntity(new StringEntity(body));
            }
            response = httpclient.execute(httpPost);
        }
        WxAPIv3HttpContent wxAPIv3HttpContent = verifyResponseRSA(response);
        return wxAPIv3HttpContent;
    }

    /**
     * 此方法主要是用作图片上传只用调用，暂时不考虑其它接口使用
     * @param method POST
     * @param url 接口地址
     * @param body 需要签名的数据
     * @param multipartEntityBuilder form-data对象，必须传
     * @return
     * @throws Exception
     */
    public static WxAPIv3HttpContent getWxAPIv3HttpContent(String method, String url,String body,MultipartEntityBuilder multipartEntityBuilder) throws Exception {
        if(multipartEntityBuilder == null){
            return null;
        }
        //拼装http头的Authorization内容
        String authorization = WxAPIV3HttpUtils.getAuthorization(method,url,body);
        System.out.println("authorization值:"+authorization);

        //接口URL
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost =new HttpPost(url);

        //设置头部
        httpPost.addHeader("Accept","application/json");
        httpPost.addHeader("Content-Type","multipart/form-data");
        httpPost.addHeader("Authorization", authorization);
        //创建MultipartEntityBuilder
        //MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
        //设置boundary
        //multipartEntityBuilder.setBoundary("boundary");
        //multipartEntityBuilder.setCharset(Charset.forName("UTF-8"));
        //设置meta内容
        //multipartEntityBuilder.addTextBody("meta","{\"filename\":\""+filename+"\",\"sha256\":\""+fileSha256+"\"}", ContentType.APPLICATION_JSON);
        //设置图片内容
        //multipartEntityBuilder.addBinaryBody("file", file, ContentType.create("image/"+imageType), filename);
        //放入内容
        httpPost.setEntity(multipartEntityBuilder.build());
        //获取返回内容
        CloseableHttpResponse response = httpclient.execute(httpPost);
        WxAPIv3HttpContent wxAPIv3HttpContent = verifyResponseRSA(response);
        return wxAPIv3HttpContent;
    }
}

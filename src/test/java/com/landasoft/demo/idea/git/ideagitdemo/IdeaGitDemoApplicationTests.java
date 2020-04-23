package com.landasoft.demo.idea.git.ideagitdemo;
import com.alibaba.fastjson.JSONObject;
import com.landasoft.demo.idea.git.ideagitdemo.util.FileUtils;
import com.landasoft.demo.idea.git.ideagitdemo.util.RSAUtils;
import com.landasoft.demo.idea.git.ideagitdemo.util.WxAPIV3AesUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.*;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;

@SpringBootTest
class IdeaGitDemoApplicationTests {

    @Test
    void contextLoads() {
    }

    /**
     * 测试领券通知数据解密测试
     * @throws IOException
     * @throws GeneralSecurityException
     */
    @Test
    void testDecryptToString() throws IOException, GeneralSecurityException {
        String body = "{\"id\":\"a0d3b5f3-fb41-59d4-8887-2fc6fa73a28f\",\"create_time\":\"2020-04-22T12:54:24+08:00\",\"resource_type\":\"encrypt-resource\",\"event_type\":\"COUPON.SEND\",\"summary\":\"商家券领券通知\",\"resource\":{\"original_type\":\"coupon\",\"algorithm\":\"AEAD_AES_256_GCM\",\"ciphertext\":\"NMQoeTBQhmLg1nzzqHJBsN/m40LbxQpPIfgwz1s0zQgLM5WyqPed0BNDFGQnIdR6yseIdYPfzpJD/aBoLfX4SfK5sElWnuwEaiVXBzOP0kCZohC7sK9urWyHI88Bnq1zW0Id9FJ9bKWoR4vwmjpgrTKtZvxOFMmxV1uxY3SggEkUDvR5EzsZPZbdJjFRTxj/vL2MoHnWvHh+nLcPQiFg9wsKJ+jufPJfnT9Y6ZWpvLa/MU5gaU2ItZYDpJnGnbmN1p7wKAfKXJ5rcSumwqalQgGcMP6Jlw0aQofzUw3NW3eUUmSk5Rlp7zXzJMzRz77UKE2cfaqCevM0gIeeqHkxqt949OnywJbfC+0TbuHk1fd+ffBSakZurgdh8qqmIO667L82RTXmGA0+2eHMdYYiBtslODQa1AyO0O5MV3hIxj2cwpHm2YXsVDxxGvu3KX4FAx3WUw==\",\"associated_data\":\"coupon\",\"nonce\":\"QC8M6Rj84iKL\"}}";
        JSONObject jsonObject = JSONObject.parseObject(body);
        String wx_api3_key="";
        byte[] key = wx_api3_key.getBytes("UTF-8");
        WxAPIV3AesUtils aesUtil = new WxAPIV3AesUtils(key);
        //附加数据
        String assc = jsonObject.getJSONObject("resource").getString("associated_data");
        //nonce
        String nonce = jsonObject.getJSONObject("resource").getString("nonce");
        //数据密文
        String ciphertext = jsonObject.getJSONObject("resource").getString("ciphertext");

        String decryptToString = aesUtil.decryptToString(assc.getBytes("UTF-8"),nonce.getBytes("UTF-8"),ciphertext);
        System.out.println(decryptToString);
    }

    /**
     * 微信支付商家券上传图片测试
     * @throws IOException
     */
    @Test
    void testUploadImage() throws IOException {
        try {
            //私钥文件路径
            String rsaPrivateKeyFilePath = "D:\\develop\\program\\idea\\sagesoft\\idea-git-demo\\src\\main\\resources\\cert\\apiclient_key.pem";

            //微信支付平台公钥 openssl x509 -in apiclient_cert.pem -pubkey -noout > apiclient_cert_pub.pem
            String rsaPublicKeyFile ="D:\\develop\\program\\idea\\sagesoft\\idea-git-demo\\src\\main\\resources\\cert\\apiclient_cert.pem";
            //商户号
            String mchid ="";
            //证书序列号
            String serial_no ="";
            //商户私钥（拷贝apiclient_key.pem文件里-----BEGIN PRIVATE KEY-----和-----END PRIVATE KEY-----之间的内容）
            String rsaPrivateKey =RSAUtils.getPrivateKeyString(rsaPrivateKeyFilePath);
            //时间戳
            String timestamp = Long.toString(System.currentTimeMillis()/1000);
            //随机数
            String nonce_str =Long.toString(System.currentTimeMillis());
            String url ="https://api.mch.weixin.qq.com/v3/marketing/favor/media/image-upload";
            //图片文件
            //String filePath ="C:\\Users\\wulinyun\\Desktop\\img\\filea.jpg";//文件路径
            String filePath ="C:\\Users\\wulinyun\\Desktop\\img\\bk\\3.png";//文件路径
            File file =new File(filePath);
            String filename = file.getName();//文件名
            String fileSha256 = DigestUtils.sha256Hex(new FileInputStream(file));//文件sha256
            //获取图片文件的图片类型
            String imageType = FileUtils.getFormatInFile(file);
            //当图片类型为null时有异常或者文件类型不对
            if(imageType == null){
                imageType = "jpg";
            }
            //拼签名串
            StringBuffer sb =new StringBuffer();
            sb.append("POST").append("\n");
            sb.append("/v3/marketing/favor/media/image-upload").append("\n");
            sb.append(timestamp).append("\n");
            sb.append(nonce_str).append("\n");
            sb.append("{\"filename\":\""+filename+"\",\"sha256\":\""+fileSha256+"\"}").append("\n");
            System.out.println("签名原串:"+sb.toString());

            //计算签名
            String sign =new String(Base64.encodeBase64(RSAUtils.signRSA(sb.toString(),rsaPrivateKey)));
            System.out.println("签名sign值:"+sign);

            //拼装http头的Authorization内容
            String authorization ="WECHATPAY2-SHA256-RSA2048 mchid=\""+mchid+"\",nonce_str=\""+nonce_str+"\",signature=\""+sign+"\",timestamp=\""+timestamp+"\",serial_no=\""+serial_no+"\"";
            System.out.println("authorization值:"+authorization);

            //接口URL
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost =new HttpPost(url);

            //设置头部
            httpPost.addHeader("Accept","application/json");
            httpPost.addHeader("Content-Type","multipart/form-data");
            httpPost.addHeader("Authorization", authorization);

            //创建MultipartEntityBuilder
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
            //设置boundary
            multipartEntityBuilder.setBoundary("boundary");
            multipartEntityBuilder.setCharset(Charset.forName("UTF-8"));
            //设置meta内容
            multipartEntityBuilder.addTextBody("meta","{\"filename\":\""+filename+"\",\"sha256\":\""+fileSha256+"\"}", ContentType.APPLICATION_JSON);
            //设置图片内容
            multipartEntityBuilder.addBinaryBody("file", file, ContentType.create("image/"+imageType), filename);
            //放入内容
            httpPost.setEntity(multipartEntityBuilder.build());
            //获取返回内容
            CloseableHttpResponse response = httpclient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            String rescontent =new String(FileUtils.InputStreamTOByte(httpEntity.getContent()));
            System.out.println("返回内容:" + rescontent);
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
            ss.append(rescontent).append("\n");
            //验证签名
            if(RSAUtils.verifyRSA(ss.toString(), Base64.decodeBase64(Wsign.getBytes()), rsaPublicKeyFile)) {
                System.out.println("签名验证成功");
            }else {
                System.out.println("签名验证失败");
            }
            EntityUtils.consume(httpEntity);
            response.close();
        }catch (Exception e) {
            System.out.println("发送POST请求异常！" + e);
            e.printStackTrace();
        }


    }
}

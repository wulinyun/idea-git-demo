package com.landasoft.demo.idea.git.ideagitdemo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest
class IdeaGitDemoApplicationTests {

    @Test
    void contextLoads() {
    }
    @Test
    void testUploadImage() throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        String strUrl = "https://api.mch.weixin.qq.com/v3/merchant/media/upload";
        String filePath = "C:\\Users\\wulinyun\\Desktop\\img\\filea.jpg";
        String strJson = "{ \"filename\": \"filea.jpg\", \"sha256\": \" 1bed931148f9a569c775f7c1fb1b511cee79aab5a42e8f83811e6a8303bd4c52\" }";

        StringBuilder sbBody1 = new StringBuilder();
        sbBody1.append("--boundary");
        sbBody1.append("Content-Disposition: form-data; name=\"meta\"\r\n");
        sbBody1.append("Content-Type: application/json\r\n");
        //此处必须有个空行
        sbBody1.append("\r\n");
        sbBody1.append(strJson + "\r\n");
        sbBody1.append("--boundary");
        sbBody1.append("Content-Disposition: form-data; name=\"file\"; filename=\"filea.jpg\"\r\n");
        sbBody1.append("Content-Type: image/jpg\r\n");
        //此处必须有个空行
        sbBody1.append("\r\n");
        //此处为图片二进制内容
        StringBuilder sbBody2 = new StringBuilder();
        sbBody2.append("\r\n");
        sbBody2.append("--boundary--\r\n");
        byte[] byteArray1 = sbBody1.toString().getBytes("UTF-8");
        FileInputStream fileInputStream = new FileInputStream(filePath);
        //获取文件大小字节
        int length = fileInputStream.available();
        //读取文件字节到一个数组中
        int bytesRead = 0;
        int bytesToRead = length;
        byte[] byteArray2 = new byte[bytesToRead];
        while (bytesRead < bytesToRead) {
            int result = fileInputStream.read(byteArray2, bytesRead, bytesToRead - bytesRead);
            if (result == -1)
                break;
            bytesRead += result;
        }
        fileInputStream.close();
        System.out.println((bytesRead == length));
        byte[] byteArray3 = sbBody2.toString().getBytes("UTF-8");

        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type","multipart/form-data;boundary=boundary");
        String auth = "WECHATPAY2-SHA256-RSA2048 mchid=\"1492097252\",serial_no=\"4881D87565831A1598B4A9B97B6788AFDDA769D5\",nonce_str=\"1587457182446\",timestamp=\"1587457182\",signature=\"GS/oP1OAAtGnXf3rhE95qun+BR8F+y/zBA4SfLD6tmy0htZYROVx9hGevG4/Lrh/kNfOFKEtBy4tKdUaP0EGQU5UDjJuOEyU8PCd28p2zsAilGAzdV/Ph1Ou5Y2irhX7boZuJ3A4wXBHcl8jRR+uuAcMkW/q5U0nq8qcsFLPruuOBL0KOH00N1g+ulk+UTc6QITGX5cx0AnZlC+t2J0p8+TlKlZTT4lCPs49XDdDxxVo+Xjui3eYVLmVXsu81OWzJRYNi0n7sla3hqK0x/DqVRTU5y7YE3fx20xihPoF0jMfiNhAatQakf6ZINGNkQqjjwaXsx4fpXI9M817m2ENWg==\"";
        headers.set("Authorization",auth);
        headers.set("user-agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.143 Safari/537.36");
        byte[] bt3 = new byte[byteArray1.length+byteArray2.length+byteArray3.length];
        //System.arraycopy(byteArray1, 0, bt3, 0, byteArray1.length);
        //System.arraycopy(byteArray2, 0, bt3, byteArray1.length, byteArray2.length);
        //System.arraycopy(byteArray3, 0, bt3, byteArray1.length+byteArray2.length, byteArray3.length);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<MultiValueMap<String, Object>>(paramMap,headers);
        //HttpEntity<ByteArrayResource> httpEntity = new HttpEntity<>(newByteArrayResource(bt3), headers);
        ResponseEntity<String> response = restTemplate.exchange(strUrl, HttpMethod.POST, httpEntity, String.class);
        System.out.println(response.getBody());
    }

}

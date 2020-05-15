package com.landasoft.demo.idea.git.ideagitdemo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.landasoft.demo.idea.git.ideagitdemo.config.WxAPIV3Config;
import com.landasoft.demo.idea.git.ideagitdemo.response.WxAPIv3HttpContent;
import com.landasoft.demo.idea.git.ideagitdemo.util.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.*;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        byte[] key = WxAPIV3Config.wx_api3_key.getBytes("UTF-8");
        WxAPIV3AesUtils aesUtil = new WxAPIV3AesUtils(key);
        //附加数据
        String assc = jsonObject.getJSONObject("resource").getString("associated_data");
        //nonce
        String nonce = jsonObject.getJSONObject("resource").getString("nonce");
        //数据密文
        String ciphertext = jsonObject.getJSONObject("resource").getString("ciphertext");
        /**
         * {
         * 	"event_type": "EVENT_TYPE_BUSICOUPON_SEND",
         * 	"coupon_code": "1201059453000011062474",
         * 	"stock_id": "1282070000000007",
         * 	"send_time": "2020-04-22T12:54:24+08:00",
         * 	"openid": "o01G1wO3fUiYs_bsGOgBdAnIWauk",
         * 	"unionid": "",
         * 	"send_channel": "BUSICOUPON_SEND_CHANNEL_H5",
         * 	"send_merchant": "1492097252",
         * 	"attach_info": "",
         * 	"send_req_no": "202004221240"
         * }
         */
        String decryptToString = aesUtil.decryptToString(assc.getBytes("UTF-8"),nonce.getBytes("UTF-8"),ciphertext);
        System.out.println(decryptToString);
    }

    /**
     * 测试普通下载证书接口，此接口用于下载v3证书，将内容保存作为证书验签文件apiclient_cert_v3.pem，用于后续从中获取的微信v3公钥验签微信回复验签
     *
     */
    @Test
    void testApiCertificatesRequest() throws Exception {
        String method = "GET";
        String url ="https://api.mch.weixin.qq.com/v3/certificates";
        String body = "";
        WxAPIv3HttpContent wxAPIv3HttpContent = WxAPIV3HttpUtils.getWxAPIv3HttpContent(method,url,body,null);
        String resContent =wxAPIv3HttpContent.getData();
        System.out.println("返回内容:" + resContent);

        //解密数据
        JSONObject jsonObject = JSONObject.parseObject(resContent);
        JSONArray data = jsonObject.getJSONArray("data");
        JSONObject encrypt_certificate = data.getJSONObject(0).getJSONObject("encrypt_certificate");
        byte[] key = WxAPIV3Config.wx_api3_key.getBytes("UTF-8");
        WxAPIV3AesUtils aesUtil = new WxAPIV3AesUtils(key);

        //附加数据
        String assc = encrypt_certificate.getString("associated_data");
        //nonce
        String nonce = encrypt_certificate.getString("nonce");
        //数据密文
        String ciphertext = encrypt_certificate.getString("ciphertext");

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
            String url ="https://api.mch.weixin.qq.com/v3/marketing/favor/media/image-upload";
            String method = "GET";
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
            //创建MultipartEntityBuilder
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
            //设置boundary
            multipartEntityBuilder.setBoundary("boundary");
            multipartEntityBuilder.setCharset(Charset.forName("UTF-8"));
            String body = "{\"filename\":\""+filename+"\",\"sha256\":\""+fileSha256+"\"}";
            //设置meta内容
            multipartEntityBuilder.addTextBody("meta",body, ContentType.APPLICATION_JSON);
            //设置图片内容
            multipartEntityBuilder.addBinaryBody("file", file, ContentType.create("image/"+imageType), filename);
            InputStream fileInputStream = null;
            multipartEntityBuilder.addBinaryBody("file", fileInputStream, ContentType.create("image/"+imageType), filename);
            WxAPIv3HttpContent wxAPIv3HttpContent = WxAPIV3HttpUtils.getWxAPIv3HttpMultipartEntityContent("POST",url,body,multipartEntityBuilder);
            /**
             * {
             * 	"media_url": "https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N4QfSI0hgx6W7CD59k4Jd3GIgw8vFx9VicQ/0"
             * }
             */
            String resContent =wxAPIv3HttpContent.getData();
            System.out.println("返回内容:" + resContent);
        }catch (Exception e) {
            System.out.println("发送POST请求异常！" + e);
            e.printStackTrace();
        }
    }

    /**
     * 创建商家券API
     * 详情文档地址：https://pay.weixin.qq.com/wiki/doc/apiv3/wxpay/marketing/busifavor/chapter3_1.shtml
     *
     */
    @Test
    void testCreateBusifavorStocks(){
        String method = "POST";
        String url ="https://api.mch.weixin.qq.com/v3/marketing/busifavor/stocks";
        //Map<String,Object> bodyMap = new HashMap<String,Object>();
        JSONObject bodyMap = new JSONObject();
        bodyMap.put("stock_name","皇堡");
        bodyMap.put("belong_merchant","1492097252");
        bodyMap.put("comment","测试");
        bodyMap.put("goods_name","线下核销");
        bodyMap.put("stock_type","EXCHANGE");
        //核销规则
        JSONObject coupon_use_rule_Map = new JSONObject();
        //Map<String,Object> coupon_use_rule_Map = new HashMap<String,Object>();

        JSONObject coupon_available_time_Map = new JSONObject();
        //Map<String,Object> coupon_available_time_Map = new HashMap<String,Object>();
        coupon_available_time_Map.put("available_begin_time","2020-05-14T00:00:00.000+08:00");
        coupon_available_time_Map.put("available_end_time","2020-05-31T00:00:00.000+08:00");
        coupon_available_time_Map.put("available_day_after_receive",10);
        coupon_use_rule_Map.put("coupon_available_time",coupon_available_time_Map);

        JSONObject exchange_coupon_Map = new JSONObject();
        //Map<String,Object> exchange_coupon_Map = new HashMap<String,Object>();
        exchange_coupon_Map.put("exchange_price",88);
        exchange_coupon_Map.put("transaction_minimum",888);
        coupon_use_rule_Map.put("exchange_coupon",exchange_coupon_Map);

        coupon_use_rule_Map.put("use_method","OFF_LINE");
        bodyMap.put("coupon_use_rule",coupon_use_rule_Map);

        //发送规则
        JSONObject stock_send_rule_Map = new JSONObject();
        //Map<String,Object> stock_send_rule_Map = new HashMap<String,Object>();
        stock_send_rule_Map.put("max_amount",88888);
        stock_send_rule_Map.put("max_coupons",8);
        stock_send_rule_Map.put("max_coupons_per_user",8);
        stock_send_rule_Map.put("max_amount_by_day",1000);
        stock_send_rule_Map.put("max_coupons_by_day",100);
        stock_send_rule_Map.put("natural_person_limit",false);
        stock_send_rule_Map.put("prevent_api_abuse",false);
        stock_send_rule_Map.put("transferable",false);
        stock_send_rule_Map.put("shareable",false);
        bodyMap.put("stock_send_rule",stock_send_rule_Map);

        //商户请求单号
        bodyMap.put("out_request_no","14920972522020051500003");
        //自定义入口
        JSONObject custom_entrance_Map = new JSONObject();
        //Map<String,Object> custom_entrance_Map = new HashMap<String,Object>();
        custom_entrance_Map.put("appid","wxfa8b1fef701daf4e");
        bodyMap.put("custom_entrance",custom_entrance_Map);
        //样式信息
        JSONObject display_pattern_info_Map = new JSONObject();
        //Map<String,Object> display_pattern_info_Map = new HashMap<String,Object>();
        display_pattern_info_Map.put("description","测试当中");
        display_pattern_info_Map.put("merchant_logo_url","https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N0P1wzAesC8ibPWj5YTUicEJNVaMVnRYQDsA/0");
        display_pattern_info_Map.put("merchant_name","汉堡王");
        display_pattern_info_Map.put("background_color","Color040");
        display_pattern_info_Map.put("coupon_image_url","https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N0iao9u3BBeWd3Dib7Lv3JNJj6gnc2ztpo6A/0");
        bodyMap.put("display_pattern_info",display_pattern_info_Map);
        //券code模式
        bodyMap.put("coupon_code_mode","MERCHANT_API");
        //事件通知配置
        JSONObject notify_config_Map = new JSONObject();
        //Map<String,Object> notify_config_Map = new HashMap<String,Object>();
        notify_config_Map.put("notify_appid","wxfa8b1fef701daf4e");
        bodyMap.put("notify_config",notify_config_Map);
        String body = JSON.toJSONString(bodyMap);
        //String body = "{\"belong_merchant\":\"1492097252\",\"comment\":\"测试\",\"coupon_code_mode\":\"MERCHANT_API\",\"coupon_use_rule\":{\"coupon_available_time\":{\"available_begin_time\":\"2020-05-14T00:00:00.000+08:00\",\"available_day_after_receive\":10,\"available_end_time\":\"2020-05-31T00:00:00.000+08:00\"},\"exchange_coupon\":{\"exchange_price\":88,\"transaction_minimum\":888},\"use_method\":\"OFF_LINE\"},\"custom_entrance\":{\"appid\":\"wxfa8b1fef701daf4e\"},\"display_pattern_info\":{\"background_color\":\"Color040\",\"coupon_image_url\":\"https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N0iao9u3BBeWd3Dib7Lv3JNJj6gnc2ztpo6A/0\",\"description\":\"测试当中\",\"merchant_logo_url\":\"https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N0P1wzAesC8ibPWj5YTUicEJNVaMVnRYQDsA/0\",\"merchant_name\":\"汉堡王\"},\"goods_name\":\"线下核销\",\"notify_config\":{\"notify_appid\":\"wxfa8b1fef701daf4e\"},\"out_request_no\":\"14920972522020051500002\",\"stock_name\":\"皇堡\",\"stock_send_rule\":{\"max_amount\":88888,\"max_amount_by_day\":1000,\"max_coupons\":8,\"max_coupons_by_day\":100,\"max_coupons_per_user\":8,\"natural_person_limit\":false,\"prevent_api_abuse\":false,\"shareable\":false,\"transferable\":false},\"stock_type\":\"EXCHANGE\"}";
        WxAPIv3HttpContent wxAPIv3HttpContent = null;
        try {
            wxAPIv3HttpContent = WxAPIV3HttpUtils.getWxAPIv3HttpContent(method,url, body,ContentType.APPLICATION_JSON);
            /**
             * {
             *     "create_time": "2020-05-15T10:34:48+08:00",
             *     "stock_id": "1282070000000017"
             * }
             */
            String resContent =wxAPIv3HttpContent.getData();
            System.out.println("返回内容:" + resContent);
        } catch (Exception e) {
            e.printStackTrace();
        }




    }
    /**
     * 查询商家券详情API
     * 地址为：https://pay.weixin.qq.com/wiki/doc/apiv3/wxpay/marketing/busifavor/chapter3_2.shtml
     * @throws IOException
     */
    @Test
   void testBusifavorStocks(){
        String method = "GET";
        String url ="https://api.mch.weixin.qq.com/v3/marketing/busifavor/stocks/1282070000000007";
        String body = "";
        WxAPIv3HttpContent wxAPIv3HttpContent = null;
        try {
            wxAPIv3HttpContent = WxAPIV3HttpUtils.getWxAPIv3HttpContent(method,url,body,null);
            /**
             *{
             * 	"belong_merchant": "1492097252",
             * 	"comment": "活动使用",
             * 	"coupon_code_mode": "WECHATPAY_MODE",
             * 	"coupon_use_rule": {
             * 		"coupon_available_time": {
             * 			"available_begin_time": "2020-04-19T13:29:35+08:00",
             * 			"available_day_after_receive": 30,
             * 			"available_end_time": "2020-05-20T13:29:35+08:00",
             * 			"irregulary_avaliable_time": []
             *                },
             * 		"fixed_normal_coupon": {
             * 			"discount_amount": 88,
             * 			"transaction_minimum": 100
             *        },
             * 		"use_method": "OFF_LINE"* 	},
             * 	"custom_entrance": {
             * 		"appid": "wxfa8b1fef701daf4e",
             * 		"hall_id": "L6Jdj0k-DQDczxyxNHbmPw",
             * 		"mini_programs_info": {
             * 			"entrance_words": "欢迎选购",
             * 			"guiding_words": "有更多优惠哦",
             * 			"mini_programs_appid": "wx7a01c0cb0c07d7aa",
             * 			"mini_programs_path": "/page/index"
             * 		}
             * 	},
             * 	"display_pattern_info": {
             * 		"background_color": "Color030",
             * 		"coupon_image_url": "https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N0iao9u3BBeWd3Dib7Lv3JNJj6gnc2ztpo6A/0",
             * 		"description": "测试门店可用",
             * 		"merchant_logo_url": "https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N0P1wzAesC8ibPWj5YTUicEJNVaMVnRYQDsA/0",
             * 		"merchant_name": "微信支付"
             * 	},
             * 	"goods_name": "门店使用",
             * 	"notify_config": {
             * 		"notify_appid": "wxfa8b1fef701daf4e"
             * 	},
             * 	"send_count_information": {
             * 		"total_send_amount": 264,
             * 		"total_send_num": 3
             * 	},
             * 	"stock_id": "1282070000000007",
             * 	"stock_name": "活动券6",
             * 	"stock_send_rule": {
             * 		"max_amount": 100000,
             * 		"max_coupons": 1136,
             * 		"max_coupons_per_user": 2
             * 	},
             * 	"stock_state": "RUNNING",
             * 	"stock_type": "NORMAL"
             * }
             */
            String resContent =wxAPIv3HttpContent.getData();
            System.out.println("返回内容:" + resContent);
        } catch (Exception e) {
            e.printStackTrace();
        }

   }

    /**
     * 根据过滤条件查询用户券API
     * 地址为：https://pay.weixin.qq.com/wiki/doc/apiv3/wxpay/marketing/busifavor/chapter3_4.shtml
     */
    @Test
    void testBusifavorUsersCoupons(){
        String method = "GET";
        String url ="https://api.mch.weixin.qq.com/v3/marketing/busifavor/users/o01G1wO3fUiYs_bsGOgBdAnIWauk/coupons?appid="+WxAPIV3Config.appid+"&creator_merchant="+WxAPIV3Config.mchid;
        String body = "";
        WxAPIv3HttpContent wxAPIv3HttpContent = null;
        try {
            wxAPIv3HttpContent = WxAPIV3HttpUtils.getWxAPIv3HttpContent(method,url,body,null);
            /**
             * {
             * 	"data": [{
             * 		"available_start_time": "2020-04-22T00:00:00+08:00",
             * 		"belong_merchant": "1492097252",
             * 		"comment": "活动使用",
             * 		"coupon_code": "1201059453000011035744",
             * 		"coupon_state": "SENDED",
             * 		"coupon_use_rule": {
             * 			"coupon_available_time": {
             * 				"available_begin_time": "2020-04-19T13:29:35+08:00",
             * 				"available_day_after_receive": 30,
             * 				"available_end_time": "2020-05-20T13:29:35+08:00",
             * 				"irregulary_avaliable_time": []
             *                        },
             * 			"discount_coupon": {
             * 				"discount_percent": 88,
             * 				"transaction_minimum": 100
             *            },
             * 			"use_method": "OFF_LINE"* 		},
             * 		"custom_entrance": {
             * 			"appid": "wxfa8b1fef701daf4e",
             * 			"hall_id": "L6Jdj0k-DQDczxyxNHbmPw",
             * 			"mini_programs_info": {
             * 				"entrance_words": "欢迎选购",
             * 				"guiding_words": "有更多优惠哦",
             * 				"mini_programs_appid": "wx7a01c0cb0c07d7aa",
             * 				"mini_programs_path": "/page/index"
             * 			}
             * 		},
             * 		"display_pattern_info": {
             * 			"background_color": "Color030",
             * 			"coupon_image_url": "https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N0iao9u3BBeWd3Dib7Lv3JNJj6gnc2ztpo6A/0",
             * 			"description": "测试门店可用",
             * 			"merchant_logo_url": "https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N0P1wzAesC8ibPWj5YTUicEJNVaMVnRYQDsA/0",
             * 			"merchant_name": "微信支付"
             * 		},
             * 		"expire_time": "2020-05-20T13:29:35+08:00",
             * 		"goods_name": "门店使用",
             * 		"receive_time": "2020-04-22T16:54:48+08:00",
             * 		"send_request_no": "202004221652",
             * 		"stock_id": "1282070000000008",
             * 		"stock_name": "活动券7",
             * 		"stock_type": "DISCOUNT"
             * 	}, {
             * 		"available_start_time": "2020-04-22T00:00:00+08:00",
             * 		"belong_merchant": "1492097252",
             * 		"comment": "活动使用",
             * 		"coupon_code": "1201059453000011082766",
             * 		"coupon_state": "SENDED",
             * 		"coupon_use_rule": {
             * 			"coupon_available_time": {
             * 				"available_begin_time": "2020-04-19T13:29:35+08:00",
             * 				"available_day_after_receive": 30,
             * 				"available_end_time": "2020-05-20T13:29:35+08:00",
             * 				"irregulary_avaliable_time": []
             * 			},
             * 			"exchange_coupon": {
             * 				"exchange_price": 88,
             * 				"transaction_minimum": 100
             * 			},
             * 			"use_method": "OFF_LINE"
             * 		},
             * 		"custom_entrance": {
             * 			"appid": "wxfa8b1fef701daf4e",
             * 			"hall_id": "L6Jdj0k-DQDczxyxNHbmPw",
             * 			"mini_programs_info": {
             * 				"entrance_words": "欢迎选购",
             * 				"guiding_words": "有更多优惠哦",
             * 				"mini_programs_appid": "wx7a01c0cb0c07d7aa",
             * 				"mini_programs_path": "/page/index"
             * 			}
             * 		},
             * 		"display_pattern_info": {
             * 			"background_color": "Color030",
             * 			"coupon_image_url": "https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N0iao9u3BBeWd3Dib7Lv3JNJj6gnc2ztpo6A/0",
             * 			"description": "测试门店可用",
             * 			"merchant_logo_url": "https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N0P1wzAesC8ibPWj5YTUicEJNVaMVnRYQDsA/0",
             * 			"merchant_name": "微信支付"
             * 		},
             * 		"expire_time": "2020-05-20T13:29:35+08:00",
             * 		"goods_name": "门店使用",
             * 		"receive_time": "2020-04-22T13:12:54+08:00",
             * 		"send_request_no": "202004221253",
             * 		"stock_id": "1282070000000009",
             * 		"stock_name": "活动券8",
             * 		"stock_type": "EXCHANGE"
             * 	}, {
             * 		"available_start_time": "2020-04-22T00:00:00+08:00",
             * 		"belong_merchant": "1492097252",
             * 		"comment": "活动使用",
             * 		"coupon_code": "1201059453000011112738",
             * 		"coupon_state": "SENDED",
             * 		"coupon_use_rule": {
             * 			"coupon_available_time": {
             * 				"available_begin_time": "2020-04-19T13:29:35+08:00",
             * 				"available_day_after_receive": 30,
             * 				"available_end_time": "2020-05-20T13:29:35+08:00",
             * 				"irregulary_avaliable_time": []
             * 			},
             * 			"discount_coupon": {
             * 				"discount_percent": 88,
             * 				"transaction_minimum": 100
             * 			},
             * 			"use_method": "OFF_LINE"
             * 		},
             * 		"custom_entrance": {
             * 			"appid": "wxfa8b1fef701daf4e",
             * 			"hall_id": "L6Jdj0k-DQDczxyxNHbmPw",
             * 			"mini_programs_info": {
             * 				"entrance_words": "欢迎选购",
             * 				"guiding_words": "有更多优惠哦",
             * 				"mini_programs_appid": "wx7a01c0cb0c07d7aa",
             * 				"mini_programs_path": "/page/index"
             * 			}
             * 		},
             * 		"display_pattern_info": {
             * 			"background_color": "Color030",
             * 			"coupon_image_url": "https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N0iao9u3BBeWd3Dib7Lv3JNJj6gnc2ztpo6A/0",
             * 			"description": "测试门店可用",
             * 			"merchant_logo_url": "https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N0P1wzAesC8ibPWj5YTUicEJNVaMVnRYQDsA/0",
             * 			"merchant_name": "微信支付"
             * 		},
             * 		"expire_time": "2020-05-20T13:29:35+08:00",
             * 		"goods_name": "门店使用",
             * 		"receive_time": "2020-04-22T13:11:33+08:00",
             * 		"send_request_no": "202004221252",
             * 		"stock_id": "1282070000000008",
             * 		"stock_name": "活动券7",
             * 		"stock_type": "DISCOUNT"
             * 	}, {
             * 		"available_start_time": "2020-04-22T00:00:00+08:00",
             * 		"belong_merchant": "1492097252",
             * 		"comment": "活动使用",
             * 		"coupon_code": "1201059453000011062474",
             * 		"coupon_state": "SENDED",
             * 		"coupon_use_rule": {
             * 			"coupon_available_time": {
             * 				"available_begin_time": "2020-04-19T13:29:35+08:00",
             * 				"available_day_after_receive": 30,
             * 				"available_end_time": "2020-05-20T13:29:35+08:00",
             * 				"irregulary_avaliable_time": []
             * 			},
             * 			"fixed_normal_coupon": {
             * 				"discount_amount": 88,
             * 				"transaction_minimum": 100
             * 			},
             * 			"use_method": "OFF_LINE"
             * 		},
             * 		"custom_entrance": {
             * 			"appid": "wxfa8b1fef701daf4e",
             * 			"hall_id": "L6Jdj0k-DQDczxyxNHbmPw",
             * 			"mini_programs_info": {
             * 				"entrance_words": "欢迎选购",
             * 				"guiding_words": "有更多优惠哦",
             * 				"mini_programs_appid": "wx7a01c0cb0c07d7aa",
             * 				"mini_programs_path": "/page/index"
             * 			}
             * 		},
             * 		"display_pattern_info": {
             * 			"background_color": "Color030",
             * 			"coupon_image_url": "https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N0iao9u3BBeWd3Dib7Lv3JNJj6gnc2ztpo6A/0",
             * 			"description": "测试门店可用",
             * 			"merchant_logo_url": "https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N0P1wzAesC8ibPWj5YTUicEJNVaMVnRYQDsA/0",
             * 			"merchant_name": "微信支付"
             * 		},
             * 		"expire_time": "2020-05-20T13:29:35+08:00",
             * 		"goods_name": "门店使用",
             * 		"receive_time": "2020-04-22T12:54:24+08:00",
             * 		"send_request_no": "202004221240",
             * 		"stock_id": "1282070000000007",
             * 		"stock_name": "活动券6",
             * 		"stock_type": "NORMAL"
             * 	}, {
             * 		"available_start_time": "2020-04-21T00:00:00+08:00",
             * 		"belong_merchant": "1492097252",
             * 		"comment": "活动使用",
             * 		"coupon_code": "1201059453000010378280",
             * 		"coupon_state": "SENDED",
             * 		"coupon_use_rule": {
             * 			"coupon_available_time": {
             * 				"available_begin_time": "2020-04-19T13:29:35+08:00",
             * 				"available_day_after_receive": 30,
             * 				"available_end_time": "2020-05-20T13:29:35+08:00",
             * 				"irregulary_avaliable_time": []
             * 			},
             * 			"fixed_normal_coupon": {
             * 				"discount_amount": 88,
             * 				"transaction_minimum": 100
             * 			},
             * 			"use_method": "OFF_LINE"
             * 		},
             * 		"custom_entrance": {
             * 			"appid": "wxfa8b1fef701daf4e",
             * 			"hall_id": "L6Jdj0k-DQDczxyxNHbmPw",
             * 			"mini_programs_info": {
             * 				"entrance_words": "欢迎选购",
             * 				"guiding_words": "有更多优惠哦",
             * 				"mini_programs_appid": "wx7a01c0cb0c07d7aa",
             * 				"mini_programs_path": "/page/index"
             * 			}
             * 		},
             * 		"display_pattern_info": {
             * 			"background_color": "Color030",
             * 			"coupon_image_url": "",
             * 			"description": "测试门店可用",
             * 			"merchant_logo_url": "https://wx.gtimg.com/mch/img/wxpaylogo.png",
             * 			"merchant_name": "微信支付"
             * 		},
             * 		"expire_time": "2020-05-20T13:29:35+08:00",
             * 		"goods_name": "门店使用",
             * 		"receive_time": "2020-04-21T12:49:57+08:00",
             * 		"send_request_no": "202004211249",
             * 		"stock_id": "1282070000000006",
             * 		"stock_name": "活动券5",
             * 		"stock_type": "NORMAL"
             * 	}, {
             * 		"available_start_time": "2020-04-21T00:00:00+08:00",
             * 		"belong_merchant": "1492097252",
             * 		"comment": "活动使用",
             * 		"coupon_code": "1201059453000010437160",
             * 		"coupon_state": "SENDED",
             * 		"coupon_use_rule": {
             * 			"coupon_available_time": {
             * 				"available_begin_time": "2020-04-19T13:29:35+08:00",
             * 				"available_day_after_receive": 30,
             * 				"available_end_time": "2020-05-20T13:29:35+08:00",
             * 				"irregulary_avaliable_time": []
             * 			},
             * 			"fixed_normal_coupon": {
             * 				"discount_amount": 88,
             * 				"transaction_minimum": 100
             * 			},
             * 			"use_method": "OFF_LINE"
             * 		},
             * 		"custom_entrance": {
             * 			"appid": "wxfa8b1fef701daf4e",
             * 			"hall_id": "L6Jdj0k-DQDczxyxNHbmPw"
             * 		},
             * 		"display_pattern_info": {
             * 			"background_color": "Color030",
             * 			"coupon_image_url": "",
             * 			"description": "测试门店可用",
             * 			"merchant_logo_url": "https://wx.gtimg.com/mch/img/wxpaylogo.png",
             * 			"merchant_name": "微信支付"
             * 		},
             * 		"expire_time": "2020-05-20T13:29:35+08:00",
             * 		"goods_name": "门店使用",
             * 		"receive_time": "2020-04-21T12:40:01+08:00",
             * 		"send_request_no": "202004211239",
             * 		"stock_id": "1282070000000005",
             * 		"stock_name": "活动券4",
             * 		"stock_type": "NORMAL"
             * 	}, {
             * 		"available_start_time": "2020-04-21T00:00:00+08:00",
             * 		"belong_merchant": "1492097252",
             * 		"comment": "活动使用",
             * 		"coupon_code": "1201059453000010385562",
             * 		"coupon_state": "DELETED",
             * 		"coupon_use_rule": {
             * 			"coupon_available_time": {
             * 				"available_begin_time": "2020-04-19T13:29:35+08:00",
             * 				"available_day_after_receive": 30,
             * 				"available_end_time": "2020-05-20T13:29:35+08:00",
             * 				"irregulary_avaliable_time": []
             * 			},
             * 			"fixed_normal_coupon": {
             * 				"discount_amount": 88,
             * 				"transaction_minimum": 100
             * 			},
             * 			"use_method": "OFF_LINE"
             * 		},
             * 		"custom_entrance": {
             * 			"appid": "wxfa8b1fef701daf4e"
             * 		},
             * 		"display_pattern_info": {
             * 			"background_color": "Color030",
             * 			"coupon_image_url": "",
             * 			"description": "测试门店可用",
             * 			"merchant_logo_url": "https://wx.gtimg.com/mch/img/wxpaylogo.png",
             * 			"merchant_name": "微信支付"
             * 		},
             * 		"expire_time": "2020-05-20T13:29:35+08:00",
             * 		"goods_name": "门店使用",
             * 		"receive_time": "2020-04-21T11:25:45+08:00",
             * 		"send_request_no": "202004211121",
             * 		"stock_id": "1282070000000003",
             * 		"stock_name": "活动券2",
             * 		"stock_type": "NORMAL"
             * 	}],
             * 	"limit": 20,
             * 	"offset": 0,
             * 	"total_count": 7
             * }
             */
            String resContent =wxAPIv3HttpContent.getData();
            System.out.println("返回内容:" + resContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询用户单张券详情API
     * 地址为：https://pay.weixin.qq.com/wiki/doc/apiv3/wxpay/marketing/busifavor/chapter3_5.shtml
     */
    @Test
    void testBusifavorUsersOpenidCouponsCouponCodeAppidsAppid(){
        String method = "GET";
        String url ="https://api.mch.weixin.qq.com/v3/marketing/busifavor/users/{openid}/coupons/{coupon_code}/appids/{appid}";
        url = url.replace("{openid}","o01G1wO3fUiYs_bsGOgBdAnIWauk");
        url = url.replace("{coupon_code}","1201059453000011082766");
        url = url.replace("{appid}",WxAPIV3Config.appid);
        String body = "";
        WxAPIv3HttpContent wxAPIv3HttpContent = null;
        try {
            wxAPIv3HttpContent = WxAPIV3HttpUtils.getWxAPIv3HttpContent(method,url,body,null);
            /**
             * {
             * 	"available_start_time": "2020-04-22T00:00:00+08:00",
             * 	"belong_merchant": "1492097252",
             * 	"comment": "活动使用",
             * 	"coupon_code": "1201059453000011082766",
             * 	"coupon_state": "SENDED",
             * 	"coupon_use_rule": {
             * 		"coupon_available_time": {
             * 			"available_begin_time": "2020-04-19T13:29:35+08:00",
             * 			"available_day_after_receive": 30,
             * 			"available_end_time": "2020-05-20T13:29:35+08:00",
             * 			"irregulary_avaliable_time": []
             *                },
             * 		"exchange_coupon": {
             * 			"exchange_price": 88,
             * 			"transaction_minimum": 100
             *        },
             * 		"use_method": "OFF_LINE"* 	},
             * 	"custom_entrance": {
             * 		"appid": "wxfa8b1fef701daf4e",
             * 		"hall_id": "L6Jdj0k-DQDczxyxNHbmPw",
             * 		"mini_programs_info": {
             * 			"entrance_words": "欢迎选购",
             * 			"guiding_words": "有更多优惠哦",
             * 			"mini_programs_appid": "wx7a01c0cb0c07d7aa",
             * 			"mini_programs_path": "/page/index"
             * 		}
             * 	},
             * 	"display_pattern_info": {
             * 		"background_color": "Color030",
             * 		"coupon_image_url": "https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N0iao9u3BBeWd3Dib7Lv3JNJj6gnc2ztpo6A/0",
             * 		"description": "测试门店可用",
             * 		"merchant_logo_url": "https://wxpaylogo.qpic.cn/wxpaylogo/PiajxSqBRaEI1qUibGfkJ4N0P1wzAesC8ibPWj5YTUicEJNVaMVnRYQDsA/0",
             * 		"merchant_name": "微信支付"
             * 	},
             * 	"expire_time": "2020-05-20T13:29:35+08:00",
             * 	"goods_name": "门店使用",
             * 	"receive_time": "2020-04-22T13:12:54+08:00",
             * 	"send_request_no": "202004221253",
             * 	"stock_id": "1282070000000009",
             * 	"stock_name": "活动券8",
             * 	"stock_type": "EXCHANGE"
             * }
             */
            String resContent =wxAPIv3HttpContent.getData();
            System.out.println("返回内容:" + resContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询商家券事件通知地址API
     * 地址为：https://pay.weixin.qq.com/wiki/doc/apiv3/wxpay/marketing/busifavor/chapter3_8.shtml
     */
    @Test
    void testGetBusifavorCallbacks(){
        String method = "GET";
        String url ="https://api.mch.weixin.qq.com/v3/marketing/busifavor/callbacks?mchid="+WxAPIV3Config.mchid;
        String body = "";
        WxAPIv3HttpContent wxAPIv3HttpContent = null;
        try {
            wxAPIv3HttpContent = WxAPIV3HttpUtils.getWxAPIv3HttpContent(method,url,body,null);
            /**
             * {
             * 	"mchid": "1492097252",
             * 	"notify_url": "https://newsales.sagesoft.cn/scrm-bk-jd/rs/external/api/jingDong/collBack"
             * }
             */
            String resContent =wxAPIv3HttpContent.getData();
            System.out.println("返回内容:" + resContent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 设置商家券事件通知地址API
     * 参考地址为：https://pay.weixin.qq.com/wiki/doc/apiv3/wxpay/marketing/busifavor/chapter3_7.shtml
     */
    @Test
    void testSetBusifavorCallbacks(){
        String method = "POST";
        String url ="https://api.mch.weixin.qq.com/v3/marketing/busifavor/callbacks";
        Map<String,Object> bodyMap = new HashMap<String,Object>();
        bodyMap.put("mchid",WxAPIV3Config.mchid);
        bodyMap.put("notify_url","https://newsales.sagesoft.cn/scrm-bk-jd/rs/external/api/jingDong/collBack");
        String body = JSON.toJSONString(bodyMap);
        //String body = "{\"mchid\":\"1492097252\",\"notify_url\":\"https://newsales.sagesoft.cn/scrm-bk-jd/rs/external/api/jingDong/collBack\"}";
        WxAPIv3HttpContent wxAPIv3HttpContent = null;
        try {
            wxAPIv3HttpContent = WxAPIV3HttpUtils.getWxAPIv3HttpContent(method,url,body,ContentType.APPLICATION_JSON);
            /**
             * {
             * 	"mchid": "1492097252",
             * 	"notify_url": "https://newsales.sagesoft.cn/scrm-bk-jd/rs/external/api/jingDong/collBack",
             * 	"update_time": "2020-04-24T14:45:48+08:00"
             * }
             */
            String resContent =wxAPIv3HttpContent.getData();
            System.out.println("返回内容:" + resContent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 核销用户券API
     * 详情地址为：https://pay.weixin.qq.com/wiki/doc/apiv3/wxpay/marketing/busifavor/chapter3_3.shtml
     */
    @Test
    void testBusifavorCouponsUse(){
        String method = "POST";
        String url ="https://api.mch.weixin.qq.com/v3/marketing/busifavor/coupons/use";
        Map<String,Object> bodyMap = new HashMap<String,Object>();
        //券code
        bodyMap.put("coupon_code","1201059453000011082766");
        //批次号
        bodyMap.put("stock_id","1282070000000009");
        //公众账号ID
        bodyMap.put("appid",WxAPIV3Config.appid);
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        //请求核销时间
        bodyMap.put("use_time",sdf.format(new Date()));
        //bodyMap.put("use_time","2020-04-24T15:43:10.789+08:00");
        //核销请求单据号
        bodyMap.put("use_request_no", IDUtils.getGeneratID(null));
        String body = JSON.toJSONString(bodyMap);
        WxAPIv3HttpContent wxAPIv3HttpContent = null;
        try {
            wxAPIv3HttpContent = WxAPIV3HttpUtils.getWxAPIv3HttpContent(method,url,body,ContentType.APPLICATION_JSON);
            /**
             *
             * {
             * 	"openid": "o01G1wO3fUiYs_bsGOgBdAnIWauk",
             * 	"stock_id": "1282070000000009",
             * 	"wechatpay_use_time": "2020-04-24T15:32:38+08:00"
             * }
             * {
             * 	"code": "RESOURCE_ALREADY_EXISTS",
             * 	"message": "券已被其他订单核销"
             * }
             */
            String resContent =wxAPIv3HttpContent.getData();
            System.out.println("返回内容:" + resContent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

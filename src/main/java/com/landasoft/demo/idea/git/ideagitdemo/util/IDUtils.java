package com.landasoft.demo.idea.git.ideagitdemo.util;

import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @Author wulinyun
 * @Version 1.0
 * @JdkVesion 1.7
 * @Description ID生成器
 * @Date 2020/4/24 15:51
 */
public class IDUtils {
    /**
     * 通过时间获取指定格式的字符串
     * @param pattern
     * @return
     */
    public static String getDate(String pattern){
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date nowDate = new Date();
        String dateString = sdf.format(nowDate);
        return dateString;
    }

    /**
     * 获取指定个数的数字字符串
     * @param num 个数
     * @return
     */
    public static String getRandomNumString(int num){
        //获得系统时间，作为生成随机数的种子
        long seed = System.currentTimeMillis();
        //调用种子生成随机数
        Random random = new Random(seed);
        // 装载生成的随机数
        String randomString = "";
        for(int i=0;i<num;i++){
            randomString += (int)(10*(Math.random()));
        }
        return randomString;
    }

    /**
     * 获取唯一ID 默认pattern为yyyyMMddHHmmssSSS 17位  默认num为3 3位随机数 默认返回20位
     * @param pattern 默认pattern为yyyyMMddHHmmssSSS 17位
     * @param num 默认num为3 3位随机数
     * @return 默认返回20位
     */
    public static String getGeneratID(String pattern,int num){
        if(StringUtils.isEmpty(pattern)){
            pattern = "yyyyMMddHHmmssSSS";
        }
        if(StringUtils.isEmpty(num)){
            num = 3;
        }
        String idStr = getDate(pattern) + getRandomNumString(num);
        return idStr;
    }

    public static void main(String[] args) {
        String pattern = "yyyyMMddHHmmssSSS";
        String idStr = getGeneratID(pattern,3);
    }
}

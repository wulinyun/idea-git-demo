package com.landasoft.demo.idea.git.ideagitdemo.util;

import org.springframework.util.StringUtils;

/**
 * @Author wulinyun
 * @Version 1.0
 * @JdkVesion 1.7
 * @Description 驼峰与下划线方式名称互相转换工具类
 * @Date 2020/4/24 18:09
 */
public class CamelUnderlineUtil {
    private static final char UNDERLINE ='_';

    /**
     * String属性转下划线属性
     * @param param String驼峰属性
     * @return
     */
    public static String camelToUnderline(String param) {
        if (StringUtils.isEmpty(param)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int len = param.length();
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(UNDERLINE);
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     *  下划线属性转String属性
     * @param param String下划线属性
     * @return
     */
    public static String underlineToCamel(String param){
        if (StringUtils.isEmpty(param)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int len = param.length();
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (c==UNDERLINE) {
                if(++i<len){
                    sb.append(Character.toUpperCase(param.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}

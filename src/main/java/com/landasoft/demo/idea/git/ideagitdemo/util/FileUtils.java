package com.landasoft.demo.idea.git.ideagitdemo.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author wulinyun
 * @Version 1.0
 * @JdkVesion 1.7
 * @Description 文件操作工具类
 * @Date 2020/4/22 11:55
 */
public class FileUtils {
    /**
     * 通过流获取字节数组
     * @param in 流
     * @return byte[] 字节数组
     * @throws IOException IO异常
     */
    public static byte[] InputStreamTOByte(InputStream in)throws IOException {

        int BUFFER_SIZE =4096;
        ByteArrayOutputStream outStream =new ByteArrayOutputStream();
        byte[] data =new byte[BUFFER_SIZE];
        int count = -1;

        while((count = in.read(data,0,BUFFER_SIZE)) != -1)
            outStream.write(data,0, count);

        data =null;
        byte[] outByte = outStream.toByteArray();
        outStream.close();

        return outByte;
    }
}

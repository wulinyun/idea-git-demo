package com.landasoft.demo.idea.git.ideagitdemo.response;

import java.io.Serializable;

/**
 * @Author wulinyun
 * @Version 1.0
 * @JdkVesion 1.7
 * @Description WxAPIv3接口获取内容封装:对返回的json接口，以及验签结果进行返回
 * @Date 2020/4/24 12:17
 */
public class WxAPIv3HttpContent implements Serializable {
    /**
     * 接口返回数据
     */
    private String data;
    /**
     * 应答验签结果
     */
    private Boolean verify;
    public WxAPIv3HttpContent() {
        this.data = data;
    }
    public WxAPIv3HttpContent(String data) {
        this.data = data;
        this.verify = false;
    }

    public WxAPIv3HttpContent(String data, Boolean verify) {
        this.data = data;
        this.verify = verify;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Boolean getVerify() {
        return verify;
    }

    public void setVerify(Boolean verify) {
        this.verify = verify;
    }
}

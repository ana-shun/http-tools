package cn.haoziy.test;

import cn.haoziy.http.HttpUtil;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        HttpUtil httpUtil = new HttpUtil();
        httpUtil.httpGetInputStream("http://localhost:8080/api/getHello");
        System.out.println(httpUtil.printInputStream());
    }
}

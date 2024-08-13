package cn.haoziy;

import cn.haoziy.http.HttpUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpUtil httpUtil = new HttpUtil();
        httpUtil.setProxy("http://127.0.0.1:10809");
        httpUtil.setReadTimeout(100000);
        Map<String,String> headers = new HashMap<>();
        headers.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        headers.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpUtil.setHeader(headers);
        Map<String,String> params = new HashMap<>();
        params.put("zsid","15874063");
        httpUtil.setParam(params);
        httpUtil.httpGetInputStream("http://localhost:8080/api/getHello");
        System.out.println(httpUtil.printInputStream());
        System.out.println("响应长度：" + httpUtil.contentLength);
        System.out.println("响应时间：" + httpUtil.responseTime + "ms");
    }
}
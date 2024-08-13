package cn.haoziy.http;


import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpUtilTest {

    @Test
    public void httpGetInputStream() throws IOException {
        HttpUtil httpUtil = new HttpUtil();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "zhangsan");
        requestBody.put("age", 20);
        httpUtil.setRequestBody(requestBody);
        httpUtil.post("http://localhost:8080/api");
        System.out.println("response: "+httpUtil.printInputStream());
    }
}
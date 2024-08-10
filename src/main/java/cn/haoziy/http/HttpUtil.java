package cn.haoziy.http;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.util.Map;

public class HttpUtil {
    protected InputStream inputStream;
    protected final Integer CONNECT_TIMEOUT = 5000;
    protected final Integer READ_TIMEOUT = 5000;


    /**
     * download file from url
     *
     * @param strUrl string url
     * @return input stream of file
     */
    public HttpUtil httpGetInputStream(String strUrl) {
        URL url = constructURL(strUrl);
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            this.inputStream = connection.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return this;
    }

    /**
     *
     * @param strUrl string url
     * @param connectTimeout connect timeout
     * @param readTimeout read timeout
     * @return input stream of file
     */
    public HttpUtil httpGetInputStream(String strUrl,int connectTimeout,int readTimeout) {
        URL url = constructURL(strUrl);
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectTimeout<=0?CONNECT_TIMEOUT:connectTimeout);
            connection.setReadTimeout(readTimeout<=0?READ_TIMEOUT:readTimeout);
            this.inputStream = connection.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return this;
    }

    /**
     * download file from url with headers
     *
     * @param strUrl  string url
     * @param headers headers
     * @return input stream of file
     */
    public HttpUtil httpGetInputStream(String strUrl, Map<String, String> headers) {
        URL url = constructURL(strUrl);
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            this.inputStream = connection.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return this;
    }


    /**
     * get file name from url
     *
     * @param url url
     * @return file name
     */
    private static String getFileName(URL url) {
        Path path = Path.of(url.getPath());
        return path.getFileName().toString();
    }

    /**
     * @param str string
     * @return url
     * throw IllegalArgumentException if throws: MalformedURLException – if no protocol is specified, or an unknown protocol is found, or spec is null, or the parsed URL fails to comply with the specific syntax of the associated protocol.
     * @author hushunshun
     * @date 2021/4/19 10:54
     * construct URL from string
     */
    protected static URL constructURL(String str) {
        try {
            return new URL(str);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + str, e);
        }
    }

    /**
     * @param str string
     * @return uri
     * throws NullPointerException – If str is null IllegalArgumentException – If the given string is not a valid URI
     * @author hushunshun
     * @date 2021/4/19 10:54
     * construct URI from string
     */
    protected static URI constructURI(String str) {
        try {
            return URI.create(str);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URI: " + str, e);
        }
    }

    protected static FileOutputStream constructOutputStream(String path) throws FileNotFoundException {
        return new FileOutputStream(path);
    }

    public static void main(String[] args) {
        URL url = HttpUtil.constructURL("https://admin.chinesetest.cn/userFace/2023/2023-11-26/828636/43414768/d9323ebe50bf4b678e1b27570dfd9f40.jpg");
        HttpUtil httpUtil = new HttpUtil();

        try {
            System.out.println(httpUtil.httpGetInputStream(url.toString()).inputStream.transferTo(new FileOutputStream(System.getenv("USERPROFILE")+"/"+(getFileName(url)))));
            System.out.println(System.getenv("USERPROFILE")+"\\"+(getFileName(url)).replaceAll("\\\\","/"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            System.out.println(connection.getContentType());
            System.out.println(connection.getRequestMethod());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

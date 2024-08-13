package cn.haoziy.http;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class HttpUtil {
    protected InputStream inputStream;
    protected final StringBuilder responseBuilder = new StringBuilder();
    protected Proxy defaultProxy = Proxy.NO_PROXY;
    protected int connectTimeout;
    protected int readTimeout;
    protected Charset charset;
    protected boolean followRedirects = true;
    protected final Integer CONNECT_TIMEOUT = 5000;
    protected final Integer READ_TIMEOUT = 5000;
    protected final byte[] BUFFER = new byte[(1 << 12) * 5];
    public Long responseTime;
    public Long contentLength;
    protected Map<String, String> headers = new HashMap<>();
    protected Map<String, String> params = new HashMap<>();
    protected Map<String, Object> requestBody = new HashMap<>();
    protected HttpURLConnection connection;

    /**
     * download file from url
     *
     * @param strUrl string url
     */
    public void httpGetInputStream(String strUrl) {
        URL url = constructURL(strUrl + getParams());
        try {
            //open connection
            long startTime = System.currentTimeMillis();
            connection = (HttpURLConnection) url.openConnection(defaultProxy);
            connection.setConnectTimeout(this.connectTimeout > 0 ? this.connectTimeout : CONNECT_TIMEOUT);
            connection.setReadTimeout(this.readTimeout > 0 ? this.readTimeout : READ_TIMEOUT);
            String location;
            if (followRedirects && (location = connection.getHeaderField("Location")) != null) {
                httpGetInputStream(location);
                return;
            }
            contentLength = connection.getContentLengthLong();
            this.inputStream = connection.getInputStream();
            responseTime = System.currentTimeMillis() - startTime;
        } catch (IOException e) {
            //if any exception occurs, throw runtime exception and without return value
            throw new RuntimeException(e.getMessage());
        }

    }
    public void setRequestBody(Map<String, Object> requestBody) {
        this.requestBody = requestBody;
    }

    /**
     * @param proxy proxy url
     *              eg: if auth is needed, use "http://username:password@host:port" else use "http://host:port"
     */
    public void setProxy(String proxy) {
        if (proxy == null) {
            this.defaultProxy = Proxy.NO_PROXY;
        } else {
            URL url = constructURL(proxy);
            String protocol = url.getProtocol();
            int port;
            switch (protocol) {
                case "http":
                    port = url.getPort() == -1 ? 80 : url.getPort();
                    this.defaultProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(url.getHost(), port));
                    break;
                case "https":
                    port = url.getPort() == -1 ? 443 : url.getPort();
                    this.defaultProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(url.getHost(), port));
                    break;
                case "socks":
                case "socks4":
                case "socks5":
                    this.defaultProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(url.getHost(), url.getPort()));
                    break;
                default:
                    this.defaultProxy = Proxy.NO_PROXY;
                    break;
            }
            if (proxy.contains("@")) {
                String[] auth = proxy.split("@")[0].split(":");
                String username = auth[0];
                String password = auth[1];
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        if (getRequestingHost().equalsIgnoreCase(url.getHost())) {
                            return new PasswordAuthentication(username, password.toCharArray());
                        }
                        return null;
                    }
                });
            } else {
                Authenticator.setDefault(null);
            }
        }
    }

    /**
     * @param connectTimeout int connect timeout
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout > 0 ? connectTimeout : CONNECT_TIMEOUT;
    }

    /**
     * @param readTimeout int read timeout
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout > 0 ? readTimeout : READ_TIMEOUT;
    }

    /**
     * @param charset string charset name
     */
    public void setCharset(String charset) {
        try {
            this.charset = Charset.forName(charset);
        } catch (Exception e) {
            this.charset = Charset.defaultCharset();
        }
    }

    /**
     * @param followRedirects boolean follow redirects
     */
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public void setHeader(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setParam(Map<String, String> params) {
        this.params = params;
    }

    /**
     * let the map of params to url
     *
     * @return string of params
     */
    private String getParams() {
        StringBuilder payload = new StringBuilder();
        for (Map.Entry<String, String> entry : this.params.entrySet()) {
            payload.append("?")
                    .append(URLDecoder.decode(entry.getKey(), Charset.defaultCharset()))
                    .append("=")
                    .append(URLDecoder.decode(entry.getValue(), Charset.defaultCharset()))
                    .append("&");
        }
        // remove last "&"
        if (payload.length() > 0) {
            payload.deleteCharAt(payload.length() - 1);
        }
        return payload.toString();
    }

    private byte[] getRequestBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
            sb
                    .append("\"")
                    .append(entry.getKey())
                    .append("\":\"")
                    .append(entry.getValue())
                    .append("\"")
                    .append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        System.out.println(sb);
        return sb.toString().getBytes(Charset.defaultCharset());
    }

    public void post(String strUrl) {
        URL url = constructURL(strUrl);
        try {
            //open connection
            long startTime = System.currentTimeMillis();
            connection = (HttpURLConnection) url.openConnection(defaultProxy);
            connection.setConnectTimeout(this.connectTimeout > 0 ? this.connectTimeout : CONNECT_TIMEOUT);
            connection.setReadTimeout(this.readTimeout > 0 ? this.readTimeout : READ_TIMEOUT);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = getRequestBody();
                os.write(input, 0, input.length);
                os.flush();
            }
            contentLength = connection.getContentLengthLong() == -1 ? connection.getInputStream().available() : connection.getContentLengthLong();
            this.inputStream = connection.getInputStream();
            responseTime = System.currentTimeMillis() - startTime;
            String location;
            if (followRedirects && (location = connection.getHeaderField("Location")) != null) {
                post(location);
            }
        } catch (IOException e) {
            //if any exception occurs, throw runtime exception and without return value
            throw new RuntimeException(e.getMessage());
        }
    }

        /**
         * print input stream
         *
         * @return response string
         * @throws IOException if io exception occurs
         */
        public String printInputStream () throws IOException {
            InputStream is = inputStream;
            byte[] buffer = new byte[contentLength.intValue()];
            byte[] temp = buffer.length < BUFFER.length ? buffer : BUFFER;
            int len;
            while ((len = is.read(temp)) > 0) {
                responseBuilder.append(new String(temp, 0, len, this.charset == null ? Charset.defaultCharset() : this.charset));
            }
            return responseBuilder.toString();
        }


        /**
         * get file name from url
         *
         * @param url url
         * @return file name
         */
        private String getFileName (URL url){
            Path path = Path.of(url.getPath());
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(defaultProxy);
            } catch (IOException e) {
                return url.getPath();
            }
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
        protected static URL constructURL (String str){
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
        protected static URI constructURI (String str){
            try {
                return URI.create(str);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid URI: " + str, e);
            }
        }

        protected static FileOutputStream constructOutputStream (String path) throws FileNotFoundException {
            return new FileOutputStream(path);
        }


    }

package com.ws.common.util;

import com.ws.common.entity.RespEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class HttpUtil2 {

    // 连接管理器
    private static PoolingHttpClientConnectionManager pool;

    // 请求配置
    private static RequestConfig requestConfig;

    private static int TIME_OUT = 30; //超时设置: 单位 秒

    static {
        //连接池管理
        pool = new PoolingHttpClientConnectionManager(60, TimeUnit.SECONDS);
        // 最大连接数
        pool.setMaxTotal(200);
        // 最大路由基数
        pool.setDefaultMaxPerRoute(100);

        requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(TIME_OUT * 1000)
                .setConnectTimeout(TIME_OUT * 1000)
                .setSocketTimeout(TIME_OUT * 1000)
                .build();

        /*requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000).setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000).build();*/
    }

    public static CloseableHttpClient getHttpClient() {

        CloseableHttpClient httpClient = HttpClients.custom()
                // 设置连接池管理
                .setConnectionManager(pool)
                // 设置请求配置
                .setDefaultRequestConfig(requestConfig)
                // 设置重试次数
                .setRetryHandler((exception,executionCount,context) ->{
                    // Do not retry if over max retry count,如果重试次数超过了retryTime,则不再重试请求
                    if (executionCount >= 3) {
                        return false;
                    }
                    // 服务端断掉客户端的连接异常
                    if (exception instanceof NoHttpResponseException) {
                        return true;
                    }
                    // time out 超时重试
                    if (exception instanceof InterruptedIOException) {
                        return true;
                    }
                    // Unknown host
                    if (exception instanceof UnknownHostException) {
                        return false;
                    }
                    // Connection refused
                    if (exception instanceof ConnectTimeoutException) {
                        return false;
                    }
                    // SSL handshake exception
                    if (exception instanceof SSLException) {
                        return false;
                    }
                    HttpClientContext clientContext = HttpClientContext.adapt(context);
                    HttpRequest request = clientContext.getRequest();
                    if (!(request instanceof HttpEntityEnclosingRequest)) {
                        return true;
                    }
                    return false;
                })
                .build();

        return httpClient;
    }

    private static <T> T excute(Function<CloseableHttpResponse,T> function) throws IOException {
        CloseableHttpResponse response=null;
        try {
            return function.apply(response);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public static String doGet(String url)throws Exception{
        /*RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000).setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000).build();*/

        return excute(response -> {
            try {
                HttpGet httpGet = new HttpGet(url);
                httpGet.setConfig(requestConfig);
                response= getHttpClient().execute(httpGet);
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            } catch (IOException e) {
                return null;
            }
        });
    }

    public static RespEntity doGetWithCode(String url) throws Exception{
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000).setConnectionRequestTimeout(30000)
                .setSocketTimeout(60000).build();

        return excute(response -> {
            try {
                HttpGet httpGet = new HttpGet(url);
                httpGet.setConfig(requestConfig);
                response= getHttpClient().execute(httpGet);
                RespEntity respEntity = new RespEntity();
                respEntity.setCode(response.getStatusLine().getStatusCode());
                respEntity.setData(EntityUtils.toString(response.getEntity(), "UTF-8"));
                return respEntity;
            } catch (IOException e) {
                RespEntity respEntity = new RespEntity();
                respEntity.setCode(500);
                respEntity.setData(e.getMessage());
                return respEntity;
            }
        });
    }

    public static String doGet(String url,String encode)throws Exception{
        /*RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000).setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000).build();*/

        return excute(response -> {
            try {
                HttpGet httpGet = new HttpGet(url);
                httpGet.setConfig(requestConfig);
                response= getHttpClient().execute(httpGet);
                return EntityUtils.toString(response.getEntity(), encode);
            } catch (IOException e) {
                return null;
            }
        });
    }

    public static String doPost(String url,List<NameValuePair> params)throws Exception{
        return excute(response -> {
            try {
                HttpPost post = new HttpPost(url);
                post.setEntity(new UrlEncodedFormEntity(params,"utf-8"));
                response = getHttpClient().execute(post);
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            } catch (IOException e) {
                return null;
            }
        });
    }

    public static String doPost(String url,String json)throws Exception{
        return excute(response -> {
            try {
                HttpPost post = new HttpPost(url);
                StringEntity entity = new StringEntity(json,"utf-8");//解决中文乱码问题
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                post.setEntity(entity);
                response = getHttpClient().execute(post);
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            } catch (IOException e) {
                return null;
            }
        });
    }

    public static String doPostXml(String url,String xmlBody)throws Exception{
        return excute(response -> {
            try {
                HttpPost post = new HttpPost(url);
                StringEntity entity = new StringEntity(xmlBody,"utf-8");//解决中文乱码问题
                entity.setContentEncoding("UTF-8");
                entity.setContentType("text/xml");
                post.setEntity(entity);
                response = getHttpClient().execute(post);
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            } catch (IOException e) {
                return null;
            }
        });
    }
    
    /**
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        String prestr = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }
        return prestr;
    }

    /**
     * 除去数组中的空值和签名参数
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static Map<String, String> paraFilter(Map<String, String> sArray) {
        Map<String, String> result = new HashMap<String, String>();
        if (sArray == null || sArray.size() <= 0) {
            return result;
        }
        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")
                    || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(key, value);
        }
        return result;
    }

}

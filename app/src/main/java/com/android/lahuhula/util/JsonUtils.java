package com.android.lahuhula.util;

import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by lenovo on 2017/3/19.
 */

public class JsonUtils {
    private static final String TAG = JsonUtils.class.getSimpleName();
    public static final String POST_TILE = "sdata=";
    public static final String USER_LOGIN_URI = "http://sxfarm.cn/MWBS.asmx/UserLogin";
    public static final String USER_REGISTER_URI = "http://sxfarm.cn/MWBS.asmx/UserRegister";
    public static final String SEND_ADD_CIRCLE_INFO_URI = "http://sxfarm.cn/MWBS.asmx/FriendInfoAdd";
    public static final String SEND_ADD_CIRCLE_PICTURE_URI = "http://sxfarm.cn/MWBS.asmx/UpLoadImg";
    public static final String CIRCLE_INFO_GET_URI = "http://sxfarm.cn/MWBS.asmx/FriendInfoQuery";

    public static final String RESULT_TAG = "string";

    public static final int RESULT_USER_LOGIN_SUCCESS = 0x10;
    public static final int RESULT_USER_LOGIN_PASSWORD_ERROR = 0x11;
    public static final int RESULT_USER_LOGIN_USER_NAME_NOT_EXITS = 0x12;

    public static byte[] httpGet(final String url) {
        if (url == null || url.length() == 0) {
            Log.e(TAG, "httpGet, url is null");
            return null;
        }

        HttpClient httpClient = getNewHttpClient();
        HttpGet httpGet = new HttpGet(url);

        try {
            HttpResponse resp = httpClient.execute(httpGet);
            if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Log.d(TAG, "httpGet fail, status code = " + resp.getStatusLine().getStatusCode());
                return null;
            }

            return EntityUtils.toByteArray(resp.getEntity());

        } catch (Exception e) {
            Log.e(TAG, "httpGet exception, e = " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] httpPost(String url, String entity) {
        if (url == null || url.length() == 0) {
            Log.e(TAG, "httpPost, url is null");
            return null;
        }

        HttpClient httpClient = getNewHttpClient();
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.setEntity(new StringEntity(entity));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse resp = httpClient.execute(httpPost);
            if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Log.d(TAG, "HttpPost fail, status code = " + resp.getStatusLine().getStatusCode());
                return null;
            }
            return EntityUtils.toByteArray(resp.getEntity());
        } catch (Exception e) {
            Log.e(TAG, "httpPost exception, e = " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    private static class SSLSocketFactoryEx extends SSLSocketFactory {

        SSLContext sslContext = SSLContext.getInstance("TLS");

        public SSLSocketFactoryEx(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                }
            };

            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host,	port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    public static JsonResultData loginHttpPost(String urlPath, String entity) {
        if (urlPath == null || urlPath.length() <= 0) {
            Log.e(TAG, "httpPost, url is null");
            return null;
        }

        entity = POST_TILE + entity;
        entity = Utils.StringFormatToUTF8(entity);
        try {
            URL url = new URL(urlPath);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            //设置参数
            httpConn.setDoOutput(true);   //需要输出
            httpConn.setDoInput(true);   //需要输入
            httpConn.setUseCaches(false);  //不允许缓存
            httpConn.setRequestMethod("POST");   //设置POST方式连接
            //设置请求属性
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            httpConn.setRequestProperty("Charset", "UTF-8");
            //连接,也可以不用明文connect，使用下面的httpConn.getOutputStream()会自动connect
            httpConn.connect();
            //建立输入流，向指向的URL传入参数
            DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
            dos.writeBytes(entity);
            dos.flush();
            dos.close();
            //获得响应状态
            int resultCode = httpConn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == resultCode) {
                StringBuffer sb = new StringBuffer();
                String readLine = new String();
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
                while ((readLine = responseReader.readLine()) != null) {
                    sb.append(readLine).append("\n");
                }
                responseReader.close();
                String result = sb.toString();
                Log.d(TAG, "loginHttpPost, result:" + result);

                if (result.contains("password error")) {
                    return new JsonResultData(false, "密码错误");
                } else if (result.contains("not exists")) {
                    return new JsonResultData(false, "该用户名不存在");
                } else {
                    return new JsonResultData(true, result);
                }
            }
        } catch (MalformedURLException e) {
            return new JsonResultData(false, "登录异常");
        } catch (IOException e) {
            return new JsonResultData(false, "登录异常");
        }
        return new JsonResultData(false, "登录异常");
    }

    public static JsonResultData registerInfoHttpPost(String urlPath, String entity) {
        if (urlPath == null || urlPath.length() <= 0) {
            Log.e(TAG, "registerInfoHttpPost, url is null");
            return null;
        }

        entity = Utils.StringFormatToUTF8(entity);
        try {
            URL url = new URL(urlPath);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            //设置参数
            httpConn.setDoOutput(true);   //需要输出
            httpConn.setDoInput(true);   //需要输入
            httpConn.setUseCaches(false);  //不允许缓存
            httpConn.setRequestMethod("POST");   //设置POST方式连接
            //设置请求属性
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            httpConn.setRequestProperty("Charset", "UTF-8");
            //连接,也可以不用明文connect，使用下面的httpConn.getOutputStream()会自动connect
            httpConn.connect();
            //建立输入流，向指向的URL传入参数
            DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
            dos.writeBytes(entity);
            dos.flush();
            dos.close();
            //获得响应状态
            int resultCode = httpConn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == resultCode) {
                StringBuffer sb = new StringBuffer();
                String readLine = new String();
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
                while ((readLine = responseReader.readLine()) != null) {
                    sb.append(readLine).append("\n");
                }
                responseReader.close();
                String result = sb.toString();
                Log.d(TAG, "registerInfoHttpPost, result:" + result);

                if (result.contains("password error")) {
                    return new JsonResultData(false, "密码错误");
                } else if (result.contains("not exists")) {
                    return new JsonResultData(false, "该用户名不存在");
                } else {
                    return new JsonResultData(true, null);
                }
            }
        } catch (MalformedURLException e) {
            return new JsonResultData(false, "注册异常");
        } catch (IOException e) {
            return new JsonResultData(false, "注册异常");
        }
        return new JsonResultData(false, "注册异常");
    }

    public static class JsonResultData {
        private static boolean isSeccuss = false;
        private static String resultString = null;

        public JsonResultData(boolean isSeccuss, String result) {
            this.isSeccuss = isSeccuss;
            this.resultString = result;
        }

        public static boolean isSuccess() {
            return isSeccuss;
        }

        public static String getResultString() {
            return resultString;
        }
    }


    public static JsonResultData sendCircleHttpPost(ArrayList<String> pictureEntity, String circleEntity) {
        if (pictureEntity == null) {
            JsonResultData infoResult = sendCircleInfoHttpPost(circleEntity);
            if (infoResult.isSuccess()) {
                return new JsonResultData(true, "发送朋友圈成功");
            }
            return new JsonResultData(false, "发送朋友圈失败");
        }

        int picSuccessCount = 0;
        for (String singleEntity : pictureEntity) {
            JsonResultData picResult = circlePictureHttpPost(singleEntity);
            if (picResult.isSuccess()) {
                picSuccessCount ++;
            }
        }
        if (pictureEntity.size() == picSuccessCount) {
            JsonResultData infoResult = sendCircleInfoHttpPost(circleEntity);
            if (infoResult.isSuccess()) {
                return new JsonResultData(true, "发送朋友圈成功");
            }
        }
        return new JsonResultData(false, "发送朋友圈失败");
    }

    public static JsonResultData sendCircleInfoHttpPost(String entity) {
        entity = Utils.StringFormatToUTF8(entity);

        try {
            URL url = new URL(SEND_ADD_CIRCLE_INFO_URI);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            //设置参数
            httpConn.setDoOutput(true);   //需要输出
            httpConn.setDoInput(true);   //需要输入
            httpConn.setUseCaches(false);  //不允许缓存
            httpConn.setRequestMethod("POST");   //设置POST方式连接
            //设置请求属性
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            httpConn.setRequestProperty("Charset", "UTF-8");
            //连接,也可以不用明文connect，使用下面的httpConn.getOutputStream()会自动connect
            httpConn.connect();
            //建立输入流，向指向的URL传入参数
            DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
            dos.writeBytes(entity);
            dos.flush();
            dos.close();
            //获得响应状态
            int resultCode = httpConn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == resultCode) {
                StringBuffer sb = new StringBuffer();
                String readLine = new String();
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
                while ((readLine = responseReader.readLine()) != null) {
                    sb.append(readLine).append("\n");
                }
                responseReader.close();
                String result = sb.toString();
                Log.d(TAG, "sendCircleInfoHttpPost, result:" + result);
                if (result.contains("true")) {
                    return new JsonResultData(true, null);
                }
            }
        } catch (MalformedURLException e) {
            return new JsonResultData(false, "发送异常");
        } catch (IOException e) {
            return new JsonResultData(false, "发送异常");
        }
        return new JsonResultData(false, "发送异常");
    }

    public static JsonResultData circlePictureHttpPost(String entity) {
        try {
            URL url = new URL(SEND_ADD_CIRCLE_PICTURE_URI);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            //设置参数
            httpConn.setDoOutput(true);   //需要输出
            httpConn.setDoInput(true);   //需要输入
            httpConn.setUseCaches(false);  //不允许缓存
            httpConn.setRequestMethod("POST");   //设置POST方式连接
            //设置请求属性
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            httpConn.setRequestProperty("Charset", "UTF-8");
            //连接,也可以不用明文connect，使用下面的httpConn.getOutputStream()会自动connect
            httpConn.connect();
            //建立输入流，向指向的URL传入参数
            DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
            dos.writeBytes(entity);
            dos.flush();
            dos.close();
            //获得响应状态
            int resultCode = httpConn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == resultCode) {
                StringBuffer sb = new StringBuffer();
                String readLine = new String();
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
                while ((readLine = responseReader.readLine()) != null) {
                    sb.append(readLine).append("\n");
                }
                responseReader.close();
                String result = sb.toString();
                Log.d(TAG, "circlePictureHttpPost, result:" + result);
                if (result.contains("Upload Success")) {
                    return new JsonResultData(true, null);
                }
            }
        } catch (MalformedURLException e) {
            return new JsonResultData(false, "获取数据异常");
        } catch (IOException e) {
            return new JsonResultData(false, "获取数据异常");
        }
        return new JsonResultData(false, "获取数据异常");
    }


    public static JsonResultData circleInfoHttpGet(String entity) {
        entity = Utils.StringFormatToUTF8(entity);
        try {
            URL url = new URL(CIRCLE_INFO_GET_URI);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            //设置参数
            httpConn.setDoOutput(true);   //需要输出
            httpConn.setDoInput(true);   //需要输入
            httpConn.setUseCaches(false);  //不允许缓存
            httpConn.setRequestMethod("GET");   //设置POST方式连接
            //设置请求属性
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            httpConn.setRequestProperty("Charset", "UTF-8");
            //连接,也可以不用明文connect，使用下面的httpConn.getOutputStream()会自动connect
            httpConn.connect();
            //建立输入流，向指向的URL传入参数
            DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
            dos.writeBytes(entity);
            dos.flush();
            dos.close();
            //获得响应状态
            int resultCode = httpConn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == resultCode) {
                StringBuffer sb = new StringBuffer();
                String readLine = new String();
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
                while ((readLine = responseReader.readLine()) != null) {
                    sb.append(readLine).append("\n");
                }
                responseReader.close();
                String result = sb.toString();
                Log.d(TAG, "circleInfoHttpGet, result:" + result);
                Log.d("zhangtao", "circleInfoHttpGet, result:" + result);
                return new JsonResultData(true, result);
            }
        } catch (MalformedURLException e) {
            return new JsonResultData(false, "获取数据异常");
        } catch (IOException e) {
            return new JsonResultData(false, "获取数据异常");
        }
        return new JsonResultData(false, "获取数据异常");
    }

    public static Map<String,String> decodeJsonUtilXml(String content) {
        Map<String, String> xml = new HashMap<String, String>();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String nodeName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if(RESULT_TAG.equals(nodeName) == true){
                            xml.put(nodeName, parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }
            return xml;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }
}

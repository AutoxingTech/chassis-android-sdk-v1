package com.autoxing.robot_core.util;

import com.alibaba.fastjson.JSON;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetUtil {

    public enum HTTP_METHOD {
        get,
        post,
        delete,
        patch
    }

    private static String baseUrl = "http://tun.autoxing.com:6612";

    public static final String url_maps = baseUrl + "/maps";
    public static final String url_mappings = baseUrl + "/mappings";
    public static final String url_chassis_moves = baseUrl + "/chassis/moves";
    public static final String url_chassis_status = baseUrl + "/chassis/status";
    public static final String url_chassis_current_map = baseUrl + "/chassis/current-map";
    public static final String url_chassis_remote_action = baseUrl + "/chassis/remote/action";
    public static final String url_chassis_remote_twist = baseUrl + "/chassis/remote/twist";
    public static final String url_ws_topics = baseUrl + "/ws/topics";

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS) //连接超时
            .readTimeout(120, TimeUnit.SECONDS) //读取超时
            .writeTimeout(60, TimeUnit.SECONDS).build() ;//写超时

    public static void setUrlBase(String url){
        NetUtil.baseUrl = url;
    }

    public static String syncReq(String url) {
        return NetUtil.syncReq(url, HTTP_METHOD.get);
    }
    public static String syncReq(String url, HTTP_METHOD HttpMethod) {
        return NetUtil.syncReq(url,null, HttpMethod);
    }
    public static String syncReq(String url, Map map) {
        return NetUtil.syncReq(url, map, HTTP_METHOD.post);
    }

    public static <T> T syncReq(String url, Class<T> clazz){
        return syncReq(url,null, HTTP_METHOD.get, clazz);
    }
    public static <T> T syncReq(String url, HTTP_METHOD method, Class<T> clazz){
        return syncReq(url,null, method, clazz);
    }
    public static <T> T syncReq(String url, Map reqObj, Class<T> clazz){
        return syncReq(url, reqObj, HTTP_METHOD.post, clazz);
    }
    public static <T> T syncReq(String url, Map reqObj, HTTP_METHOD method, Class<T> clazz) {
        String res = NetUtil.syncReq(url, reqObj, HTTP_METHOD.get);
        res = lineToHump(res);
        T t = null;
        try {
            t = JSON.parseObject(res, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public static <T>List<T> syncReqList(String url, Class<T> clazz) {
        return syncReqList(url,null, HTTP_METHOD.get, clazz);
    }
    public static <T>List<T> syncReqList(String url, HTTP_METHOD method, Class<T> clazz) {
        return syncReqList(url,null, method, clazz);
    }
    public static <T>List<T> syncReqList(String url, Map reqObj, Class<T> clazz) {
        return syncReqList(url, reqObj, HTTP_METHOD.post, clazz);
    }
    public static <T>List<T> syncReqList(String url, Map reqObj, HTTP_METHOD method, Class<T> clazz) {
        String res =  NetUtil.syncReq(url,reqObj,method);
        res = lineToHump(res);
        List<T> ts = null;
        try {
            ts = JSON.parseArray(res, clazz);
        } catch (Exception e){
            e.printStackTrace();
        }
        return ts;
    }

    public static String syncReq(String url, Map reqObj, HTTP_METHOD httpMethod) {
        String res = null;
        Response response = syncReq2(url, reqObj, httpMethod);
        if (response != null)
        {
            try {
                res = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public static Response syncReq2(String url) {
        return NetUtil.syncReq2(url, HTTP_METHOD.get);
    }
    public static Response syncReq2(String url, HTTP_METHOD HttpMethod) {
        return NetUtil.syncReq2(url, null, HttpMethod);
    }
    public static Response syncReq2(String url, Map map) {
        return NetUtil.syncReq2(url, map, HTTP_METHOD.post);
    }

    public static Response syncReq2(String url, Map reqObj, HTTP_METHOD httpMethod) {
        String content = null;
        if (reqObj != null) {
            content = JSON.toJSONString(reqObj);
        }
        return syncReq3(url, content, httpMethod);
    }

    public static Response syncReq3(String url, String content, HTTP_METHOD httpMethod) {
        Request request = null;
        if (content == null) {
            if (httpMethod == HTTP_METHOD.post) {
                MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
                RequestBody body = RequestBody.create(null, "");
                request = new Request.Builder().url(url).post(body).build();
            } else if (httpMethod == HTTP_METHOD.patch) {
                MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
                RequestBody body = RequestBody.create(null, "");
                request = new Request.Builder().url(url).patch(body).build();
            } else if (httpMethod == HTTP_METHOD.delete) {
                FormBody body = new FormBody.Builder().build();
                request = new Request.Builder().url(url).delete(body).build();
            } else {
                request = new Request.Builder().url(url).build();
            }
        } else {
            MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
            RequestBody body = RequestBody.create(mediaType, content);

            if (httpMethod == HTTP_METHOD.post) {
                request = new Request.Builder().url(url).post(body).build();
            } else if (httpMethod == HTTP_METHOD.patch) {
                request = new Request.Builder().url(url).patch(body).build();
            } else {
                request = new Request.Builder().url(url).build();
            }
        }

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private static Pattern linePattern = Pattern.compile("_(\\w)");
    //下划线转驼峰
    private static String lineToHump(String str) {
        if(str == null){
            str = "";
        }
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

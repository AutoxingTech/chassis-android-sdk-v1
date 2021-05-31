package com.autoxing.robot.utils;
import com.alibaba.fastjson.JSON;
import okhttp3.*;
import okhttp3.internal.http.HttpMethod;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetUtil {

    public enum HTTP_METHOD{
        get,
        post,
        delete
    }

    private static String baseUrl = "http://10.10.40.92:8000";

    public static String url_maps = baseUrl+"/maps";
    public static String url_mappings = baseUrl+"/mappings";
    public static String url_current_map = baseUrl+"/chassis/current-map";
    public static String url_chassis_moves = baseUrl+"/chassis/moves";




    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS) //连接超时
            .readTimeout(120, TimeUnit.SECONDS) //读取超时
            .writeTimeout(60, TimeUnit.SECONDS).build() ;//写超时

    public static void setBaseUrl(String url){
        NetUtil.baseUrl = url;
    }

    public static String syncReq(String url) {
        return NetUtil.syncReq(url,HTTP_METHOD.get);
    }
    public static String syncReq(String url,HTTP_METHOD HttpMethod) {
        return NetUtil.syncReq(url,null,HttpMethod);
    }
    public static String syncReq(String url,Map map) {
        return NetUtil.syncReq(url,map,HTTP_METHOD.post);
    }


    public static <T> T syncReq(String url, Class<T> clazz){
        return syncReq(url,null,HTTP_METHOD.get,clazz);
    }
    public static <T> T syncReq(String url,HTTP_METHOD method, Class<T> clazz){
        return syncReq(url,null,method,clazz);
    }
    public static <T> T syncReq(String url,Map reqObj, Class<T> clazz){
        return syncReq(url,reqObj,HTTP_METHOD.post,clazz);
    }
    public static <T> T syncReq(String url,Map reqObj,HTTP_METHOD method,Class<T> clazz) {
        String res =  NetUtil.syncReq(url,reqObj,HTTP_METHOD.get);
        res = lineToHump(res);
        T t = null;
        try {
            t = JSON.parseObject(res, clazz);
        } catch (Exception e){
            //数据异常解析失败
        }
        return t;
    }

    public static <T>List<T> syncReqList(String url, Class<T> clazz){
        return syncReqList(url,null,HTTP_METHOD.get,clazz);
    }
    public static <T>List<T> syncReqList(String url,HTTP_METHOD method, Class<T> clazz){
        return syncReqList(url,null,method,clazz);
    }
    public static <T>List<T> syncReqList(String url,Map reqObj, Class<T> clazz){
        return syncReqList(url,reqObj,HTTP_METHOD.post,clazz);
    }
    public static <T>List<T> syncReqList(String url, Map reqObj, HTTP_METHOD method, Class<T> clazz) {
        String res =  NetUtil.syncReq(url,reqObj,method);
        res = lineToHump(res);
        List<T> ts = null;
        try {
            ts = JSON.parseArray(res, clazz);
        } catch (Exception e){
            //数据异常解析失败
        }
        return ts;
    }

    public static String syncReq(String url , Map reqObj,HTTP_METHOD HttpMethod){
        String res = null;
        System.out.println("reqUrl:");
        System.out.println(url);
        Request request = null;
        if(reqObj==null){

            if(HttpMethod==HTTP_METHOD.post){
                MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
                RequestBody body = RequestBody.create(null, "");
                request = new Request.Builder().url(url).post(body).build();
            }else if(HttpMethod==HTTP_METHOD.delete){
                FormBody body = new FormBody.Builder().build();
                request = new Request.Builder().url(url).delete(body).build();
                System.out.println("DELETE");
            }else{
                request = new Request.Builder().url(url).build();
            }

        }else{
            MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
            String content = JSON.toJSONString(reqObj);
            System.out.println(content);
            RequestBody body = RequestBody.create(mediaType, content);
            request = new Request.Builder().url(url).post(body).build();
        }

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            res = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("resP:");
        System.out.println(res);
        return res;
    }


    private static Pattern linePattern = Pattern.compile("_(\\w)");
    //下划线转驼峰
    private static String lineToHump(String str) {
        if(str==null){
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

package com.autoxing.robot.utils;
import com.alibaba.fastjson.JSON;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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


}

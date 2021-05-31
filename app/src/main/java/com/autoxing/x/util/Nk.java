package com.autoxing.x.util;

import okhttp3.*;

import java.io.IOException;
import java.util.Map;

public class Nk {
    private static OkHttpClient okHttpClient = new OkHttpClient();

    public void asyncReq(String url , Map reqObj, NetCallBack callBack){

        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //...
                callBack.fail();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    String result = response.body().string();
                    callBack.success(result);
                }
            }
        });
    }
}

package com.autoxing.x.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {



    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 20,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    public static void  run(CommonCallBack callBack){
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                callBack.run();
            }
        });
    }
}

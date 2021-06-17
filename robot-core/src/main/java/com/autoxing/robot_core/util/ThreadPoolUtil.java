package com.autoxing.robot_core.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 20,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());

    public static void runSync(CommonCallback callback) {

        CountDownLatch latch = new CountDownLatch(1);
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                callback.run();
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void runAsync(CommonCallback callback) {

        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                callback.run();
            }
        });
    }
}

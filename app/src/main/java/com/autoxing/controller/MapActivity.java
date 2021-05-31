package com.autoxing.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.autoxing.robot.Loaction;
import com.autoxing.robot.Map;
import com.autoxing.robot.Mapping;
import com.autoxing.robot.Platform;
import com.autoxing.x.util.CommonCallBack;
import com.autoxing.x.util.PGM;
import com.autoxing.x.util.ThreadPoolUtil;
import okhttp3.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.zip.InflaterInputStream;

public class MapActivity extends AppCompatActivity implements View.OnClickListener {

    public static Map selMap = null;

    private ImageView image;
    private EditText editTextX;
    private EditText editTextY;
    private EditText editTextZ;
    private EditText editTextWidthDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        image = findViewById(R.id.map_img);

        findViewById(R.id.setCurrentBtn).setOnClickListener(this);
        findViewById(R.id.post_moves).setOnClickListener(this);

        this.editTextX = findViewById(R.id.x);
        this.editTextY = findViewById(R.id.y);
        this.editTextZ = findViewById(R.id.z);
        this.editTextWidthDraw = findViewById(R.id.withYaw);


        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bmp = null;
                try {
                    URL myurl = new URL(selMap.getUrl()+"/thumbnail");
                    // 获得连接
                    HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
                    conn.setConnectTimeout(6000);//设置超时
                    conn.setDoInput(true);
                    conn.setUseCaches(false);//不缓存
                    conn.connect();
                    InputStream is = conn.getInputStream();//获得图片的数据流
                    bmp = BitmapFactory.decodeStream(is);//读取图像数据
                    //读取文本数据
                    //byte[] buffer = new byte[100];
                    //inputStream.read(buffer);
                    //text = new String(buffer);
                    is.close();
                    Bitmap finalBmp = bmp;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            image.setImageBitmap(finalBmp);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void parsePGM(){

        Mapping mapping = new Mapping();

        ThreadPoolUtil.run(new CommonCallBack() {
            @Override
            public void run() {
                String carToMap = selMap.getCarToMap();


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
                        byte[] zipData =  decoder.decode(carToMap);
                        ByteArrayInputStream bais = new ByteArrayInputStream(zipData);
//                        String path = "你的.pgm路径";
                        int iw, ih;
                        int[] pix;
                        PGM pgm = new PGM();
                        pgm.readPGMHeader(bais);
                        iw = pgm.getWidth();
                        ih = pgm.getHeight();
                        pix = pgm.readData(iw, ih, 5);   //P5-Gray image
                        Bitmap bitmap = Bitmap.createBitmap(iw,ih, Bitmap.Config.ARGB_4444);
                        System.out.println(pix);
                        bitmap.setPixels(pix,0,iw,0,0,iw,ih);
                        MapActivity.this.image.setImageBitmap(bitmap);
                    }
                });
            }
        });

//
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.setCurrentBtn:{
                this.setCurrntMap(selMap.getId());
            }
            break; case R.id.post_moves:{
                this.postMove();
            }
            break;
        }
    }

    private void postMove() {
        String x = this.editTextX.getText().toString();
        String y = this.editTextY.getText().toString();
        String z = this.editTextZ.getText().toString();
        String withYaw = this.editTextWidthDraw.getText().toString();
        if(x.length()>0&&y.length()>0&&z.length()>0&&withYaw.length()>0){
            float ix = Float.parseFloat(x);
            float iy = Float.parseFloat(y);
            float iz = Float.parseFloat(z);
            float iwithYaw = Float.parseFloat(withYaw);

            Platform p = Platform.getInstance();
            Loaction location = new Loaction();
            location.setX(ix);
            location.setY(iy);
            location.setZ(iz);

            ThreadPoolUtil.run(new CommonCallBack() {
                @Override
                public void run() {
                    Platform.getInstance().moveTo(location,null,iwithYaw);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            });

        }
    }

    private void setCurrntMap(int id) {

        ThreadPoolUtil.run(new CommonCallBack() {
            @Override
            public void run() {
                Platform.getInstance().setCurrentMap(id);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {  }
                });
            }
        });
    }


    byte[] decompress(byte[] compress) throws Exception {
       int  EOF = -1;
       int BUFFER_SIZE=1024;
        ByteArrayInputStream bais = new ByteArrayInputStream(compress);
        InflaterInputStream iis = new InflaterInputStream(bais);
//         return  iis.readAllBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int c = 0;
        byte[] buf = new byte[BUFFER_SIZE];
        while (true) {
            c = iis.read(buf);

            if (c == EOF)
                break;

            baos.write(buf, 0, c);
        }

        baos.flush();

        return baos.toByteArray();
    }

    private void loadMap(String  mapurl){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(mapurl).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //...
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    String result = response.body().string();

                    JSONObject obj = JSON.parseObject(result);
                    String cartoMap = obj.getString("carto_map");
                    System.out.println(cartoMap);

                    java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
                    byte[] zipData =  decoder.decode(cartoMap);

                    System.out.println("zipData:" + zipData.length);

                    byte[] decodedString = new byte[0];
                    try {
                        decodedString = MapActivity.this.decompress(zipData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                    String message1 = new String(decodedString,0, decodedString.length,"UTF-8");//原文出自【易百教程】，商业转载请联系作者获得授权，非商业请保留原文链接：https://www.yiibai.com/javazip/javazip_inflaterinputstream_read.html
//
//                        System.out.println(message1);
//                    final Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            MapActivity.this.image.setImageBitmap(decodedByte);
                        }
                    });
                }
            }
        });
    }

}

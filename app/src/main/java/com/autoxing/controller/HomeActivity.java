package com.autoxing.controller;

import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.autoxing.robot.Map;
import com.autoxing.robot.Mapping;
import com.autoxing.robot.Platform;
import com.autoxing.x.util.CommonCallBack;
import com.autoxing.x.util.ThreadPoolUtil;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        System.out.println("HOME-----------HOME------------");


        Platform.newInstance("http://10.10.40.92:8000/");


        findViewById(R.id.map_list).setOnClickListener(this);
        findViewById(R.id.mapping_list).setOnClickListener(this);
        findViewById(R.id.start_mapping).setOnClickListener(this);
        findViewById(R.id.stop_mapping).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.map_list:{
                startActivity(new Intent(HomeActivity.this,MainActivity.class));
            }
            break;
            case R.id.mapping_list:{
                startActivity(new Intent(HomeActivity.this,MappingActivity.class));
            }
            break;
            case R.id.start_mapping:{
                ThreadPoolUtil.run(new CommonCallBack() {
                    @Override
                    public void run() {
                      Mapping mapping =  Platform.getInstance().startMapping();
                      if(mapping!=null){
                          System.out.println(mapping.getStatus());
                      }
                    }
                });

            }
            break;
            case R.id.stop_mapping:{
                ThreadPoolUtil.run(new CommonCallBack() {
                    @Override
                    public void run() {
                        Platform.getInstance().stop();
                    }
                });

            }
            break;
        }
    }
}

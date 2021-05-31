package com.autoxing.controller;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.autoxing.robot.Map;
import com.autoxing.robot.Mapping;
import com.autoxing.robot.MappingStatus;
import com.autoxing.robot.Platform;
import com.autoxing.x.util.CommonCallBack;
import com.autoxing.x.util.ThreadPoolUtil;

import java.util.ArrayList;
import java.util.List;

public class MappingActivity extends AppCompatActivity {

    private class MapAdapter extends ArrayAdapter<Mapping> {

        private int resourceId;

        // 适配器的构造函数，把要适配的数据传入这里
        public MapAdapter(Context context, int textViewResourceId, List<Mapping> objects){
            super(context,textViewResourceId,objects);
            resourceId=textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            Mapping map = getItem(position); //获取当前项的Map实例

            // 加个判断，以免ListView每次滚动时都要重新加载布局，以提高运行效率
            View view;
            MappingActivity.MapAdapter.ViewHolder viewHolder;
            if (convertView==null){

                // 避免ListView每次滚动时都要重新加载布局，以提高运行效率
                view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
                // 避免每次调用getView()时都要重新获取控件实例
                viewHolder=new MappingActivity.MapAdapter.ViewHolder();
                viewHolder.mapName =view.findViewById(R.id.map_name);
                viewHolder.mapImg =view.findViewById(R.id.map_img);
                // 将ViewHolder存储在View中（即将控件的实例存储在其中）
                view.setTag(viewHolder);
            } else{
                view=convertView;
                viewHolder=(MappingActivity.MapAdapter.ViewHolder) view.getTag();
            }

            // 获取控件实例，并调用set...方法使其显示出来
            viewHolder.mapName.setText("MappingID:"+map.getId());

            return view;
        }



        // 定义一个内部类，用于对控件的实例进行缓存
        class ViewHolder{
            TextView mapName;
            ImageView mapImg;
        }
    }

    // mapList用于存储数据
    private List<Mapping> mapList=new ArrayList<>();
    MappingActivity.MapAdapter adapter = null;
    ListView listView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);
        initMappings();
        this.adapter=new MappingActivity.MapAdapter(MappingActivity.this,R.layout.map_item,mapList);

        this.listView=findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Mapping mapping = mapList.get(position);

                ThreadPoolUtil.run(new CommonCallBack() {
                    @Override
                    public void run() {
                        MappingStatus mappingStatus = mapping.getStatus();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MappingActivity.this,mappingStatus+"",1200).show();
                            }
                        });
                    }
                });
//
            }
        });
    }

    private void initMappings() {
        ThreadPoolUtil.run(new CommonCallBack() {
            @Override
            public void run() {
                Platform platForm = Platform.getInstance();
                List<Mapping> maps = platForm.getMappings();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mapList .addAll(maps);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
}

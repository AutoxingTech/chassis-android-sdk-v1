package com.autoxing.controller;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.autoxing.robot.Platform;
import com.autoxing.robot.Map;
import com.autoxing.x.util.CommonCallBack;
import com.autoxing.x.util.ThreadPoolUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private class MapAdapter extends ArrayAdapter<Map>{

        private int resourceId;

        // 适配器的构造函数，把要适配的数据传入这里
        public MapAdapter(Context context, int textViewResourceId, List<Map> objects){
            super(context,textViewResourceId,objects);
            resourceId=textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            Map map = getItem(position); //获取当前项的Map实例

            // 加个判断，以免ListView每次滚动时都要重新加载布局，以提高运行效率
            View view;
            ViewHolder viewHolder;
            if (convertView==null){

                // 避免ListView每次滚动时都要重新加载布局，以提高运行效率
                view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
                // 避免每次调用getView()时都要重新获取控件实例
                viewHolder=new ViewHolder();
                viewHolder.mapName =view.findViewById(R.id.map_name);
                viewHolder.mapImg =view.findViewById(R.id.map_img);
                // 将ViewHolder存储在View中（即将控件的实例存储在其中）
                view.setTag(viewHolder);
            } else{
                view=convertView;
                viewHolder=(ViewHolder) view.getTag();
            }

            // 获取控件实例，并调用set...方法使其显示出来
            viewHolder.mapName.setText(map.getMapName());
            this.LoadMapImg( viewHolder.mapImg,map);
            return view;
        }

        void LoadMapImg(ImageView mapImg,Map map){

            ThreadPoolUtil.run(new CommonCallBack() {
                @Override
                public void run() {
                    String carToMap = map.getCarToMap();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {  }
                    });
                }
            });

        }

        // 定义一个内部类，用于对控件的实例进行缓存
        class ViewHolder{
            TextView mapName;
            ImageView mapImg;
        }
    }

    // mapList用于存储数据
    private List<Map> mapList=new ArrayList<>();
    MapAdapter adapter = null;
    ListView listView = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 先拿到数据并放在适配器上
        initMaps(); //初始化水果数据
        this.adapter=new MapAdapter(MainActivity.this,R.layout.map_item,mapList);

        // 将适配器上的数据传递给listView
        this.listView=findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        // 为ListView注册一个监听器，当用户点击了ListView中的任何一个子项时，就会回调onItemClick()方法
        // 在这个方法中可以通过position参数判断出用户点击的是那一个子项
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map map = mapList.get(position);
//                Toast.makeText(MainActivity.this,mapItem.getMap_name(),Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("url", map.getUrl());
                intent.putExtra("id",map.getId());
                //setClass函数的第一个参数是一个Context对象
                //Context是一个类，Activity是Context类的子类，也就是说，所有的Activity对象，都可以向上转型为Context对象
                //setClass函数的第二个参数是一个Class对象，在当前场景下，应该传入需要被启动的Activity类的class对象
                intent.setClass(MainActivity.this, MapActivity.class);
                MapActivity.selMap = map;
                startActivity(intent);
            }
        });

    }


    private void initMaps() {

        ThreadPoolUtil.run(new CommonCallBack() {
            @Override
            public void run() {
                Platform platForm = Platform.getInstance();
                List<Map> maps = platForm.getMaps();

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

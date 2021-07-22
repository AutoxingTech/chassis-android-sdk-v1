package com.autoxing.robot_core;

import com.autoxing.robot_core.bean.Map;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MapHelper {

    public static Map loadMap(String path) {
        String content = null;
        try {
            File file = new File(path);
            int length = (int)file.length();
            byte[] buff = new byte[length];
            FileInputStream fin = new FileInputStream(file);
            fin.read(buff);
            fin.close();
            content = new String(buff,"UTF-8");
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }

        Map map = new Map();
        map.setData(content);
        return map;
    }
}

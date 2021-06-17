package com.autoxing.robot_core.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * 提供文件处理工具类，文件读取、保存
 *
 * @author chenlh
 * @since 1.0
 */
@SuppressWarnings("unused")
public class FileUtil {
    @SuppressWarnings("unused")
    public static boolean isFileExists(String filePath) {
        return isFileExists(getFileByPath(filePath));
    }

    public static boolean isFileExists(File file) {
        return file != null && file.exists();
    }

    public static File getFileByPath(String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }

    @SuppressWarnings("unused")
    public static boolean isDir(String dirPath) {
        return isDir(getFileByPath(dirPath));
    }

    public static boolean isDir(File file) {
        return isFileExists(file) && file.isDirectory();
    }

    @SuppressWarnings("unused")
    public static boolean isFile(String filePath) {
        return isFile(getFileByPath(filePath));
    }

    public static boolean isFile(File file) {
        return isFileExists(file) && file.isFile();
    }

    @SuppressWarnings("unused")
    public static boolean createOrExistsDir(String dirPath) {
        return createOrExistsDir(getFileByPath(dirPath));
    }

    public static boolean createOrExistsDir(File file) {
        // 如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    public static void saveFile(String savePath, byte[] bytes) {
        saveFile(savePath, bytes, false);
    }

    private static void checkDirExist(String fileName) {
        File dir = new File(fileName).getParentFile();
        if (dir != null && !dir.exists()) {
            boolean success = dir.mkdirs();
            System.out.println("success = " + success);
        }
    }

    public static void saveFile(String savePath, byte[] bytes, boolean isAppend) {
        checkDirExist(savePath);
        FileOutputStream fop = null;
        File file;
        try {
            file = new File(savePath);
            if (!file.exists()) {
                boolean success = file.createNewFile();
                System.out.println("success = " + success);
            }

            fop = new FileOutputStream(file, isAppend);
            fop.write(bytes);
            fop.flush();
            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isSpace(String s) {
        return (s == null || s.trim().length() == 0);
    }

    @SuppressWarnings("unused")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static HashSet<String> readPropFilesKeySet(String fileName) {
        HashSet<String> result = new HashSet<>();
        Properties prop = new Properties();
        if (new File(fileName).exists()) {
            try {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8);

                prop.load(isr);
                Set<Object> keySet = prop.keySet();
                for (Object obj : keySet) {
                    result.add(String.valueOf(obj));
                }
                isr.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @SuppressWarnings("unused")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String readPropFilesForKey(String fileName, String key) {
        String result = null;
        Properties prop = new Properties();
        if (new File(fileName).exists()) {
            try {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8);
                prop.load(isr);
                result = prop.getProperty(key);
                isr.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @SuppressWarnings("unused")
    public static String readFileToString(String fileName) {
        return readFileToString(new File(fileName));
    }

    public static String readFileToString(File fin) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(fin));
            StringBuilder sb = new StringBuilder();
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                if (lineNum != 0)
                    sb.append("\n");
                sb.append(line);
                lineNum++;
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    public static byte[] getContent(String filePath) throws IOException {
        File file = new File(filePath);
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            System.out.println("file too big...");
            return null;
        }
        FileInputStream fi = new FileInputStream(file);
        byte[] buffer = new byte[(int) fileSize];
        int offset = 0;
        int numRead;
        while (offset < buffer.length
                && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }
        // 确保所有数据均被读取
        if (offset != buffer.length) {
            fi.close();
            throw new IOException("Could not completely read file " + file.getName());
        }
        fi.close();
        return buffer;
    }
}

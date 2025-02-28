package com.android.documentsui.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import com.android.documentsui.provider.FileUtils;


public class NetUtils {
    protected static final String TAG = "NetUtils";

    public static String getLinuxApp() {
        try {
            URL url = new URL(
                    "http://127.0.0.1:18080/api/v1/apps?page=" + 1 + "&page_size=" + 200);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();

            connection.setDoOutput(false);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(true);
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(3000);
            connection.connect();
            int code = connection.getResponseCode();
            String res = "";
            if (code == 200) { // 
                // 
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String line = null;

                while ((line = reader.readLine()) != null) {
                    res += line + "\n";
                }
                reader.close();
            }
            connection.disconnect();

            // Log.i(TAG,"getLinuxApp res "+res);
            try {
                JSONObject jsonResponse = new JSONObject(res);
                JSONObject mpRes = jsonResponse.getJSONObject("data");
                // 获取内层的 "data" 数组
                JSONArray responseArray = mpRes.getJSONArray("data");

                // 遍历数组并解析每个对象
                for (int i = 0; i < responseArray.length(); i++) {
                    JSONObject item = responseArray.getJSONObject(i);
                    String name = item.getString("Name").toString().replaceAll(" ", "_");;
                    String exec = item.getString("Path").replaceAll(" %F", "").replaceAll(" %u", "").replaceAll(" %U", "").replaceAll(" ", "");
                    String IconPath = item.getString("IconPath");
                    String key = name ;
                    if(FileUtils.containsChinese(name)){
                       int lastIndex = exec.lastIndexOf('/');
                       if(lastIndex > 0){
                          key = exec.substring(lastIndex+1);
                       }
                    }
                    Log.i(TAG,"FastBitmapDrawable_key: "+key + ",IconPath: "+IconPath + ",exec: "+exec+",name: "+name);
                    FileUtils.setSystemProperty(key,IconPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String gotoLinuxApp(String name, String exec) {
        try {
            // 目标URL
            String targetURL = "http://127.0.0.1:18080/api/v1/xserver";
            // 创建URL对象
            URL url = new URL(targetURL);
            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为POST
            connection.setRequestMethod("POST");

            // 设置允许输出
            connection.setDoOutput(true);

            // 设置请求属性，例如Content-Type
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // POST参数
            String postParameters = "App=" + name + "&Path=" + exec + "&Display=:0";
            Log.i("bella", "gotoLinuxApp postParameters: " + postParameters);
            // 获取输出流并写入参数
            try (OutputStream os = connection.getOutputStream()) {
                os.write(postParameters.getBytes(StandardCharsets.UTF_8));
            }

            // 获取响应码
            int responseCode = connection.getResponseCode();
            Log.i("bella", "gotoLinuxApp Response Code: " + responseCode);
            // 根据需要处理响应内容
            // ...

            // 关闭连接
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getFdeMode() {
        try {
            URL url = new URL(
                    "http://127.0.0.1:18080/api/v1/fde_mode");
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();

            connection.setDoOutput(false);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(true);
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(3000);
            connection.connect();
            int code = connection.getResponseCode();
            String res = "";
            if (code == 200) { //
                //
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String line = null;

                while ((line = reader.readLine()) != null) {
                    res += line + "\n";
                }
                reader.close();
            }
            connection.disconnect();

            Log.i("bella", "getFdeMode res " + res);
            try {
                JSONObject jsonResponse = new JSONObject(res);
                JSONObject mpData = jsonResponse.getJSONObject("Data");
                return mpData.getString("FDEMode");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
            // Map<String, Object> mpRes = new Gson().fromJson(res, new TypeToken<Map<String, Object>>() {
            // }.getType());
            // Map<String, Object> mpData = (Map<String, Object>) mpRes.get("Data");
            // return  mpData.get("FDEMode").toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

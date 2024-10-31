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

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;




public class NetUtils {
    
    public static String  getLinuxApp(){
        try {
            URL url = new URL(
                    "http://127.0.0.1:18080/api/v1/apps?page="+1+"&page_size="+100);
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

            // Log.i("bella","getLinuxApp res "+res);
            Map<String, Object> mpRes = new Gson().fromJson(res, new TypeToken<Map<String, Object>>() {
                    }.getType());
            Map<String, Object> mpData = (Map<String, Object>) mpRes.get("data");
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) mpData.get("data");
            if(responseList != null){
                Log.i("bella","getLinuxApp responseList " + responseList.size());
                for(Map<String,Object> mp : responseList){
                    // Log.i("bella","getLinuxApp IconPath " + mp.get("IconPath") + " ,Path : "+mp.get("Path")+ " ,IconType : "+mp.get("IconType")+ " ,Name : "+mp.get("Name"));
                }
            }

            

            return res ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null ;
    }



    public static String  gotoLinuxApp(String name,String exec){
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
            String postParameters = "App="+name+"&Path="+exec+"&Display=:0";
            Log.i("bella","gotoLinuxApp postParameters: " + postParameters);
            // 获取输出流并写入参数
            try (OutputStream os = connection.getOutputStream()) {
                os.write(postParameters.getBytes(StandardCharsets.UTF_8));
            }
 
            // 获取响应码
            int responseCode = connection.getResponseCode();
            Log.i("bella","gotoLinuxApp Response Code: " + responseCode);
            // 根据需要处理响应内容
            // ...
 
            // 关闭连接
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null ;
    }
}

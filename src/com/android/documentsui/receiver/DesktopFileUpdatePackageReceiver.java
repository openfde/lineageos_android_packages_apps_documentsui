package com.android.documentsui.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.android.documentsui.DocumentsApplication;
import com.android.documentsui.IpcService;
import org.json.JSONObject;

public class DesktopFileUpdatePackageReceiver extends BroadcastReceiver {

    private static final String TAG = "DesktopFileUpdatePackageReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        String mode = intent.getStringExtra("mode");
        String path = intent.getStringExtra("path");
        String packageName = intent.getStringExtra("packageName");
        String data = intent.getStringExtra("data");

       try {
        if(data !=null && !"".equals(data)){
            JSONObject jsonObject = new JSONObject(data);
            mode = jsonObject.getString("OpCode");
            path = jsonObject.getString("FileName");
        }
      
        Log.i(TAG, "MediaProvider--DesktopFileUpdate---onReceive--action " + action + ",mode: " + mode + ",path: " + path);

        IpcService ipcService = DocumentsApplication.getInstance().getIpcService();
        if (ipcService != null) {
            ipcService.gotoClientApp("UPDATE_DESKTOP", mode + "###" + path);
        } else {
            Log.i(TAG, "ipcService is null");
        }
       } catch (Exception e) {
        e.printStackTrace();
       }

    }

}

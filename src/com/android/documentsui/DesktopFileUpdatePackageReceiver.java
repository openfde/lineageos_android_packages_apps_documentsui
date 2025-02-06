package com.android.documentsui;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
// import com.android.documentsui.IpcService;
// import com.android.documentsui.DocumentsApplication;


public class DesktopFileUpdatePackageReceiver extends BroadcastReceiver {
    private static final String TAG = "DesktopFileUpdatePackageReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        String type =  intent.getStringExtra("mode");
        String path =  intent.getStringExtra("path");

        Log.i(TAG,"MediaProvider--DesktopFileUpdate---onReceive--action "+action +",type: "+type+",path: "+path);

        IpcService ipcService  = DocumentsApplication.getInstance().getIpcService();
        if(ipcService !=null ){
            ipcService.gotoClientApp("UPDATE_DESKTOP",type+"###"+path);
        }else {
            Log.i(TAG,"ipcService is null");
        }

    }
    
}

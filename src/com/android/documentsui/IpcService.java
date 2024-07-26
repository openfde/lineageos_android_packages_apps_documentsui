package com.android.documentsui;

import java.io.File;

import com.android.documentsui.base.Providers;
import com.android.documentsui.files.FilesActivity;
import com.android.documentsui.provider.FileUtils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.provider.DocumentsContract;
import android.content.ComponentName;


public class IpcService extends Service {
    Context context ;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }



    private final IDocAidlInterface.Stub myBinder = new IDocAidlInterface.Stub() {
        @Override
        public String basicIpcMethon(String method,String params) throws RemoteException {
            Log.i("bella","basicIpcMethon.....method........ "+method + ",params "+params);
            if("OPEN_DOC".equals(method)){
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String path = "content://"+Providers.AUTHORITY_STORAGE+"/document/"+Providers.ROOT_ID_DESKTOP+"%2f"+params;
                Uri uri = Uri.parse(path);
                String mimeType = FileUtils.getMimeType(new File(Providers.PATH_ID_DESKTOP+params));
                if(mimeType.contains("image")){
                    intent.setDataAndType(uri, "image/*");
                }else if(mimeType.contains("text")){
                    intent.setDataAndType(uri, "text/plain");
                }else{
                    intent.setDataAndType(uri, "application/*");
                }
                intent.putExtra("docTitle",params);
                int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_SINGLE_TOP;
                flags |= Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                flags |= Intent.FLAG_ACTIVITY_NEW_TASK;
                intent.setFlags(flags);
                context.startActivity(intent);
            }else if("OPEN_DIR".equals(method)){
                  Intent intent = new Intent();
                  ComponentName componentName = new ComponentName( "com.android.documentsui", "com.android.documentsui.files.FilesActivity"  );
                  intent.setComponent(componentName);
                  intent.putExtra("getPath", "/mnt/sdcard/Desktop/");
                  intent.putExtra("childPath",params);
                  int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_SINGLE_TOP;
                    flags |= Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    flags |= Intent.FLAG_ACTIVITY_NEW_TASK;
                    intent.setFlags(flags);
                  startActivity(intent);
            }else if("DELETE_FILE".equals(method)){
                FileUtils.deleteFiles(params);
            }else if("NEW_FILE".equals(method)){
                FileUtils.newFile();
            }else if("NEW_DIR".equals(method)){
                FileUtils.newDir();
            }
            return "this is document ui app";
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }
}
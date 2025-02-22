package com.android.documentsui;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.database.Cursor;
import android.content.ComponentName;
import android.os.Bundle;

import com.android.documentsui.base.Providers;
import com.android.documentsui.base.UserId;
import com.android.documentsui.files.FilesActivity;
import com.android.documentsui.provider.FileUtils;
import com.android.documentsui.ui.OpenLinuxAppActivity;
import com.android.documentsui.ui.RenameDialogActivity;
import com.android.documentsui.util.NetUtils;
import com.android.documentsui.util.SPUtils;
import android.database.ContentObserver;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import android.app.ActivityManager;
import android.app.Activity;

public class IpcService extends Service {
    Context context ;

    private static final String TAG = "IpcService";

    private IDataChangedCallback dataChangedCallback;

    public static final String ACTION_UPDATE_FILE = "com.android.documentsui.UPDATE_FILE";

    private Handler handler = new Handler();


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        DocumentsApplication.getInstance().setIpcService(this);
        
        // LockingContentObserver mObserver = new LockingContentObserver(new ContentLock(),  () -> {
        //     Log.i(TAG,"ContentLock........ ");
        // });
    }

   

    private final IDocAidlInterface.Stub myBinder = new IDocAidlInterface.Stub() {

        @Override
        public void register( IDataChangedCallback callback){
             dataChangedCallback = callback;   
        }

        @Override
        public String basicIpcMethon(String method,String params) throws RemoteException {
            Log.i(TAG,"basicIpcMethon.....method........ "+method + ",params "+params);

            if(FileUtils.OPEN_FILE.equals(method)){
                finishDialog();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String path = "content://"+Providers.AUTHORITY_STORAGE+"/document/"+Providers.ROOT_ID_DESKTOP+"%2f"+params;
                Uri uri = Uri.parse(path);
                String mimeType = FileUtils.getMimeType(new File(Providers.PATH_ID_DESKTOP+params));
                if (mimeType == null) {
                    if (params.contains(".txt") || params.contains(".json")  || params.contains(".md")) {
                        intent.setDataAndType(uri, "text/plain");
                    } else {
                        intent.setDataAndType(uri, "application/*");
                    }
                } else if (mimeType.contains("image")) {
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
            }else if(FileUtils.OPEN_DIR.equals(method)){
                finishDialog();
                //   Intent intent = new Intent();
                //   ComponentName componentName = new ComponentName( "com.android.documentsui", "com.android.documentsui.files.FilesActivity"  );
                //   intent.setComponent(componentName);
                //   intent.putExtra("getPath", FileUtils.PATH_ID_DESKTOP);
                //   intent.putExtra("childPath",params);
                //   int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_SINGLE_TOP;
                //     flags |= Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                //     flags |= Intent.FLAG_ACTIVITY_NEW_TASK;
                //     intent.setFlags(flags);
                //   startActivity(intent);

                  Intent intent = new Intent();
                  intent.setClass(context, FilesActivity.class);
                  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                  intent.putExtra("childPath",params);
                  SPUtils.putDocInfo(context,"getPath",FileUtils.PATH_ID_DESKTOP);
                  context.startActivity(intent);
            }else if(FileUtils.OPEN_LINUX_APP.equals(method)){
                String[] arrParams = params.split("###");
                String name = arrParams[0].trim().replaceAll("%[FfUu]", "");
                String exec = arrParams[1].trim().replaceAll("%[FfUu]", "");
                String type = arrParams[2];
                String fileName =  name+".desktop";
                if(arrParams.length > 3){
                    fileName = arrParams[3];
                }
                String path = "content://" + Providers.AUTHORITY_STORAGE + "/document/" + Providers.ROOT_ID_DESKTOP + "%2f" + fileName;
                if (!"open".equals(type)) {
                    new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground(Void... voids) {
                            return NetUtils.getFdeMode();
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            super.onPostExecute(result);
                            Intent intent = new Intent();
                            intent.setClass(context, OpenLinuxAppActivity.class);
                            intent.putExtra("openParams", params);
                            intent.putExtra("fdeModel", result);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    }.execute();
                    // Uri uri = Uri.parse(path);
                    // Intent targetIntent = new Intent(Intent.ACTION_VIEW);
                    // targetIntent.setDataAndType(uri, "application/vnd.desktop");
                    // targetIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    // Intent chooser = Intent.createChooser(targetIntent, "选择应用打开desktop文件");
                    // int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_SINGLE_TOP;
                    // flags |= Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    // flags |= Intent.FLAG_ACTIVITY_NEW_TASK;
                    // chooser.setFlags(flags);
                    // context.startActivity(chooser);
                } else {
                    Log.i(TAG, "bella fileName: " + fileName + "   ,path : "+path);
                    Uri uri = Uri.parse(path);
                    Intent shareIntent = new Intent(Intent.ACTION_VIEW);
                    shareIntent.setDataAndType(uri, "application/vnd.desktop");
                    shareIntent.putExtra("fromOther", "Launcher");
                    shareIntent.putExtra("vnc_activity_name", name);
                    shareIntent.putExtra("App", name);
                    shareIntent.putExtra("openParams", params);
                    shareIntent.putExtra("docTitle", fileName);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_SINGLE_TOP;
                    flags |= Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    flags |= Intent.FLAG_ACTIVITY_NEW_TASK;
                    shareIntent.setFlags(flags);
                    startActivity(shareIntent);
                }
            } else if (FileUtils.DELETE_FILE.equals(method)) {
                FileUtils.deleteFiles(params);
                // gotoClientApp("DELETE");
            }else if(FileUtils.NEW_FILE.equals(method)){
                String fileName = FileUtils.newFile();
                gotoClientApp("NEW_FILE",fileName);
            }else if(FileUtils.NEW_DIR.equals(method)){
                String fileName = FileUtils.newDir();
                gotoClientApp("NEW_DIR",fileName);
            }else if(FileUtils.COPY_DIR.equals(method) || FileUtils.COPY_FILE.equals(method)){
                // Intent intent = new Intent(ACTION_UPDATE_FILE);
                // intent.putExtra("EXTRA_DATA", params);
                // intent.putExtra("EXTRA_TYPE", 1);
                // context.sendBroadcast(intent);
                SPUtils.putDocInfo(context, FileUtils.FILE_OPERATE, FileUtils.OP_COPY);
                Uri derivedUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Desktop/"+params);
                FileUtils.copyFileToClipboard(context,derivedUri);
                SPUtils.putDocInfo(context, FileUtils.FILE_DESKTOP_NAME, "");
            }else if(FileUtils.RENAME_FILE.equals(method) || FileUtils.RENAME_DIR.equals(method) ){
                try {
//                    gotoClientApp("RENAME",params);
//                    renameDialog(FileUtils.PATH_ID_DESKTOP+arrFileName[0]);

                    Intent intent = new Intent();
                    intent.setClass(context, RenameDialogActivity.class);
                    intent.putExtra("oldFileName",params);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(FileUtils.CUT_DIR.equals(method)  || FileUtils.CUT_FILE.equals(method)){
                // Intent intent = new Intent(ACTION_UPDATE_FILE);
                // intent.putExtra("EXTRA_DATA", params);
                // intent.putExtra("EXTRA_TYPE", 2);
                // context.sendBroadcast(intent);
                SPUtils.putDocInfo(context, FileUtils.FILE_OPERATE, FileUtils.OP_CUT);
                SPUtils.putDocInfo(context, FileUtils.FILE_DESKTOP_NAME, params);
                Uri derivedUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Desktop/"+params);
                FileUtils.copyFileToClipboard(context,derivedUri);
            }else if(FileUtils.PASTE_DIR.equals(method) || FileUtils.PASTE_FILE.equals(method)){
                // Intent intent = new Intent(ACTION_UPDATE_FILE);
                // intent.putExtra("EXTRA_DATA", params);
                // intent.putExtra("EXTRA_TYPE", 3);
                // context.sendBroadcast(intent);
                Uri uri = FileUtils.pasteFileFromClipboard(context);
                if(uri == null){
                    Log.e(TAG, "uri is null ");
                }else{
                    Log.i(TAG, "uri "+uri.toString());
                    String fileName = FileUtils.extractFileName(uri.getLastPathSegment()) ;
                    String newFilePath = FileUtils.getUniqueFileName(FileUtils.PATH_ID_DESKTOP,fileName);
                    Log.i(TAG, "newFilePath "+newFilePath + " , fileName "+fileName + " ,uriPath: "+uri.getPath() );
                    File destinationFile =  new File(FileUtils.PATH_ID_DESKTOP,newFilePath);
                    
                    String opStr = SPUtils.getDocInfo(context, FileUtils.FILE_OPERATE);
                    if(!fileName.contains(".")){
                        FileUtils.copyFolder(FileUtils.PATH_ID_DESKTOP+fileName,FileUtils.PATH_ID_DESKTOP+newFilePath);
                    }else{
                        FileUtils.copyUriToFile(context,uri,destinationFile);
                    }

                    SPUtils.putDocInfo(context, FileUtils.FILE_DESKTOP_NAME, "");
                    if(FileUtils.OP_CUT.equals(opStr)){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "opStr  "+opStr);
                                FileUtils.deleteFileWithUri(context,uri);
                                FileUtils.cleanClipboard(context);
                                
                            }
                        }, 1000);
                    }
                    gotoClientApp("PASTE");
                }

            }else if(FileUtils.FILE_LIST.equals(method)) {
            //    return FileUtils.getDesktopFiles();
            }else if(FileUtils.OP_INIT.equals(method)) {
                FileUtils.createDesktopDir();
            }else if(FileUtils.OP_CREATE_ANDROID_ICON.equals(method)) {
                FileUtils.createAllAndroidIconToLinux(context,params);
            }else if(FileUtils.OP_CREATE_LINUX_ICON.equals(method)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        NetUtils.getLinuxApp();
                    }
                }).start();
            }else if(FileUtils.CLICK_BLANK.equals(method)){
                finishDialog();
            }
            return "1";
        }
    };


    public  void gotoClientApp(String params){
       try {
        dataChangedCallback.onCallback(params);
       } catch (Exception e) {
        e.printStackTrace();
       }
    }

    public  void gotoClientApp(String method,String params){
        try {
         dataChangedCallback.onCallbackString(method,params);
        } catch (Exception e) {
         e.printStackTrace();
        }
     }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public void finishDialog(){
        Activity activity = DocumentsApplication.getInstance().getCurrentActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    public  boolean isActivityRunning(Context context, Class<?> activityClass) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
    
            for (ActivityManager.RunningTaskInfo task : tasks) {
                if (activityClass.getName().equals(task.baseActivity.getClassName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return false;
    }
}
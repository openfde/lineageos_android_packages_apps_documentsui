package com.android.documentsui;

import java.io.File;
import java.net.URI;

import com.android.documentsui.base.Providers;
import com.android.documentsui.clipping.DocumentClipper;
import com.android.documentsui.files.FilesActivity;
import com.android.documentsui.provider.FileUtils;
import com.android.documentsui.util.SPUtils;
import com.android.documentsui.util.NetUtils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.util.Log;
import android.provider.DocumentsContract;
import com.android.documentsui.provider.FileUtils;
import com.android.documentsui.ui.RenameDialogActivity;
import android.content.ComponentName;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.os.Handler;
import android.os.FileObserver;
import android.app.AlertDialog;
import android.content.DialogInterface;


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
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String path = "content://"+Providers.AUTHORITY_STORAGE+"/document/"+Providers.ROOT_ID_DESKTOP+"%2f"+params;
                Uri uri = Uri.parse(path);
                String mimeType = FileUtils.getMimeType(new File(Providers.PATH_ID_DESKTOP+params));
                if(mimeType == null ){
                    intent.setDataAndType(uri, "application/*");
                }else if(mimeType.contains("image")){
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
                String type = arrParams[2].trim();

                if("open".equals(type)){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            NetUtils.gotoLinuxApp(name,exec);
                        }
                    }).start();
                }else if("vnc".equals(type)){
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("com.iiordanov.bVNC", "com.iiordanov.bVNC.LinuxAppActivity");
                    intent.setComponent(componentName);
                    intent.putExtra("fromOther", "Launcher");
                    intent.putExtra("vnc_activity_name", name);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }else{
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("com.fde.x11", "com.fde.x11.AppListActivity");
                    intent.setComponent(componentName);
                    // intent.putExtra("Path", "mate-terminal");
                    // intent.putExtra("App", "MATE Terminal");
                    intent.putExtra("App", name);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent); 
                }
            }else if(FileUtils.DELETE_FILE.equals(method)){
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
                FileUtils.createAllAndroidIconToLinux(context);
            }else if(FileUtils.OP_CREATE_LINUX_ICON.equals(method)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        NetUtils.getLinuxApp();
                    }
                }).start();
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
}
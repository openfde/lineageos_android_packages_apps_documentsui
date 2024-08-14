package com.android.documentsui;

import java.io.File;
import java.net.URI;

import com.android.documentsui.base.Providers;
import com.android.documentsui.clipping.DocumentClipper;
import com.android.documentsui.files.FilesActivity;
import com.android.documentsui.provider.FileUtils;
import com.android.documentsui.util.SPUtils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.provider.DocumentsContract;
import android.content.ComponentName;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.os.Handler;

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
                  Intent intent = new Intent();
                  ComponentName componentName = new ComponentName( "com.android.documentsui", "com.android.documentsui.files.FilesActivity"  );
                  intent.setComponent(componentName);
                  intent.putExtra("getPath", FileUtils.PATH_ID_DESKTOP);
                  intent.putExtra("childPath",params);
                  int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_SINGLE_TOP;
                    flags |= Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    flags |= Intent.FLAG_ACTIVITY_NEW_TASK;
                    intent.setFlags(flags);
                  startActivity(intent);

                //   Intent intent = new Intent(ACTION_UPDATE_FILE);
                //   intent.putExtra("EXTRA_DATA", params);
                //   intent.putExtra("EXTRA_TYPE", 5);
                //   context.sendBroadcast(intent);
            }else if(FileUtils.DELETE_FILE.equals(method)){
                FileUtils.deleteFiles(params);
            }else if(FileUtils.NEW_FILE.equals(method)){
                FileUtils.newFile();
            }else if(FileUtils.NEW_DIR.equals(method)){
                FileUtils.newDir();
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
                    String[] arrFileName = params.split("###");
                    File file = new File(FileUtils.PATH_ID_DESKTOP+arrFileName[0]);
                    file.renameTo(new File(FileUtils.PATH_ID_DESKTOP+arrFileName[1]));
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
                // gotoClientApp("CUT_FILE.....");
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
                }

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

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }
}
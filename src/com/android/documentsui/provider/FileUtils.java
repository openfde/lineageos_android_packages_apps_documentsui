package com.android.documentsui.provider;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.graphics.PixelFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.android.documentsui.R;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.VectorDrawable;

public class FileUtils {

    public static final String PATH_ID_DESKTOP = "/mnt/sdcard/Desktop/";

    public static final String OPEN_DIR = "OPEN_DIR";

    public static final String OPEN_FILE = "OPEN_FILE";

    public static final String DELETE_DIR = "DELETE_DIR";

    public static final String OPEN_LINUX_APP = "OPEN_LINUX_APP";

    public static final String CLICK_BLANK = "CLICK_BLANK";

    public static final String DELETE_FILE = "DELETE_FILE";

    public static final String NEW_DIR = "NEW_DIR";

    public static final String NEW_FILE = "NEW_FILE";

    public static final String COPY_DIR = "COPY_DIR";

    public static final String COPY_FILE = "COPY_FILE";

    public static final String CUT_DIR = "CUT_DIR";

    public static final String CUT_FILE = "CUT_FILE";

    public static final String PASTE_DIR = "PASTE_DIR";

    public static final String PASTE_FILE = "PASTE_FILE";

    public static final String RENAME_DIR = "RENAME_DIR";

    public static final String RENAME_FILE = "RENAME_FILE";

    public static final String DIR_INFO = "DIR_INFO";

    public static final String FILE_INFO = "FILE_INFO";

    public static final String FILE_OPERATE = "FILE_OPERATE";

    public static final String FILE_LIST = "FILE_LIST";

    public static final String OP_COPY = "OP_COPY";

    public static final String OP_CUT = "OP_CUT";

    public static final String OP_PASTE = "OP_PASTE";

    public static final String OP_INIT = "OP_INIT";

    public static final String OP_CREATE_LINUX_ICON = "OP_CREATE_LINUX_ICON";

    public static final String OP_CREATE_ANDROID_ICON = "OP_CREATE_ANDROID_ICON";

    public static final String FILE_DESKTOP_NAME = "FILE_DESKTOP_NAME";

    protected static final String TAG = "FileUtils";


   /**
     * 默认root需要查询的项
     */
    public final static String[] DEFAULT_ROOT_PROJECTION = new String[] { Root.COLUMN_ROOT_ID, Root.COLUMN_SUMMARY,
            Root.COLUMN_FLAGS, Root.COLUMN_TITLE, Root.COLUMN_DOCUMENT_ID, Root.COLUMN_ICON,
            Root.COLUMN_AVAILABLE_BYTES };
    /**
     * 默认Document需要查询的项
     */
    public final static String[] DEFAULT_DOCUMENT_PROJECTION = new String[] { Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME, Document.COLUMN_FLAGS, Document.COLUMN_MIME_TYPE, Document.COLUMN_SIZE,
            Document.COLUMN_LAST_MODIFIED };

    public static final String SUPPORTED_QUERY_ARGS = joinNewline(
            DocumentsContract.QUERY_ARG_DISPLAY_NAME,
            DocumentsContract.QUERY_ARG_FILE_SIZE_OVER,
            DocumentsContract.QUERY_ARG_LAST_MODIFIED_AFTER,
            DocumentsContract.QUERY_ARG_MIME_TYPES);

    public static void createDesktopDir(){
        File file = new File(PATH_ID_DESKTOP);
        if(!file.exists()){
            file.mkdirs();
        }
    }    
    
    public static void createDesktopDir(String path){
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
}    

    public static String joinNewline(String... args) {
        return TextUtils.join("\n", args);
    }

    public static String[] resolveRootProjection(String[] projection) {
        return projection != null ? projection : FileUtils.DEFAULT_ROOT_PROJECTION;
    }

    public static  File createFile(String documentId, String mimeType, String displayName)
            throws FileNotFoundException {
        File file = new File(documentId, displayName);
        if (file.exists()) {
            file = new File(documentId,  getUniqueFileName(documentId,displayName));
        }  
       
        if (mimeType.equals(Document.MIME_TYPE_DIR)) {
            if (!file.mkdirs()) {
                throw new FileNotFoundException("Failed to create directory(s): " + file);
            }
        } else {
            boolean created = false;
            try {
                created = file.createNewFile();
            } catch (IOException e) {
            }
            if (!created) {
                throw new FileNotFoundException("createNewFile operation failed for: " + file);
            }
        }
        return file;
    }

    public static String getUniqueFileName(String documentId,String fileName ) {
        String name = fileName ;
        String extension = "" ;
        if(fileName.contains(".") && fileName.length() > 0){
             name = fileName.substring(0, fileName.lastIndexOf('.'));
             extension = fileName.substring(fileName.lastIndexOf('.'));
        }else{

        }
      
        String newName = name;
        int count = 0;
        File newFile;
        do {
            count++;
            newName = name + "_" + count + extension;
            newFile = new File(documentId,newName);
        } while (newFile.exists());

        return newName;
    }

     public static String copyFile(String sourceDocumentId, String targetParentDocumentId)
            throws FileNotFoundException {

        File parent = new File(targetParentDocumentId);
        File oldFile = new File(sourceDocumentId);
        File newFile = new File(parent.getPath(), oldFile.getName());

        try {
            // Create the new File to copy into
            boolean wasNewFileCreated = false;
            if (newFile.createNewFile()) {
                if (newFile.setWritable(true) && newFile.setReadable(true)) {
                    wasNewFileCreated = true;
                }
            }

            if (!wasNewFileCreated) {
                throw new FileNotFoundException("Failed to copy document " + sourceDocumentId +
                        ". Could not create new file.");
            }

            // Copy the bytes into the new file
            try (InputStream inStream = new FileInputStream(oldFile)) {
                try (OutputStream outStream = new FileOutputStream(newFile)) {
                    // Transfer bytes from in to out
                    byte[] buf = new byte[4096]; // ideal range for network: 2-8k, disk: 8-64k
                    int len;
                    while ((len = inStream.read(buf)) > 0) {
                        outStream.write(buf, 0, len);
                    }
                    outStream.flush();
                    outStream.close();
                    inStream.close();
                }
            }
        } catch (IOException e) {
            throw new FileNotFoundException("Failed to copy document: " + sourceDocumentId +
                    ". " + e.getMessage());
        }
        return newFile.getAbsolutePath();
    }


    public static  String renameFile(String documentId, String displayName) throws FileNotFoundException {
        Log.i("bella", "renameFile   documentId: " + documentId + " , displayName: "+displayName);
        if (displayName == null) {
            throw new FileNotFoundException("Failed to rename document, new name is null");
        }

        // Create the destination file in the same directory as the source file
        File sourceFile = new File(documentId);
        File sourceParentFile = sourceFile.getParentFile();
        if (sourceParentFile == null) {
            throw new FileNotFoundException("Failed to rename document. File has no parent.");
        }
        File destFile = new File(sourceParentFile.getPath(), displayName);

        // Try to do the rename
        try {
            boolean renameSucceeded = sourceFile.renameTo(destFile);
            if (!renameSucceeded) {
                throw new FileNotFoundException("Failed to rename document. Renamed failed.");
            }
        } catch (Exception e) {
            throw new FileNotFoundException("Failed to rename document. Error: " + e.getMessage());
        }

        return destFile.getAbsolutePath();
    }

    public static void call(String method, String arg, Bundle extras,Context context){
        if(method.contains("createDocument") || method.contains("renameDocument") ){
            Intent intent = new Intent();
            intent.setAction("android.intent.action.FDE_PROVIDER"); 
            context.sendBroadcast(intent);
        }
    }

    public static void deleteFiles(String path){
      try {
        File file  = new File(path);
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteFiles(child.getAbsolutePath());
            }
        }
        file.delete();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    public static void includeFile(final MatrixCursor result, final File file) throws FileNotFoundException {
        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, file.getAbsolutePath());
        row.add(Document.COLUMN_DISPLAY_NAME, file.getName());
        String mimeType = getDocumentType(file.getAbsolutePath());
        row.add(Document.COLUMN_MIME_TYPE, mimeType);
        int flags = file.canWrite()
                ? Document.FLAG_SUPPORTS_DELETE | Document.FLAG_SUPPORTS_WRITE | Document.FLAG_SUPPORTS_RENAME
                        | (mimeType.equals(Document.MIME_TYPE_DIR) ? Document.FLAG_DIR_SUPPORTS_CREATE : 0)
                : 0;
        if (mimeType.startsWith("image/"))
            flags |= Document.FLAG_SUPPORTS_THUMBNAIL;
        row.add(Document.COLUMN_FLAGS, flags);
        row.add(Document.COLUMN_SIZE, file.length());
        row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified());
    }

    public static void includeVolumesFile(final MatrixCursor result, final File file) throws FileNotFoundException {
        String fileName = file.getName();
        String uuid = parseFile(fileName);
        if (uuid != null) {
            fileName = uuid;
        }
        // Log.i("bella", "includeVolumesFile uuid " + uuid + ",fileName " + fileName);
        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, file.getAbsolutePath());
        row.add(Document.COLUMN_DISPLAY_NAME, fileName);
        String mimeType = getDocumentType(file.getAbsolutePath());
        row.add(Document.COLUMN_MIME_TYPE, mimeType);
        int flags = file.canWrite()
                ? Document.FLAG_SUPPORTS_DELETE | Document.FLAG_SUPPORTS_WRITE | Document.FLAG_SUPPORTS_RENAME
                        | (mimeType.equals(Document.MIME_TYPE_DIR) ? Document.FLAG_DIR_SUPPORTS_CREATE : 0)
                : 0;
        if (mimeType.startsWith("image/"))
            flags |= Document.FLAG_SUPPORTS_THUMBNAIL;
        row.add(Document.COLUMN_FLAGS, flags);
        row.add(Document.COLUMN_SIZE, file.length());
        row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified());
    }

    public static boolean isMissingPermission(@Nullable Context context) {
        if (context == null) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 通知root的Uri失去权限, 禁止相关操作
            context.getContentResolver().notifyChange(
                    DocumentsContract.buildRootsUri(LinuxRootProvider.AUTHORITY), null);
            return true;
        }
        return false;
    }

    public static String getDocumentType(final String documentId) throws FileNotFoundException {
        File file = new File(documentId);
        if (file.isDirectory())
            return Document.MIME_TYPE_DIR;
        final int lastDot = file.getName().lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = file.getName().substring(lastDot + 1);
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }
        return "vnd.android.document/directory";
    }

    public static  String readFile() {
        String filePath = "/volumes/.fde_path_key";
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return content;
    }

    public static String getLinuxUUID(){
        String result = null;
        try {
            String jsonString = readFile();
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String uuid = jsonObject.getString("UUID");
                String path = jsonObject.getString("Path");
                if ("/".equals(path)) {
                    result = uuid;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int findNthSlashIndex(String str, int n) {
        int index = -1;
        int count = 0;

        // 从头开始查找斜杠
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '/') {
                count++;
                if (count == n) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public static String findFileDir(String findPath){
        String rootPath = "/volumes"+"/"+getLinuxUUID()+getLinuxHomeDir();
        final File parent = new File(rootPath);
        String resPath = "" ;
        for (File file : parent.listFiles()) {
            if(file.getPath().contains(findPath)){
                resPath = findPath;
                break;
            }
        }
        return resPath;
    }

    public static String getLinuxHomeDir(){
       try {
            String propertyValue = SystemProperties.get("waydroid.host_data_path");
            int len = findNthSlashIndex(propertyValue,3);
            String subPath = propertyValue.substring(0,len);
            Log.i("bella","propertyValue "+propertyValue +",subPath "+subPath);
            return subPath;
       } catch (Exception e) {
         e.printStackTrace();
       }
       return "/";
    }

    public static  String parseFile(String filePath) {
        String result = null;
        try {
            String jsonString = readFile();
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String uuid = jsonObject.getString("UUID");
                String path = jsonObject.getString("Path");
                if (filePath.equals(uuid)) {
                    result = path;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * get file type
     * @param filePath
     * @return
     */
    public static String getFileTyle (String filePath){
        try {
            File file = new File(filePath);
            String fileName = file.getName();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                String extension = fileName.substring(dotIndex + 1);
                return  extension;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static  Bitmap getThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        return ThumbnailUtils.extractThumbnail(bitmap, width, height);
    }

    public static String getMimeType(File file) {
        String mimeType = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return mimeType;
    }


      public static String readTextFromUri(Uri uri,Context context) {
        StringBuilder text = new StringBuilder();
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                text.append(line).append("\n");
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    public static String readTextFromPath(String docPath) {
        try {
            File file = new File(docPath);
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            fis.close();
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static void writeTextToUri(Uri uri, String text,Context context) {
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
             BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)) {
            bufferedWriter.write(text);
            bufferedWriter.close();
            outputStreamWriter.close();
            outputStream.close();
            Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public static void writeTextToPath(String docPath, String fileContents,Context context) {
        File file = new File(docPath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(fileContents.getBytes());
            Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void clearFileContent(Uri uri,int contentBytesLen,Context context) {
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                // Write an empty byte array to clear the content
                outputStream.write(new byte[contentBytesLen]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String newDir() {     
        String documentId = PATH_ID_DESKTOP; 
        File folder = new File(documentId);   
        String newDirName = "NewDir"; 
        if (!folder.exists()) {
            boolean result = folder.mkdirs();
            if (result) {
                Log.i("bella", "Folder created: " + folder.getAbsolutePath());
            } else {
                Log.e("bella", "Failed to create folder");
            }
        } else {
            Log.i("bella", "Folder already exists");
            newDirName =  getUniqueFileName(documentId,newDirName);
        }
        folder = new File(documentId,newDirName);
        folder.mkdirs();
        return newDirName;
    }

public static void initFolder(){

}    

public static String newFile() {      
    try{
        String documentId = PATH_ID_DESKTOP;
        File folder = new File(documentId);  
        String newDocName = "NewFile.txt";  
        if (!folder.exists()) {
            boolean result = folder.mkdirs();
            if (result) {
                Log.i("bella", "Folder created: " + folder.getAbsolutePath());
            } else {
                Log.e("bella", "Failed to create folder");
            }
        } else {
            Log.i("bella", "file already exists");
            newDocName =  getUniqueFileName(documentId,newDocName);
        }
        folder = new File(documentId,newDocName);
        folder.createNewFile();
        folder.setExecutable(true);
        return newDocName;
    }catch(Exception e){
        e.printStackTrace();
    }
    return null;
}


public static void copyFileToClipboard(Context context, Uri uri) {
    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newUri(context.getContentResolver(), "File", uri);
    if (clip == null) {
        Log.i("bella", "copyFileToClipboard........ clip is null ");
    }
    clipboard.setPrimaryClip(clip);
    Log.i("bella", "copyFileToClipboard  File copied to clipboard");
}

public static Uri pasteFileFromClipboard(Context context) {
    try {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = clipboard.getPrimaryClip();
        if (clip == null) {
            Log.i("bella", "pasteFileFromClipboard........ clip is null ");
        }
        ClipData.Item item = clip.getItemAt(0);
        return item.getUri();

    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}

public static void cleanClipboard(Context context) {
    try {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(null); 
            clipboard.clearPrimaryClip();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

public static void copyFolder(String oldPath, String newPath) {
 
    try {
        File newDir = new File(newPath);
        if (!newDir.exists()) newDir.mkdirs();  
        File oldDir = new File(oldPath);
        String[] file = oldDir.list();
        File temp = null;
        for (int i = 0; i < file.length; i++) {
            if (oldPath.endsWith(File.separator)) {
                temp = new File(oldPath + file[i]);
            } else {
                temp = new File(oldPath + File.separator + file[i]);
            }

            if (temp.isFile()) {
                FileInputStream input = new FileInputStream(temp);
                FileOutputStream output = new FileOutputStream(newPath + "/" +
                        (temp.getName()).toString());
                byte[] b = new byte[1024];
                int len;
                while ((len = input.read(b)) != -1) {
                    output.write(b, 0, len);
                }
                output.flush();
                output.close();
                input.close();
            }
            if (temp.isDirectory()) { 
                copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

}


public static void copyUriToFile(Context context, Uri uri, File destinationFile) {
    try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
         FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

public static boolean deleteFileWithUri(Context context, Uri uri) {
    ContentResolver contentResolver = context.getContentResolver();
    try {
        // Use DocumentsContract for document Uris
        if (DocumentsContract.isDocumentUri(context, uri)) {
            return DocumentsContract.deleteDocument(contentResolver, uri);
        }
        // Handle other Uris (like MediaStore)
        int rowsDeleted = contentResolver.delete(uri, null, null);
        return rowsDeleted > 0;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}


public static String extractFileName(String path) {
    if (path == null || path.isEmpty()) {
        return null;
    }
    int lastSlashIndex = path.lastIndexOf('/');
    if (lastSlashIndex == -1) {
        return path;
    }
    return path.substring(lastSlashIndex + 1);
}


public static String getDesktopFiles(){
  try {
    String documentId = FileUtils.PATH_ID_DESKTOP; 
    File parent = new File(documentId);
    File[] files = parent.listFiles();
    return Arrays.stream(files)
                .map(File::getName) 
                .collect(Collectors.joining("###"));
  } catch (Exception e) {
    e.printStackTrace();
  }
  return null ;
}

public static void drawableToPng(Context context ,Drawable drawable, String filePath) {
        Bitmap bitmapT = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),  drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        
        Bitmap bitmap;
        if(drawable instanceof  AdaptiveIconDrawable){
            AdaptiveIconDrawable adaptiveIconDrawable = (AdaptiveIconDrawable)drawable;
            bitmapT = adaptiveIconToBitmap(adaptiveIconDrawable);
            bitmapT  = scaleBitmap(bitmapT,42,42);
            Bitmap b2 = vectorToBitmap(context, R.mipmap.bg_android);
            b2  = scaleBitmap(b2,80,80);
            bitmap = overlayBitmaps(b2,bitmapT);  
        }else {
            Log.i("bella","createAllAndroidIconToLinux drawable is not  AdaptiveIconDrawable....  ");
            if(drawable instanceof BitmapDrawable){
                Log.i("bella","createAllAndroidIconToLinux drawable 1111");
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                bitmapT = bitmapDrawable.getBitmap();
                bitmapT  = scaleBitmap(bitmapT,42,42);
                Bitmap b2 = vectorToBitmap(context, R.mipmap.bg_android);
                b2  = scaleBitmap(b2,80,80);
                bitmap = overlayBitmaps(b2,bitmapT);  
            // }else if(drawable instanceof VectorDrawable){
            // }else if(drawable instanceof StateListDrawable){
            // }else if(drawable instanceof LayerDrawable){
            // }else if(drawable instanceof LevelListDrawable){
            // }else if(drawable instanceof ShapeDrawable){
            // }else if(drawable instanceof GradientDrawable){
            // }else if(drawable instanceof InsetDrawable){
            // }else if(drawable instanceof RippleDrawable){
            // }else if(drawable instanceof ColorDrawable){
            // }else if(drawable instanceof AdaptiveIconDrawable){
            }else{
                Log.i("bella","createAllAndroidIconToLinux drawable cccc");
                bitmap = bitmapT;
            }
            
        }
                
        // bitmapT  = scaleBitmap(bitmapT,42,42);
        // Bitmap b2 = vectorToBitmap(context, R.mipmap.bg_android);
        // b2  = scaleBitmap(b2,80,80);
        // Bitmap bitmap = overlayBitmaps(b2,bitmapT);  
        
        
        Canvas canvas = new Canvas(bitmapT);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        // 保存Bitmap到PNG文件
        File file = new File(filePath);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

}

public static Bitmap vectorToBitmap(Context context, int drawableId) {
    Drawable drawable = context.getResources().getDrawable(drawableId, null);
    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
}

public static  Bitmap overlayBitmaps(Bitmap bitmap1, Bitmap bitmap2) {
    // 创建一个与第一个 Bitmap 相同大小的空白 Bitmap
    Bitmap overlayBitmap = Bitmap.createBitmap(bitmap1.getWidth(), bitmap1.getHeight(), bitmap1.getConfig());
    // 创建 Canvas，将第一个 Bitmap 作为底图
    Canvas canvas = new Canvas(overlayBitmap);
    canvas.drawBitmap(bitmap1, 0, 0, null);  // 将 bitmap1 绘制到 canvas 上
    // 将第二个 Bitmap 绘制到 Canvas 上，叠加在第一个 Bitmap 上
    canvas.drawBitmap(bitmap2, (bitmap1.getWidth()-bitmap2.getWidth())/2, (bitmap1.getHeight()-bitmap2.getHeight())/2, null);  // 将 bitmap2 绘制到 canvas 上
    // 将叠加后的 Bitmap 设置到 ImageView
    return overlayBitmap ;
}

public static  Bitmap scaleBitmap(Bitmap originalBitmap, int newWidth, int newHeight) {
    return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
}

private static   List<ApplicationInfo>   getAllApp(Context context) {
    LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
    UserManager userManager = (UserManager)context.getSystemService(Context.USER_SERVICE);
    List<UserHandle> userHandles = userManager.getUserProfiles();
    List<LauncherActivityInfo> list = new ArrayList<>();
    for (UserHandle userHandle : userHandles) {
        list.addAll(launcherApps.getActivityList(null, userHandle));
    }

    PackageManager packageManager = context.getPackageManager();
    List<ApplicationInfo>  listApps = new ArrayList<>();
    Log.i("bella","getAllApp list  size:   " + list.size());
    for (LauncherActivityInfo li : list){
        String appName = packageManager.getApplicationLabel(li.getApplicationInfo()).toString();
        Drawable icon = packageManager.getApplicationIcon(li.getApplicationInfo());
        String packageName = li.getApplicationInfo().packageName ;
        // Log.i("bella","getAllApp list  li:  getName: " + li.getName() +"  ,appName: "+appName + ",packageName "+packageName + " ,name : "+li.getApplicationInfo().name);
        listApps.add(li.getApplicationInfo());
    }
    return listApps;
}

public static String getMD5(String input) {
    try {
        // 创建一个 MessageDigest 实例，指定使用 MD5 算法
        MessageDigest digest = MessageDigest.getInstance("MD5");

        // 计算 MD5 值，得到一个字节数组
        byte[] hashBytes = digest.digest(input.getBytes());

        // 转换字节数组为 16 进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return "a"+hexString.toString();
    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
    }
    return null;
    }

 public static void  createAllAndroidIconToLinux(Context context,String packageName){
    PackageManager packageManager = context.getPackageManager();
    String rootPath = "/volumes"+"/"+getLinuxUUID()+getLinuxHomeDir()+ "/.local/share/icons/"; 

   
    if("".equals(packageName)){
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(0);
        apps.addAll(getAllApp(context));
        for (ApplicationInfo appInfo : apps) {
            try {
                // if(appInfo.name !=null){
                    Drawable icon = packageManager.getApplicationIcon(appInfo);
                    String appName = packageManager.getApplicationLabel(appInfo).toString();
                    String md5 = getMD5(appInfo.packageName);
    
                    String path = rootPath+md5+".png";
                    Log.i("bella","createAllAndroidIconToLinux md5 : "+md5+",path: "+path +",packName: "+appInfo.packageName);
                    File file = new File(path);
                    if(!file.exists() && !path.contains(" ") ){
                        drawableToPng(context,icon,path);
                    }    
                // }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }else{
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            Drawable icon = packageManager.getApplicationIcon(appInfo);
            String appName = packageManager.getApplicationLabel(appInfo).toString();
            String md5 = getMD5(appInfo.packageName);

            String path = rootPath+md5+".png";
            Log.i("bella","createAllAndroidIconToLinux md5 : "+md5+",path: "+path +",packageName: "+packageName +",appName: "+appName);
            File file = new File(path);
            if(!file.exists() && !path.contains(" ") ){
                drawableToPng(context,icon,path);
            }    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    

    // Log.i("bella","createAllAndroidIconToLinux rootPath : "+rootPath + ",apps "+apps.size());

    
 }


    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true; // app installed
        } catch (PackageManager.NameNotFoundException e) {
            return false; // app not install
        }
    }

    public static void setSystemProperty(String key, String value) {
        try {
            Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method setMethod = systemPropertiesClass.getDeclaredMethod("set", String.class, String.class);
            setMethod.invoke(null, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap addTextWatermark(Bitmap source, String watermarkText) {
        int width = source.getWidth();
        int height = source.getHeight();

        // 创建一个新的Bitmap，大小与原始Bitmap相同
        Bitmap watermarkBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // 创建画布并将原图绘制到画布上
        Canvas canvas = new Canvas(watermarkBitmap);
        canvas.drawBitmap(source, 0, 0, null);

        // 设置水印文本样式
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);  // 设置水印文本颜色
        //paint.setAlpha(100);  // 设置透明度，100代表半透明
        paint.setTextSize(14f);  // 设置文本大小
        // paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setAntiAlias(true);  // 设置抗锯齿

        // 获取水印文本的边界框，用于计算文本位置
        Rect textBounds = new Rect();
        paint.getTextBounds(watermarkText, 0, watermarkText.length(), textBounds);
        int textWidth = textBounds.width();
        int textHeight = textBounds.height();

        // 设置文本的位置（右下角）
        float x = (width - textWidth)/2;//width - textWidth - 20f;  // 距离右侧20像素
        float y = (height - textHeight)/2;//height - textHeight - 20f;  // 距离底部20像素

        // 在Bitmap上绘制文本水印
        canvas.drawText(watermarkText, x, y, paint);

        return watermarkBitmap;
    }

     // 在Bitmap上添加图片水印
     private static Bitmap addImageWatermark(Bitmap source, int watermarkResId, Context context) {
        int width = source.getWidth();
        int height = source.getHeight();

        // 创建一个新的Bitmap，大小与原始Bitmap相同
        Bitmap watermarkBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // 创建画布并将原图绘制到画布上
        Canvas canvas = new Canvas(watermarkBitmap);
        canvas.drawBitmap(source, 0, 0, null);

        // 获取水印图片
        Bitmap watermarkImage = BitmapFactory.decodeResource(context.getResources(), watermarkResId);

        // 设置水印图片的大小（可选）
        int watermarkWidth = width / 4;  // 水印宽度为原图的1/4
        int watermarkHeight = watermarkImage.getHeight() * watermarkWidth / watermarkImage.getWidth();  // 保持宽高比

        // 设置水印图片的位置（右下角）
        float left = width - watermarkWidth - 20f;  // 距离右侧20像素
        float top = height - watermarkHeight - 20f;  // 距离底部20像素

        // 在Bitmap上绘制水印图片
        canvas.drawBitmap(Bitmap.createScaledBitmap(watermarkImage, watermarkWidth, watermarkHeight, true), left, top, null);

        return watermarkBitmap;
    }
    

    public static void createLinuxDesktopFile(String title ,String packageName,int itemType ){
        createDesktopDir(PATH_ID_DESKTOP);
            try{    
                if(title.contains(".desktop") || itemType == 8 || itemType == 9){
                    return ;
                }
    
                String documentId =  "/volumes"+"/"+getLinuxUUID()+getLinuxHomeDir()+"/桌面/";  
                File ff = new File(documentId);
                if(!ff.exists()){
                    documentId =  "/volumes"+"/"+getLinuxUUID()+getLinuxHomeDir()+"/Desktop/";  
                }

                String md5 = getMD5(packageName);
                String pathDesktop = documentId+""+ md5+"_fde.desktop";
                File file = new File(pathDesktop);
                if(file.exists()){
                    Log.i(TAG,"bella...pathDesktop is exists :  "+pathDesktop);
                    return ;
                }
                Path desktopFilePath = Paths.get(pathDesktop);

                String picPath = "/volumes"+"/"+getLinuxUUID()+getLinuxHomeDir()+"/.local/share/icons/"+md5+".png" ;
                File filePic = new File(picPath);
                String homeDir = getLinuxHomeDir();
                String linuxPath = homeDir+"/.local/share/icons/"+md5+".png";
                Log.i(TAG,"bella...homeDir :  "+homeDir + ",linuxPath: "+linuxPath);
                File linuxPic = new File(linuxPath);
                if(!linuxPic.exists()){
                    Log.i(TAG,"bella...insert.............md5: "+md5 +  ", linuxPath "+linuxPath + ",packageName:  "+packageName);
                }else{
                    //if pic exists ,return 
                }    
    
                List<String> lines = List.of(
                    "[Desktop Entry]",
                    "Type=Application",
                    "Name="+title,
                    "Name[zh_CN]="+title,
                    "Categories="+itemType,
                    "Exec=fde_launch "+packageName,
                    "Icon="+linuxPic
                );
         
                // 写入.desktop文件
                Files.write(desktopFilePath, lines, StandardOpenOption.CREATE);
                file.setExecutable(true);
 
            }catch(Exception e){
                e.printStackTrace();
            }
    }

    public static Bitmap adaptiveIconToBitmap(AdaptiveIconDrawable adaptiveIconDrawable) {
        int width = adaptiveIconDrawable.getIntrinsicWidth();
        int height = adaptiveIconDrawable.getIntrinsicHeight();
    
        // 创建一个与 AdaptiveIcon 大小相同的 Bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
    
        // 获取前景和背景
        Drawable background = adaptiveIconDrawable.getBackground();
        Drawable foreground = adaptiveIconDrawable.getForeground();
    
        // 绘制背景和前景
        if (background != null) {
            background.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            background.draw(canvas);
        }else {
            Log.i("bella","createAllAndroidIconToLinux background is null ...  ");
        }
        if (foreground != null) {
            foreground.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            foreground.draw(canvas);
        }else {
            Log.i("bella","createAllAndroidIconToLinux  background is null ....  ");
        }
    
        return bitmap;
    }

}

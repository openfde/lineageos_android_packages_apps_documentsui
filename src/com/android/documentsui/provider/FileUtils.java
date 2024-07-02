package com.android.documentsui.provider;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.DocumentsProvider;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.SystemProperties;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.documentsui.R;
import android.os.Bundle;
import android.content.Intent;
import android.provider.MediaStore;
import android.content.ContentResolver;
import android.net.Uri;
public class FileUtils {
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

    private static String getUniqueFileName(String documentId,String fileName ) {
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
                    DocumentsContract.buildRootsUri(MusicProvider.AUTHORITY), null);
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
        return "application/octet-stream";
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


    
}

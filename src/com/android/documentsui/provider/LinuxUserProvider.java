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

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.documentsui.R;
import android.os.Bundle;
import android.content.Intent;

public class LinuxUserProvider extends DocumentsProvider {

    public static final String AUTHORITY = "com.android.documentsui.fusionvolume";
    public static final String DOC_ID_ROOT = "linux";
    public static final String DIR_ID_ROOT = "/volumes";

    @Override
    public Cursor queryRoots(final String[] projection) throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(FileUtils.resolveRootProjection(projection));
        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Root.COLUMN_ROOT_ID, DOC_ID_ROOT);
        row.add(Root.COLUMN_FLAGS, Root.FLAG_LOCAL_ONLY | Root.FLAG_SUPPORTS_RECENTS
                | Root.FLAG_SUPPORTS_CREATE | Root.FLAG_SUPPORTS_SEARCH);
        row.add(Root.COLUMN_ICON, R.mipmap.icon_home);
        row.add(Root.COLUMN_TITLE, getContext().getString(R.string.fde_user_dir));
        row.add(Root.COLUMN_DOCUMENT_ID, DIR_ID_ROOT+"/"+FileUtils.getLinuxUUID()+ FileUtils.getLinuxHomeDir());
        row.add(Root.COLUMN_QUERY_ARGS, FileUtils.SUPPORTED_QUERY_ARGS);
        return result;
    }

    @Override
    public boolean isChildDocument(final String parentDocumentId, final String documentId) {
        return documentId.startsWith(parentDocumentId);
    }

    @Override
    public Cursor queryChildDocuments(final String parentDocumentId, final String[] projection,
            final String sortOrder) throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : FileUtils.DEFAULT_DOCUMENT_PROJECTION);
        final File parent = new File(parentDocumentId);
        for (File file : parent.listFiles()) {
            // 不显示隐藏的文件或文件夹
            if (parentDocumentId.contains("volumes")) {
                FileUtils.includeVolumesFile(result, file);
            } else if (!file.getName().startsWith(".")) {
                // 添加文件的名字, 类型, 大小等属性
                FileUtils.includeFile(result, file);
            }
        }
        return result;
    }

    @Override
    public Cursor queryDocument(final String documentId, final String[] projection) throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : FileUtils.DEFAULT_DOCUMENT_PROJECTION);
        FileUtils.includeFile(result, new File(documentId));
        return result;
    }



    @Override
    public String getDocumentType(final String documentId) throws FileNotFoundException {
        return FileUtils.getDocumentType(documentId);
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        Bundle result = super.call(method, arg, extras);
        FileUtils.call(method, arg, extras,getContext());
        return result;
    }


    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, @Nullable CancellationSignal signal)
            throws FileNotFoundException {
        return ParcelFileDescriptor.open(new File(documentId),
                ParcelFileDescriptor.MODE_READ_WRITE);
    }

    @Override
    public String renameDocument(String documentId, String displayName)
            throws FileNotFoundException {
        return FileUtils.renameFile(documentId, displayName);        
    }


    @Override
    public String copyDocument(String sourceDocumentId, String targetParentDocumentId)
            throws FileNotFoundException {
        return FileUtils.copyFile(sourceDocumentId, targetParentDocumentId);        
    }

    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        File file = new File(documentId);
        if (file.delete()) {
        } else {
            throw new FileNotFoundException("Failed to delete document with id " + documentId);
        }
    }

    @Override
    public String createDocument(
            String documentId, String mimeType, String displayName)
            throws FileNotFoundException {
        File file = FileUtils.createFile(documentId, mimeType, displayName);

        getContext().getContentResolver().notifyChange(
                DocumentsContract.buildDocumentUri(AUTHORITY, documentId),
                null, false);

        return file.getPath();
    }

    @Override
    public boolean onCreate() {
        Log.i("bella","onCreate........... ");
        return true; // 
    }

}

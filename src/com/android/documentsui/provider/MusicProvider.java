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
import com.android.documentsui.provider.StubProvider.StubDocument;

public class MusicProvider extends DocumentsProvider {
    public static final String AUTHORITY = "com.android.documentsui.music";
    public static final String DOC_ID_ROOT = "/";
    public static final String DIR_ID_ROOT = "/mnt/sdcard/Music";
    public static final String VOLUME_TITLE = "Music";
    public static final String FUSION_PREFIX = "fusion:";
    private static final String TAG = "MusicProvider";

    /**
     * 默认root需要查询的项
     */
    private final static String[] DEFAULT_ROOT_PROJECTION = new String[] { Root.COLUMN_ROOT_ID, Root.COLUMN_SUMMARY,
            Root.COLUMN_FLAGS, Root.COLUMN_TITLE, Root.COLUMN_DOCUMENT_ID, Root.COLUMN_ICON,
            Root.COLUMN_AVAILABLE_BYTES };
    /**
     * 默认Document需要查询的项
     */
    private final static String[] DEFAULT_DOCUMENT_PROJECTION = new String[] { Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME, Document.COLUMN_FLAGS, Document.COLUMN_MIME_TYPE, Document.COLUMN_SIZE,
            Document.COLUMN_LAST_MODIFIED };

    protected static final String SUPPORTED_QUERY_ARGS = joinNewline(
            DocumentsContract.QUERY_ARG_DISPLAY_NAME,
            DocumentsContract.QUERY_ARG_FILE_SIZE_OVER,
            DocumentsContract.QUERY_ARG_LAST_MODIFIED_AFTER,
            DocumentsContract.QUERY_ARG_MIME_TYPES);

    private static String joinNewline(String... args) {
        return TextUtils.join("\n", args);
    }

    /**
     * 进行读写权限检查
     */
    static boolean isMissingPermission(@Nullable Context context) {
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

    /*
     * 在此方法中组装一个cursor, 他的内容就是home与sd卡的路径信息,
     * 并将home与sd卡的信息存到数据库中
     */
    @Override
    public Cursor queryRoots(final String[] projection) throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(resolveRootProjection(projection));
        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Root.COLUMN_ROOT_ID, DOC_ID_ROOT);
        row.add(Root.COLUMN_FLAGS, Root.FLAG_LOCAL_ONLY | Root.FLAG_SUPPORTS_RECENTS
                | Root.FLAG_SUPPORTS_CREATE | Root.FLAG_SUPPORTS_SEARCH);
        row.add(Root.COLUMN_ICON, R.mipmap.icon_audio);
        row.add(Root.COLUMN_TITLE, getContext().getString(R.string.chip_title_audio));
        row.add(Root.COLUMN_DOCUMENT_ID, DIR_ID_ROOT);
        row.add(Root.COLUMN_QUERY_ARGS, SUPPORTED_QUERY_ARGS);
        return result;
    }

    private static String[] resolveRootProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_ROOT_PROJECTION;
    }

    @Override
    public boolean isChildDocument(final String parentDocumentId, final String documentId) {
        return documentId.startsWith(parentDocumentId);
    }

    @Override
    public Cursor queryChildDocuments(final String parentDocumentId, final String[] projection,
            final String sortOrder) throws FileNotFoundException {
        // 判断是否缺少权限
        // if (FusionDocumentProvider.isMissingPermission(getContext())) {
        // return null;
        // }
        // 创建一个查询cursor, 来设置需要查询的项, 如果"projection"为空, 那么使用默认项
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
        final File parent = new File(parentDocumentId);
        for (File file : parent.listFiles()) {
            // 不显示隐藏的文件或文件夹
            // if (parentDocumentId.contains("volumes")) {
            // includeVolumesFile(result, file);
            // } else if (!file.getName().startsWith(".")) {
            // 添加文件的名字, 类型, 大小等属性
            includeFile(result, file);
            // }
        }
        return result;
    }

    @Override
    public Cursor queryDocument(final String documentId, final String[] projection) throws FileNotFoundException {
        // if (MusicProvider.isMissingPermission(getContext())) {
        // return null;
        // }
        // 创建一个查询cursor, 来设置需要查询的项, 如果"projection"为空, 那么使用默认项
        Log.i("bella", "queryDocument " + documentId);
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
        includeFile(result, new File(documentId));
        return result;
    }

    private void includeFile(final MatrixCursor result, final File file) throws FileNotFoundException {
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

    @Override
    public String getDocumentType(final String documentId) throws FileNotFoundException {
        // if (MusicProvider.isMissingPermission(getContext())) {
        // return null;
        // }
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

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, @Nullable CancellationSignal signal)
            throws FileNotFoundException {
        return ParcelFileDescriptor.open(getFileForDocId(documentId),
                ParcelFileDescriptor.MODE_READ_WRITE);
    }

    @Override
    public String renameDocument(String documentId, String displayName)
            throws FileNotFoundException {

        Log.v(TAG, "renameDocument");
        if (displayName == null) {
            throw new FileNotFoundException("Failed to rename document, new name is null");
        }

        // Create the destination file in the same directory as the source file
        File sourceFile = getFileForDocId(documentId);
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
            Log.w(TAG, "Rename exception : " + e.getLocalizedMessage() + e.getCause());
            throw new FileNotFoundException("Failed to rename document. Error: " + e.getMessage());
        }

        return getDocIdForFile(destFile);
    }

    public String copyDocument(String sourceDocumentId, String sourceParentDocumentId,
            String targetParentDocumentId) throws FileNotFoundException {
        Log.v("bella", "copyDocument with document parent");
        if (!isChildDocument(sourceParentDocumentId, sourceDocumentId)) {
            throw new FileNotFoundException("Failed to copy document with id " +
                    sourceDocumentId + ". Parent is not: " + sourceParentDocumentId);
        }
        return copyDocument(sourceDocumentId, targetParentDocumentId);
    }

    @Override
    public String copyDocument(String sourceDocumentId, String targetParentDocumentId)
            throws FileNotFoundException {
        Log.v(TAG, "copyDocument");

        File parent = getFileForDocId(targetParentDocumentId);
        File oldFile = getFileForDocId(sourceDocumentId);
        File newFile = new File(parent.getPath(), oldFile.getName());

        Log.v(TAG, "bella targetParentDocumentId " + targetParentDocumentId);
        Log.v(TAG, "bella sourceDocumentId " + sourceDocumentId);
        Log.v(TAG, "bella targetParentDocumentId " + parent.getPath() + ", oldFile " + oldFile.getPath());
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
                }
            }
        } catch (IOException e) {
            throw new FileNotFoundException("Failed to copy document: " + sourceDocumentId +
                    ". " + e.getMessage());
        }
        return getDocIdForFile(newFile);
    }

    private String getDocIdForFile(File file) {
        String path = file.getAbsolutePath();

        // Start at first char of path under root
        final String rootPath = DIR_ID_ROOT; // mBaseDir.getPath();
        if (rootPath.equals(path)) {
            path = "";
        } else if (rootPath.endsWith("/")) {
            path = path.substring(rootPath.length());
        } else {
            path = path.substring(rootPath.length() + 1);
        }

        return "root" + ':' + path;
    }

    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        Log.v("bella", "deleteDocument " + documentId);
        File file = getFileForDocId(documentId);
        if (file.delete()) {
            Log.i("bella", "Deleted file with id " + documentId);
        } else {
            throw new FileNotFoundException("Failed to delete document with id " + documentId);
        }
    }

    @Override
    public String createDocument(
            String documentId, String mimeType, String displayName)
            throws FileNotFoundException {
        Log.i("bella", "createDocument " + documentId);
        File file = createFile(mimeType, displayName);
        if (file.exists()) {
            Log.i("bella", "createDocument 111111111111" + documentId);
        } else {
            Log.i("bella", "createDocument 00000000" + documentId);
        }
        getContext().getContentResolver().notifyChange(
                DocumentsContract.buildDocumentUri(AUTHORITY, documentId),
                null, false);

        return file.getPath();
    }

    private File createFile(String mimeType, String displayName)
            throws FileNotFoundException {

        final File file = new File(DIR_ID_ROOT, displayName);
        if (file.exists()) {
            throw new FileNotFoundException(
                    "Duplicate file names not supported for " + file);
        }

        if (mimeType.equals(Document.MIME_TYPE_DIR)) {
            if (!file.mkdirs()) {
                throw new FileNotFoundException("Failed to create directory(s): " + file);
            }
            Log.i("bella", "Created new directory: " + file);
        } else {
            boolean created = false;
            try {
                created = file.createNewFile();
            } catch (IOException e) {
                // We'll throw an FNF exception later :)
                Log.e("bella", "createNewFile operation failed for file: " + file, e);
            }
            if (!created) {
                throw new FileNotFoundException("createNewFile operation failed for: " + file);
            }
            Log.i("bella", "Created new file: " + file);
        }
        return file;
    }

    protected File getFileForDocId(String documentId)
            throws FileNotFoundException {
        // if (DOC_ID_ROOT.equals(documentId)) {
        return new File(documentId);
        // }
        // return new File(getAbsoluteFilePath(documentId));
    }

    public static String getAbsoluteFilePath(String rawDocumentId) {
        return rawDocumentId.substring(FUSION_PREFIX.length());
    }

    @Override
    public boolean onCreate() {
        return true; // 这里需要返回true
    }

}
package com.android.documentsui;

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
import java.io.FileNotFoundException;

public class FusionVolumeProvider extends DocumentsProvider {

    public static final String AUTHORITY = "com.android.documentsui.fusionvolume";
    public static final String DOC_ID_ROOT = "linux";
    public static final String DIR_ID_ROOT = "/volumes";
    public static final String VOLUME_TITLE = "Linux Volume";
    public static final String FUSION_PREFIX = "fusion:";
    /**
     * 默认root需要查询的项
     */
    private final static String[] DEFAULT_ROOT_PROJECTION = new String[]{Root.COLUMN_ROOT_ID, Root.COLUMN_SUMMARY,
            Root.COLUMN_FLAGS, Root.COLUMN_TITLE, Root.COLUMN_DOCUMENT_ID, Root.COLUMN_ICON,
            Root.COLUMN_AVAILABLE_BYTES};
    /**
     * 默认Document需要查询的项
     */
    private final static String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME, Document.COLUMN_FLAGS, Document.COLUMN_MIME_TYPE, Document.COLUMN_SIZE,
            Document.COLUMN_LAST_MODIFIED};

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
                    DocumentsContract.buildRootsUri(FusionVolumeProvider.AUTHORITY), null);
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
        row.add(Root.COLUMN_ICON, R.mipmap.ic_launcher);
        row.add(Root.COLUMN_TITLE, VOLUME_TITLE);
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
//        if (FusionDocumentProvider.isMissingPermission(getContext())) {
//            return null;
//        }
        // 创建一个查询cursor, 来设置需要查询的项, 如果"projection"为空, 那么使用默认项
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
        final File parent = new File(parentDocumentId);
        for (File file : parent.listFiles()) {
            // 不显示隐藏的文件或文件夹
            if (!file.getName().startsWith(".")) {
                // 添加文件的名字, 类型, 大小等属性
                includeFile(result, file);
            }
        }
        return result;
    }

    @Override
    public Cursor queryDocument(final String documentId, final String[] projection) throws FileNotFoundException {
//        if (FusionVolumeProvider.isMissingPermission(getContext())) {
//            return null;
//        }
        // 创建一个查询cursor, 来设置需要查询的项, 如果"projection"为空, 那么使用默认项
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
                | (mimeType.equals(Document.MIME_TYPE_DIR) ? Document.FLAG_DIR_SUPPORTS_CREATE : 0) : 0;
        if (mimeType.startsWith("image/"))
            flags |= Document.FLAG_SUPPORTS_THUMBNAIL;
        row.add(Document.COLUMN_FLAGS, flags);
        row.add(Document.COLUMN_SIZE, file.length());
        row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified());
    }

    @Override
    public String getDocumentType(final String documentId) throws FileNotFoundException {
//        if (FusionVolumeProvider.isMissingPermission(getContext())) {
//            return null;
//        }
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
    public ParcelFileDescriptor openDocument(String documentId, String mode, @Nullable CancellationSignal signal) throws FileNotFoundException {
        return ParcelFileDescriptor.open(getFileForDocId(documentId),
                ParcelFileDescriptor.MODE_READ_ONLY);
    }

    protected File getFileForDocId(String documentId)
            throws FileNotFoundException {
//        if (DOC_ID_ROOT.equals(documentId)) {
        return new File(DIR_ID_ROOT);
//        }
//        return new File(getAbsoluteFilePath(documentId));
    }

    public static String getAbsoluteFilePath(String rawDocumentId) {
        return rawDocumentId.substring(FUSION_PREFIX.length());
    }

    @Override
    public boolean onCreate() {
        return true;  // 这里需要返回true
    }
}

package com.android.documentsui.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtils {
    private static String DOC_INFO = "doc_info";

    public static String getDocInfo(Context context, String key) {
        SharedPreferences shared_doc_info = context.getSharedPreferences(DOC_INFO, context.MODE_PRIVATE);
        return shared_doc_info.getString(key, "");
    }

    public static void putDocInfo(Context context, String key, String values) {
        SharedPreferences shared_doc_info = context.getSharedPreferences(DOC_INFO, context.MODE_PRIVATE);
        shared_doc_info.edit().putString(key, values).commit();
    }

    public static int getIntDocInfo(Context context, String key) {
        SharedPreferences shared_doc_info = context.getSharedPreferences(DOC_INFO, context.MODE_PRIVATE);
        return shared_doc_info.getInt(key, 0);
    }

    public static void putIntDocInfo(Context context, String key, int values) {
        SharedPreferences shared_doc_info = context.getSharedPreferences(DOC_INFO, context.MODE_PRIVATE);
        shared_doc_info.edit().putInt(key, values).commit();
    }

    public static void cleanDocInfo(Context context) {
        SharedPreferences shared_doc_info = context.getSharedPreferences(DOC_INFO, context.MODE_PRIVATE);
        shared_doc_info.edit().clear().commit();
    }
}

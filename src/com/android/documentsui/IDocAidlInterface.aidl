// IDocAidlInterface.aidl
package com.android.documentsui;

import com.android.documentsui.IDataChangedCallback;

// Declare any non-default types here with import statements

interface IDocAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    String basicIpcMethon(String method,String params);

    oneway void register(IDataChangedCallback callback);

}
// IDataChangedCallback.aidl
package com.android.documentsui;

// Declare any non-default types here with import statements

interface IDataChangedCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    oneway void onCallback(String params);

    oneway void onCallbackString(String method,String params);
}
/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.documentsui.roots;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.File;

import android.os.Environment;
import android.net.Uri;
import com.android.documentsui.DocumentsApplication;

/**
 * Prime {@link ProvidersCache} when the system is booted.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // We already spun up our application object before getting here, which
        // kicked off a task to load roots, so this broadcast is finished once
        // that first pass is done.
        DocumentsApplication.getProvidersCache(context).setBootCompletedResult(goAsync());

        triggerSystemMediaScan(context);
    }

    public void triggerSystemMediaScan(Context context) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File externalDir = Environment.getExternalStorageDirectory();
        Uri contentUri = Uri.fromFile(externalDir);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }
}

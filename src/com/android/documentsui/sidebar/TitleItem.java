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

package com.android.documentsui.sidebar;

import static com.android.documentsui.base.SharedMinimal.DEBUG;

import android.util.Log;
import android.view.View;

import com.android.documentsui.R;
import com.android.documentsui.base.UserId;

/**
 * Dummy {@link Item} for dividers between different types of {@link Item}s.
 */
class TitleItem extends Item {
    private static final String TAG = "TitleItem";

    private static final String STRING_ID = "TitleItem";

    public TitleItem(int layoutId ,String title) {
        // Multiple spacer items can share the same string id as they're identical.
        super(layoutId, title /* title */, STRING_ID, UserId.UNSPECIFIED_USER);
    }

    @Override
    void bindView(View convertView) {
        // Nothing to bind
    }

    @Override
    boolean isRoot() {
        return false;
    }

    @Override
    void open() {
        if (DEBUG) {
            Log.d(TAG, "Ignoring click/hover on spacer item.");
        }
    }
}

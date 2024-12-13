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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.documentsui.R;
import com.android.documentsui.base.UserId;

/**
 * Dummy {@link Item} for dividers between different types of {@link Item}s.
 */
class DrawerTitleItem extends Item {
    private static final String TAG = "SpacerTitleItem";

    private static final String STRING_ID = "DrawerTitleItem";

    DrawerCallback drawerCallback;

    private  int openFlag ;

    public DrawerTitleItem(String title, int openFlag ,DrawerCallback drawerCallback) {
        // Multiple spacer items can share the same string id as they're identical.
        super(R.layout.item_drawer_header, title /* title */, STRING_ID, UserId.UNSPECIFIED_USER);
        this.drawerCallback = drawerCallback;
        this.openFlag = openFlag;
    }

    @Override
    void bindView(View convertView) {
        final TextView titleView = convertView.findViewById(android.R.id.title);
        titleView.setText(title);

        RelativeLayout rootView = convertView.findViewById(R.id.rootView);
        ImageView img = convertView.findViewById(R.id.img);
        int resId = R.drawable.icon_down;
        if(openFlag == 0){
            resId = R.drawable.icon_right;
        }
        img.setTag(resId);
        img.setImageResource(resId);

        rootView.setOnClickListener(view -> {
            Integer resourceId = (Integer) img.getTag();
            if (resourceId == R.drawable.icon_right) {
                img.setTag(R.drawable.icon_down);
                img.setImageResource(R.drawable.icon_down);
                drawerCallback.onEvent(true);
            } else {
                img.setTag(R.drawable.icon_right);
                img.setImageResource(R.drawable.icon_right);
                drawerCallback.onEvent(false);
            }
        });
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

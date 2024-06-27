package com.android.documentsui.files;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

public class IntermediateActivity extends Activity {
    private static final int REQUEST_CODE_TARGET = 88;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 启动目标活动

        Intent intent1 = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent1.setType(getIntent().getStringExtra("mimeType"));
        intent1.addCategory(Intent.CATEGORY_OPENABLE);
        intent1.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent1.putExtra(Intent.EXTRA_TITLE, getIntent().getStringExtra(Intent.EXTRA_TITLE));
        startActivityForResult(intent1, REQUEST_CODE_TARGET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_TARGET) {
            // 将结果传递回原始活动
            setResult(resultCode, data);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

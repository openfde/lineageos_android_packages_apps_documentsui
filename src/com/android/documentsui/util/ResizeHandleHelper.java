package com.android.documentsui.util;


import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class ResizeHandleHelper {
    private static final int MIN_WIDTH = 150;  // 最小宽度
    private static final int MAX_WIDTH = 300;  // 最大宽度

    private View resizeHandle;
    private View resizableView;
    private ViewGroup.LayoutParams layoutParams;

    private float startX;
    private int startWidth;

    public ResizeHandleHelper(View resizeHandle, View resizableView) {
        this.resizeHandle = resizeHandle;
        this.resizableView = resizableView;
        this.layoutParams = resizableView.getLayoutParams();

        setupResizeHandle();
    }

    private void setupResizeHandle() {
        resizeHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startWidth = layoutParams.width;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getRawX() - startX;
                        int newWidth = startWidth + (int) deltaX;

                        // 限制宽度范围
                        if (newWidth >= MIN_WIDTH && newWidth <= MAX_WIDTH) {
                            layoutParams.width = newWidth;
                            resizableView.setLayoutParams(layoutParams);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        return true;
                }
                return false;
            }
        });
    }
}

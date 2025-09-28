package com.android.documentsui.util;

import android.view.WindowManager;
import android.view.WindowMetrics;
import android.graphics.Rect;
import android.app.Activity;
import android.os.Build;

public class Utils {

    public static boolean isFreeformMaximized(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return false;
        }

        WindowManager windowManager = activity.getWindowManager();
        WindowMetrics currentWindowMetrics = windowManager.getCurrentWindowMetrics();
        Rect currentBounds = currentWindowMetrics.getBounds();
        WindowMetrics maxWindowMetrics = windowManager.getMaximumWindowMetrics();
        Rect maxBounds = maxWindowMetrics.getBounds();
        return currentBounds.equals(maxBounds);
    }
    
}

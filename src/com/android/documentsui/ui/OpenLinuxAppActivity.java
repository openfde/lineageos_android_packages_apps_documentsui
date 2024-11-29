package com.android.documentsui.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.documentsui.R;
import com.android.documentsui.provider.FileUtils;
import com.android.documentsui.util.NetUtils;
import com.android.documentsui.util.SPUtils;

public class OpenLinuxAppActivity extends Activity {

    TextView txtOnlyOnce;
    TextView txtAlways;
    TextView txtDefaultType;

    LinearLayout layoutDirectType;
    LinearLayout layoutVncType;
    LinearLayout layoutX11Type;

    View viewLine;
    View txtOtherTitle;

    Context context;

    private static final String TAG = "OpenLinuxAppActivity";
    private static final int OPEN_TYPE_DIRECT = 0;
    private static final int OPEN_TYPE_VNC = 1;
    private static final int OPEN_TYPE_X11 = 2;
    private static final String OPEN_TYPE = "openType";
    private static final String OPEN_TYPE_ALWAYS = "openType_always";
    private static final int OPEN_TYPE_DEFAULT = OPEN_TYPE_VNC;

    String name;
    String exec;
    int openType;

    boolean isInstallVnc;
    boolean isInstallX11;
    boolean isShellType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle("");
        context = this;

        setContentView(R.layout.activity_open_linux_app);

        initView();
        initEvent();

    }

    private void initView() {
        txtOnlyOnce = findViewById(R.id.txtOnlyOnce);
        txtAlways = findViewById(R.id.txtAlways);
        txtDefaultType = findViewById(R.id.txtDefaultType);

        viewLine = findViewById(R.id.viewLine);
        txtOtherTitle = findViewById(R.id.txtOtherTitle);

        layoutDirectType = findViewById(R.id.layoutDirectType);
        layoutVncType = findViewById(R.id.layoutVncType);
        layoutX11Type = findViewById(R.id.layoutX11Type);

//        txtOnlyOnce.setTextColor(R.color.primary);
//        txtAlways.setTextColor(android.R.color.primary_text_dark);

        String fdeModel = getIntent().getStringExtra("fdeModel");
        String openParams = getIntent().getStringExtra("openParams");

        String[] arrParams = openParams.split("###");
        name = arrParams[0].trim().replaceAll("%[FfUu]", "");
        exec = arrParams[1].trim().replaceAll("%[FfUu]", "");
        String type = arrParams[2];

        isInstallVnc = FileUtils.isAppInstalled(context, "com.iiordanov.bVNC");
        isInstallX11 = FileUtils.isAppInstalled(context, "com.fde.x11");
        isShellType = "shell".equals(fdeModel) ? true : false;

        Log.i(TAG, "fdeModel " + fdeModel + ",openParams " + openParams + ",isInstallVnc " + isInstallVnc + ",isInstallX11  " + isInstallX11);

//        layoutDirectType.setVisibility(isShellType ? View.VISIBLE : View.GONE);
//        layoutVncType.setVisibility(isInstallVnc ? View.VISIBLE : View.GONE);
//        layoutX11Type.setVisibility(isInstallX11 ? View.VISIBLE : View.GONE);


        openType = SPUtils.getIntDocInfo(context, OPEN_TYPE, OPEN_TYPE_DEFAULT);
        boolean[] arrBool = {isShellType, isInstallVnc, isInstallX11};
        long count = countTrueValues(arrBool);
        Log.i(TAG, "openType: " + openType + " , count:  " + count);
        if (count > 1 && "open".equals(type)) {
            viewLine.setVisibility(View.VISIBLE);
            txtOtherTitle.setVisibility(View.VISIBLE);
            int openTypeAlways = SPUtils.getIntDocInfo(context, OPEN_TYPE_ALWAYS, -1);
            Log.i(TAG, "openTypeAlways: " + openTypeAlways );
            if (openTypeAlways > -1 && arrBool[openTypeAlways]) {
                openType = openTypeAlways;
                selectOpenType();
            }
        } else if (count == 1) {
            if (isShellType) {
                openDirect();
            } else if (isInstallVnc) {
                openVnc();
            } else {
                openX11();
            }
        } else {
            //please install vnc x11 app
        }

        Drawable leftDrawable = getResources().getDrawable(R.mipmap.icon_vnc);

        switch (openType) {
            case OPEN_TYPE_DIRECT:
                txtDefaultType.setText(R.string.fde_linux_open);
                leftDrawable = getResources().getDrawable(R.mipmap.icon_vnc);
//                layoutDirectType.setVisibility(View.GONE);
                break;
            case OPEN_TYPE_VNC:
                txtDefaultType.setText(R.string.fde_vnc_open);
                leftDrawable = getResources().getDrawable(R.mipmap.icon_vnc);
//                layoutVncType.setVisibility(View.GONE);
                break;
            case OPEN_TYPE_X11:
                txtDefaultType.setText(R.string.fde_xserver_open);
                leftDrawable = getResources().getDrawable(R.mipmap.icon_xserver);
//                layoutX11Type.setVisibility(View.GONE);
                break;
            default:
                txtDefaultType.setText(R.string.fde_vnc_open);
                leftDrawable = getResources().getDrawable(R.mipmap.icon_vnc);
//                layoutVncType.setVisibility(View.GONE);
                break;
        }
        ;

        leftDrawable.setBounds(0, 0, leftDrawable.getIntrinsicWidth(), leftDrawable.getIntrinsicHeight());
        txtDefaultType.setCompoundDrawables(leftDrawable, null, null, null);

    }


    private void initEvent() {
        txtOnlyOnce.setOnClickListener(view -> {
            selectOpenType();
            SPUtils.putIntDocInfo(context, OPEN_TYPE_ALWAYS, -1);
            finish();
        });

        txtAlways.setOnClickListener(view -> {
            selectOpenType();
            SPUtils.putIntDocInfo(context, OPEN_TYPE_ALWAYS, openType);
            finish();
        });

        layoutDirectType.setOnClickListener(view -> {
            openDirect();
            SPUtils.putIntDocInfo(context, OPEN_TYPE_ALWAYS, -1);
            cleanPrefered();
        });


        layoutVncType.setOnClickListener(view -> {
            openVnc();
            SPUtils.putIntDocInfo(context, OPEN_TYPE_ALWAYS, -1);
            cleanPrefered();
        });

        layoutX11Type.setOnClickListener(view -> {
            openX11();
            SPUtils.putIntDocInfo(context, OPEN_TYPE_ALWAYS, -1);
            cleanPrefered();
        });
    }


    private void selectOpenType() {
        switch (openType) {
            case OPEN_TYPE_DIRECT:
                openDirect();
                break;
            case OPEN_TYPE_VNC:
                openVnc();
                break;
            case OPEN_TYPE_X11:
                openX11();
                break;
            default:
                openVnc();
                break;
        }
    }

    private void openDirect() {
        if(!isShellType){
            Toast.makeText(context,R.string.fde_app_choose,Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                NetUtils.gotoLinuxApp(name, exec);
            }
        }).start();
        SPUtils.putIntDocInfo(context, OPEN_TYPE, OPEN_TYPE_DIRECT);
        finish();
    }

    private void openVnc() {
        if(!isInstallVnc){
            Toast.makeText(context,getString(R.string.fde_app_install)+"Vnc app",Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.iiordanov.bVNC", "com.iiordanov.bVNC.LinuxAppActivity");
        intent.setComponent(componentName);
        intent.putExtra("fromOther", "Launcher");
        intent.putExtra("vnc_activity_name", name);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        SPUtils.putIntDocInfo(context, OPEN_TYPE, OPEN_TYPE_VNC);
        finish();
    }

    private void openX11() {
        if(!isInstallX11){
            Toast.makeText(context,getString(R.string.fde_app_install)+"X11 app",Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.fde.x11", "com.fde.x11.AppListActivity");
        intent.setComponent(componentName);
        // intent.putExtra("Path", "mate-terminal");
        // intent.putExtra("App", "MATE Terminal");
        intent.putExtra("App", name);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        SPUtils.putIntDocInfo(context, OPEN_TYPE, OPEN_TYPE_X11);
        finish();
    }

    private int countTrueValues(boolean[] array) {
        int count = 0;
        for (boolean value : array) {
            if (value) {
                count++;
            }
        }
        return count;
    }

    private void cleanPrefered(){
        getPackageManager().clearPackagePreferredActivities("com.iiordanov.bVNC");
        getPackageManager().clearPackagePreferredActivities("com.fde.x11");
        getPackageManager().clearPackagePreferredActivities("com.fde.txt");
    }

}
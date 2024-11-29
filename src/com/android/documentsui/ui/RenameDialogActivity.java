package com.android.documentsui.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.documentsui.DocumentsApplication;
import com.android.documentsui.IpcService;
import com.android.documentsui.R;
import com.android.documentsui.provider.FileUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;

public class RenameDialogActivity extends Activity {
    TextView txtOk;
    TextView txtCancel;
    TextInputEditText mEditText;
    String oldFileName ;
    TextInputLayout inputWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle("");
        oldFileName = getIntent().getStringExtra("oldFileName");
        setContentView(R.layout.dialog_file_rename);
        initView();

    }

    private void initView(){
        txtOk = findViewById(R.id.txtOk);
        txtCancel = findViewById(R.id.txtCancel);
        mEditText = findViewById(R.id.editContent);
        inputWrapper = findViewById(R.id.input_wrapper);
        mEditText.setText(oldFileName);
        inputWrapper.setBoxStrokeColor(ContextCompat.getColor(this, R.color.primary));
        inputWrapper.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        mEditText.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(
                            TextView view, int actionId, @Nullable KeyEvent event) {
                        if ((actionId == EditorInfo.IME_ACTION_DONE) || (event != null
                                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                && event.hasNoModifiers())) {
                            reNameFileName();
                        }
                        return false;
                    }
                });
        mEditText.requestFocus();
        selectFileName(mEditText);

        txtOk.setOnClickListener(view->{
            reNameFileName();
        });

        txtCancel.setOnClickListener(view->{
            finish();
        });
    }

    private void reNameFileName(){
        String newFileName = mEditText.getText().toString();
        Log.i("bella","oldFileName: " +oldFileName +", newFileName: "+newFileName );
        File file = new File(FileUtils.PATH_ID_DESKTOP + oldFileName);
        file.renameTo(new File(FileUtils.PATH_ID_DESKTOP + newFileName));
        IpcService ipcService  = DocumentsApplication.getInstance().getIpcService();
        if(ipcService !=null ){
            ipcService.gotoClientApp("RENAME",oldFileName +"###"+newFileName);
        }else {
            Log.i("bella","ipcService is null");
        }
        finish();
    }

    private void selectFileName(EditText editText) {
        String text = editText.getText().toString();
        int separatorIndex = text.lastIndexOf(".");
        editText.setSelection(0,
                (separatorIndex == -1 ) ? text.length() : separatorIndex);
    }

}
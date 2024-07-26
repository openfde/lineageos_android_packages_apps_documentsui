package com.android.documentsui.files;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.documentsui.R;
import com.android.documentsui.provider.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class DocEditViewActivity extends AppCompatActivity {

    String docPath;
    EditText editText;
    TextView txtTitle;
    TextView txtSave;
    Context context;

    int contentBytesLen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_doc_edit_view);
        editText = (EditText) findViewById(R.id.editView);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtSave = (TextView) findViewById(R.id.txtSave);
        docPath = getIntent().getStringExtra("docPath");
        String docTitle = getIntent().getStringExtra("docTitle");
        txtTitle.setText(docTitle);
        setTitle(docTitle);

        readExternalFile(docPath);

        txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileContents = editText.getText().toString();
                if (docPath == null) {
                    Uri uri = getIntent().getData();
                    FileUtils.clearFileContent(uri,contentBytesLen,context);
                    FileUtils.writeTextToUri(uri, fileContents,context);
                } else {
                    FileUtils.writeTextToPath(docPath, fileContents,context);
                }
            }
        });

    }


    private void readExternalFile(String docPath) {
        if (docPath == null) {
            Uri uri = getIntent().getData();
            String fileContent = FileUtils.readTextFromUri(uri,context);
            contentBytesLen = fileContent.getBytes().length;
            Log.i("bella", "fileContent1   ----- > " + fileContent);
            editText.setText(fileContent);
        } else {
            String fileContent = FileUtils.readTextFromPath(docPath);
            Log.i("bella", "fileContent2   ----- > " + fileContent);
            editText.setText(fileContent);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i("bella", "onBackPressed............");
    }


}
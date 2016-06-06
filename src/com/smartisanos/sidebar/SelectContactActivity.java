package com.smartisanos.sidebar;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class SelectContactActivity extends Activity {
    private static final String TAG = SelectContactActivity.class.getName();

    private static final int REQUEST_Code = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()...");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setPackage("com.android.contacts");
        intent.setType("vnd.android.cursor.dir/phone_v2");
        try{
            startActivityForResult(intent, REQUEST_Code);
        }catch(ActivityNotFoundException e){
            // TODO show dialog
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult()...");
        if(requestCode == REQUEST_Code){
            if(data != null && data.getData() != null){
                Uri uri = data.getData();
                Log.d(TAG, "uri -> " + uri);
            }
            finish();
        }
    }
}

package com.smartisanos.sidebar.setting;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarStatus;

public class AddResolveInfoGroupActivtiy extends BaseActivity {
    private static final String TAG = AddResolveInfoGroupActivtiy.class.getName();

    private ListView mListView;
    private AddResolveInfoGroupAdapter mAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "AddApplicationActivity.onCreate()...");
        setContentView(R.layout.add_resolve_layout);
        getWindow().setBackgroundDrawable(null);
        setupBackBtnOnTitle();
        mListView = (ListView) findViewById(R.id.list_view);
        mAdapter = new AddResolveInfoGroupAdapter(getApplicationContext());
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.onStart();
        SidebarController.getInstance(getApplicationContext()).requestStatus(
                SidebarStatus.UNNAME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.onStop();
        SidebarController.getInstance(getApplicationContext()).requestStatus(
                SidebarStatus.NORMAL);
    }
}

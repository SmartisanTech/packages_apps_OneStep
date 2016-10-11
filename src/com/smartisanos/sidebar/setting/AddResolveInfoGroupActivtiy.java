package com.smartisanos.sidebar.setting;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarStatus;
import com.smartisanos.sidebar.util.Utils;

public class AddResolveInfoGroupActivtiy extends BaseActivity {
    private static final String TAG = AddResolveInfoGroupActivtiy.class.getName();

    private static final int MAX_TIMES = 5;
    private static final String KEY_TIMES = "hint_sort_by_long_press_times";

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

        showHintToast();
    }

    private void showHintToast() {
        int times = Utils.Config.getIntValue(getApplicationContext(), KEY_TIMES);
        if (times < MAX_TIMES) {
            Toast.makeText(getApplicationContext(), R.string.hint_sort_by_long_press, Toast.LENGTH_SHORT).show();
            Utils.Config.setIntValue(getApplicationContext(), KEY_TIMES, times + 1);
        }
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

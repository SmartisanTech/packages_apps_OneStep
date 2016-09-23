package com.smartisanos.sidebar.setting;

import android.os.Bundle;
import android.widget.ListView;

import com.smartisanos.sidebar.R;

public class AddApplicationActivity extends BaseActivity {
    private static final String TAG = AddApplicationActivity.class.getName();

    private ListView mListView;
    private AddApplicationAdapter mAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_app_activity);
        getWindow().setBackgroundDrawable(null);

        mListView = (ListView) findViewById(R.id.list_view);
        mAdapter = new AddApplicationAdapter(this.getApplicationContext());
        mListView.setAdapter(mAdapter);
        setupBackBtnOnTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.refreshData();
    }
}

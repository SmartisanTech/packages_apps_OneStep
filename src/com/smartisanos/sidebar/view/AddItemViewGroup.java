package com.smartisanos.sidebar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.smartisanos.sidebar.R;

public class AddItemViewGroup extends LinearLayout{

    private FullGridView mContacts, mResolveInfos;

    public AddItemViewGroup(Context context) {
        this(context, null);
    }

    public AddItemViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddItemViewGroup(Context context, AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AddItemViewGroup(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContacts = (FullGridView)findViewById(R.id.contact_gridview);
        mContacts.setAdapter(new AddContactAdapter(mContext));
        mResolveInfos = (FullGridView) findViewById(R.id.resolveinfo_gridview);
        mResolveInfos.setAdapter(new AddResolveInfoGroupAdapter(mContext, findViewById(R.id.add_resolve)));
    }
}

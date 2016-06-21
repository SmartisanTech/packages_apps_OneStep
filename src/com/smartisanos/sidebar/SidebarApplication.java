package com.smartisanos.sidebar;

import com.smartisanos.sidebar.util.MailContactsHelper;

import android.app.Application;

public class SidebarApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // this is necessary ! init it to make its inner data be filled
        // so we can use it correctly later
        MailContactsHelper.getInstance(this);
    }

}

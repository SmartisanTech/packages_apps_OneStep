package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;

public class MailContactsHelper {
    private volatile static MailContactsHelper sInstance;
    public synchronized static MailContactsHelper getInstance(Context context){
        if(sInstance == null){
            synchronized(MailContactsHelper.class){
                if(sInstance == null){
                    sInstance = new MailContactsHelper(context);
                }
            }
        }
        return sInstance;
    }

    private Context mContext = null;
    private ContentObserver mContentObserver = null;
    private Map<String, List<Contact>> mContactsMap = new HashMap<String, List<Contact>>();
    private ContactRefreshAsyncTask mGenerateContactTask;

    private MailContactsHelper(Context context) {
        mContext = context;
        sendLoadingContactMessage();
        registerContentObserver();
    }

    private void registerContentObserver() {
        mContentObserver = new ContactsObserver(mHandler);
        mContext.getContentResolver().registerContentObserver(ContactsContract.CommonDataKinds.Email.CONTENT_URI, true, mContentObserver);
    }

    public boolean isContact(String address) {
        synchronized (MailContactsHelper.class) {
            return mContactsMap.containsKey(address);
        }
    }

    public long getContactId(String address) {
        synchronized (MailContactsHelper.class) {
            if (TextUtils.isEmpty(address) || mContactsMap.isEmpty()
                    || !mContactsMap.containsKey(address))
                return -1;
            return getContectNameList(address).get(0).id;
        }
    }

    private List<Contact> getContectNameList(String address) {
        synchronized (MailContactsHelper.class) {
            return mContactsMap.get(address);
        }
    }

    private void sendLoadingContactMessage() {
        mHandler.removeMessages(MSG_CONTACTS_CHANGED);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_CONTACTS_CHANGED), 500);
    }

    private void setContactsMap(Map<String, List<Contact>> contactsMap) {
        synchronized (MailContactsHelper.class) {
            mContactsMap.clear();
            mContactsMap.putAll(contactsMap);
        }
    }

    private static final int MSG_CONTACTS_CHANGED = 1;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CONTACTS_CHANGED:
                    if (mGenerateContactTask != null) {
                        mGenerateContactTask.cancel(true);
                    }
                    mGenerateContactTask = new ContactRefreshAsyncTask();
                    mGenerateContactTask.execute();
                    break;

                default:
                    break;
            }
        }
    };

    private Map<String, List<Contact>> getAllContactsWithEmail() {
        Map<String, List<Contact>> contactsMap = new HashMap<String, List<Contact>>();
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    new String[] {
                            ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Email.ADDRESS
                    }, null, null, null);
            if(cursor != null && cursor.moveToFirst()){
                do{
                    Contact contact = new Contact();
                    contact.id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    contact.email = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS));
                    contact.name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    if (contactsMap.containsKey(contact.email)) {
                        contactsMap.get(contact.email).add(contact);
                    } else {
                        List<Contact> list = new ArrayList<Contact>();
                        list.add(contact);
                        contactsMap.put(contact.email, list);
                    }
                }while(cursor.moveToNext());
            }
            return contactsMap;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    class Contact {
        public long id;
        public String email;
        public String name;
    }

    private class ContactsObserver extends ContentObserver {
        public ContactsObserver(final Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            mHandler.obtainMessage(MSG_CONTACTS_CHANGED).sendToTarget();
        }
    }

    private class ContactRefreshAsyncTask extends AsyncTask<Object, Object, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            setContactsMap(getAllContactsWithEmail());
            return null;
        }
    }
}

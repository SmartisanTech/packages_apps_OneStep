package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ContactManager extends DataManager{
    private volatile static ContactManager sInstance;
    public synchronized static ContactManager getInstance(Context context){
        if(sInstance == null){
            synchronized(ContactManager.class){
                if(sInstance == null){
                    sInstance = new ContactManager(context);
                }
            }
        }
        return sInstance;
    }

    private Context mContext;
    private List<ContactItem> mContacts = new ArrayList<ContactItem>();
    private Handler mHandler;
    private ContactManager(Context context){
        mContext = context;
        mContacts = ContactItem.getContactList(mContext);
        Collections.sort(mContacts, new ContactComparator());

        HandlerThread thread = new HandlerThread(RecentFileManager.class.getName());
        thread.start();
        mHandler = new ContactManagerHandler(thread.getLooper());
    }

    public List<ContactItem> getContactList() {
        List<ContactItem> ret = new ArrayList<ContactItem>();
        synchronized (mContacts) {
            ret.addAll(mContacts);
        }
        return ret;
    }

    public void updateOrder() {
        synchronized (mContacts) {
            Collections.sort(mContacts, new ContactComparator());
        }
        notifyListener();
        mHandler.obtainMessage(MSG_UPDATE_ORDER).sendToTarget();
    }

    public void addContact(ContactItem ci) {
        synchronized (mContacts) {
            for (int i = 0; i < mContacts.size(); ++i) {
                if (ci.sameContact(mContacts.get(i))) {
                    ci.setIndex(mContacts.get(i).getIndex());
                    mContacts.set(i, ci);
                    saveContact(ci);
                    notifyListener();
                    return;
                }
            }
            if (mContacts.size() == 0) {
                ci.setIndex(mContacts.size());
            } else {
                int maxIndex = mContacts.get(0).getIndex();
                for (int i = 1; i < mContacts.size(); ++i) {
                    if (mContacts.get(i).getIndex() > maxIndex) {
                        maxIndex = mContacts.get(i).getIndex();
                    }
                }
                ci.setIndex(maxIndex);
            }
            mContacts.add(0, ci);
            notifyListener();
            saveContact(ci);
        }
    }

    public void remove(ContactItem ci) {
        synchronized (mContacts) {
            for (int i = 0; i < mContacts.size(); ++i) {
                if (ci.sameContact(mContacts.get(i))) {
                    mContacts.remove(i);
                    notifyListener();
                    deleteContactFromDatabase(ci);
                    return;
                }
            }
        }
    }

    private void saveContact(ContactItem ci){
        mHandler.obtainMessage(MSG_SAVE_CONTACT, ci).sendToTarget();
    }

    private void deleteContactFromDatabase(ContactItem ci){
        mHandler.obtainMessage(MSG_DELETE_CONTACT, ci).sendToTarget();
    }

    public static final class ContactComparator implements Comparator<ContactItem> {
        @Override
        public int compare(ContactItem lhs, ContactItem rhs) {
            if (lhs.getIndex() > rhs.getIndex()) {
                return -1;
            } else if (lhs.getIndex() < rhs.getIndex()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private static final int MSG_SAVE_CONTACT = 0;
    private static final int MSG_DELETE_CONTACT = 1;
    private static final int MSG_UPDATE_ORDER = 2;
    private class ContactManagerHandler extends Handler {
        public ContactManagerHandler(Looper looper) {
            super(looper, null, false);
        }

        @Override
        public void handleMessage(Message msg) {
            ContactItem ci = (ContactItem) msg.obj;
            switch (msg.what) {
            case MSG_SAVE_CONTACT:
                ci.save();
                break;
            case MSG_DELETE_CONTACT:
                ci.deleteFromDatabase();
                break;
            case MSG_UPDATE_ORDER:
                synchronized(mContacts){
                    for(ContactItem cni : mContacts){
                        cni.save();
                    }
                }
                break;
            }
        }
    }
}

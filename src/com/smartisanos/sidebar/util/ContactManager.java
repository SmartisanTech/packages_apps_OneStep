package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

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
    private ContactManager(Context context){
        mContext = context;
        mContacts = ContactItem.getContactList(mContext);
        Collections.sort(mContacts, new ContactComparator());
    }

    public List<ContactItem> getContactList(){
        return mContacts;
    }

    public void addContact(ContactItem ci){
        for(int i = 0; i < mContacts.size(); ++ i){
            if(ci.sameContact(mContacts.get(i))){
                ci.setIndex(mContacts.get(i).getIndex());
                mContacts.set(i, ci);
                saveContact(ci);
                notifyListener();
                return ;
            }
        }
        ci.setIndex(mContacts.size());
        saveContact(ci);
        mContacts.add(0, ci);
        notifyListener();
    }

    private void saveContact(ContactItem ci){
        new SaveContactTask().execute(ci);
    }

    class SaveContactTask extends AsyncTask<ContactItem, Integer, Void> {
        @Override
        protected Void doInBackground(ContactItem... params) {
            ContactItem ci = params[0];
            ci.save();
            return null;
        }
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
}

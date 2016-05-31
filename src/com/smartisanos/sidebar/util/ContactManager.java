package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

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
    }

    public List<ContactItem> getContactList(){
        return mContacts;
    }

    public void addContact(ContactItem ci){
        for(int i = 0; i < mContacts.size(); ++ i){
            if(ci.sameContact(mContacts.get(i))){
                ci.setIndex(mContacts.get(i).getIndex());
                mContacts.set(i, ci);
                ci.save();
                notifyListener();
                return ;
            }
        }
        ci.setIndex(mContacts.size());
        ci.save();
        mContacts.add(0, ci);
        notifyListener();
    }
}

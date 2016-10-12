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
import android.widget.Toast;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.receiver.ShortcutReceiver;

public class ContactManager extends DataManager{
    private static final LOG log = LOG.getInstance(ContactManager.class);

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

    private static final int sMaxNumber = 10;

    private Context mContext;
    private List<ContactItem> mContacts = new ArrayList<ContactItem>();
    private Handler mHandler;

    private Toast mLimitToast;
    private Toast mContactAddedToast;

    private ContactManager(Context context){
        mContext = context;
        HandlerThread thread = new HandlerThread(ContactManager.class.getName());
        thread.start();
        mHandler = new ContactManagerHandler(thread.getLooper());
        mHandler.obtainMessage(MSG_READ_CONTACTS).sendToTarget();
    }

    public List<ContactItem> getContactList() {
        List<ContactItem> ret = new ArrayList<ContactItem>();
        synchronized (mContacts) {
            ret.addAll(mContacts);
        }
        return ret;
    }

    private void readContacts(){
        List<ContactItem> list = ContactItem.getContactList(mContext);
        Collections.sort(list, new ContactComparator());
        synchronized(mContacts){
            mContacts.clear();
            mContacts.addAll(list);
        }
        notifyListener();
    }

    public void updateOrder() {
        synchronized (mContacts) {
            Collections.sort(mContacts, new ContactComparator());
        }
        notifyListener();
        mHandler.obtainMessage(MSG_UPDATE_ORDER).sendToTarget();
    }

    public void removeDoppelgangerShortcut(String pkg) {
        if (pkg == null) {
            return;
        }
        log.error("removeDoppelgangerShortcut ["+pkg+"]");
        mHandler.obtainMessage(MSG_REMOVE_DOPPELGANGER, pkg).sendToTarget();
    }

    public boolean addContact(ContactItem ci) {
        synchronized (mContacts) {
            if(mContacts.size() >= sMaxNumber) {
                showToastDueToLimit();
                return false;
            }
            for (int i = 0; i < mContacts.size(); ++i) {
                if (ci.sameContact(mContacts.get(i))) {
                    ci.setIndex(mContacts.get(i).getIndex());
                    mContacts.set(i, ci);
                    saveContact(ci);
                    showContactAddedToast();
                    notifyListener();
                    return true;
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
                ci.setIndex(maxIndex + 1);
            }
            ci.newAdded = true;
            mContacts.add(0, ci);
            saveContact(ci);
            showContactAddedToast();
            notifyListener();
            return true;
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


    private void showToastDueToLimit() {
        if (mLimitToast != null) {
            mLimitToast.cancel();
        }
        mLimitToast = Toast.makeText(mContext, mContext.getResources()
                .getString(R.string.add_at_most_contact_number, sMaxNumber),
                Toast.LENGTH_SHORT);
        mLimitToast.show();
    }

    private void showContactAddedToast() {
        if (mContactAddedToast != null) {
            mContactAddedToast.cancel();
        }
        mContactAddedToast = Toast.makeText(mContext, R.string.contact_added, Toast.LENGTH_SHORT);
        mContactAddedToast.show();
    }

    private void saveContact(ContactItem ci){
        mHandler.obtainMessage(MSG_SAVE_CONTACT, ci).sendToTarget();
    }

    private void deleteContactFromDatabase(ContactItem ci){
        mHandler.obtainMessage(MSG_DELETE_CONTACT, ci).sendToTarget();
    }

    public void onPackageAdded(String packageName){
        // NA
    }

    public void onPackageRemoved(String packageName) {
        boolean deleted = false;
        synchronized (mContacts) {
            for (int i = 0; i < mContacts.size(); ++i) {
                ContactItem ci = mContacts.get(i);
                if (ci.getPackageName().equals(packageName)) {
                    deleted = true;
                    mContacts.remove(i);
                    i--;
                    deleteContactFromDatabase(ci);
                }
            }
        }
        if (deleted) {
            notifyListener();
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

    private static final int MSG_SAVE_CONTACT = 0;
    private static final int MSG_DELETE_CONTACT = 1;
    private static final int MSG_UPDATE_ORDER = 2;
    private static final int MSG_READ_CONTACTS = 3;
    private static final int MSG_REMOVE_DOPPELGANGER = 4;

    private class ContactManagerHandler extends Handler {
        public ContactManagerHandler(Looper looper) {
            super(looper, null, false);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAVE_CONTACT : {
                    ContactItem ci = (ContactItem) msg.obj;
                    ci.save();
                    break;
                }
                case MSG_DELETE_CONTACT : {
                    ContactItem ci = (ContactItem) msg.obj;
                    ci.deleteFromDatabase();
                    break;
                }
                case MSG_UPDATE_ORDER : {
                    synchronized(mContacts){
                        for(ContactItem cni : mContacts){
                            cni.save();
                        }
                    }
                    break;
                }
                case MSG_READ_CONTACTS : {
                    readContacts();
                    break;
                }
                case MSG_REMOVE_DOPPELGANGER : {
                    String pkg = (String) msg.obj;
                    if (pkg == null) {
                        break;
                    }
                    List<ContactItem> removeList = new ArrayList<ContactItem>();
                    if (pkg.equals(ShortcutReceiver.WECHAT)) {
                        WechatContact.removeDoppelgangerShortcut(mContext);
                        for (ContactItem item : mContacts) {
                            if (item instanceof WechatContact) {
                                WechatContact contact = (WechatContact) item;
                                if (contact.getUserId() == UserPackage.USER_DOPPELGANGER) {
                                    removeList.add(item);
                                }
                            }
                        }
                    } else if (pkg.equals(DingDingContact.PKG_NAME)) {
                        DingDingContact.removeDoppelgangerShortcut(mContext);
                        for (ContactItem item : mContacts) {
                            if (item instanceof DingDingContact) {
                                DingDingContact contact = (DingDingContact) item;
                                if (contact.systemUid == UserPackage.USER_DOPPELGANGER) {
                                    removeList.add(item);
                                }
                            }
                        }
                    }
                    if (removeList.size() > 0) {
                        synchronized(mContacts) {
                            for (ContactItem item : removeList) {
                                mContacts.remove(item);
                            }
                        }
                    }
                    notifyListener();
                    break;
                }
            }
        }
    }

    public void reportToTracker() {
        int countWechat = 0;
        int countDingDing = 0;
        int countMms = 0;
        int countEmail = 0;
        synchronized (mContacts) {
            for (ContactItem item : mContacts) {
                if (item instanceof WechatContact) {
                    countWechat++;
                } else if (item instanceof DingDingContact) {
                    countDingDing++;
                } else if (item instanceof MmsContact) {
                    countMms++;
                } else if (item instanceof MailContact) {
                    countEmail++;
                }
            }
        }
        if (countWechat > 0) {
            Tracker.onAttach(Tracker.STATUS_APPNAME, "wechat_contacts",
                    countWechat + "");
        }
        if (countDingDing > 0) {
            Tracker.onAttach(Tracker.STATUS_APPNAME, "dingding_contacts",
                    countDingDing + "");
        }
        if (countMms > 0) {
            Tracker.onAttach(Tracker.STATUS_APPNAME, "message_contacts",
                    countMms + "");
        }
        if (countEmail > 0) {
            Tracker.onAttach(Tracker.STATUS_APPNAME, "email_contacts",
                    countEmail + "");
        }
    }
}

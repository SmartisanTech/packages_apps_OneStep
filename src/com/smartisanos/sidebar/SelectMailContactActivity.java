package com.smartisanos.sidebar;

import java.util.ArrayList;
import java.util.List;

import com.smartisanos.sidebar.util.ContactItem;
import com.smartisanos.sidebar.util.ContactManager;
import com.smartisanos.sidebar.util.MailContact;
import com.smartisanos.sidebar.util.MailContactsHelper;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import smartisanos.api.ContactContantsSmt;
import smartisanos.api.ContactsContractSmt;
public class SelectMailContactActivity extends Activity {

    private static final int REQUEST_SELECT_MAIL = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent toIntent = new Intent(
                "com.android.contact.activities.ContactSelectionActivity.pickEmailsExceptSelected");
        toIntent.addCategory("android.intent.category.DEFAULT");

        ArrayList<Long> ids = new ArrayList<Long>();
        for (ContactItem ci : ContactManager.getInstance(this).getContactList()) {
            if (ci instanceof MailContact) {
                MailContact mc = (MailContact) ci;
                if (MailContactsHelper.getInstance(this).isContact(mc.getAddress())) {
                    ids.add(MailContactsHelper.getInstance(this).getContactId( mc.getAddress()));
                }
            }
        }

        if (ids.size() > 0) {
            toIntent.putExtra(ContactsContractSmt.get_BASE_URI(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI.toString());
            toIntent.putExtra(ContactsContractSmt.get_EXTRA_PHONE_IDS(), ids);
        }
        startActivityForResult(toIntent, REQUEST_SELECT_MAIL);
        overridePendingTransition(smartisanos.R.anim.pop_up_in, smartisanos.R.anim.fake_anim);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_SELECT_MAIL) {
            if(resultCode == Activity.RESULT_OK){
                addContacts(resultCode, data);
            }
            finish();
        }
    }

    private void addContacts(int request, Intent data) {
        if (data == null) {
            return;
        }
        ArrayList<Long> datas = (ArrayList<Long>) data.getExtra(ContactContantsSmt.EXTRA_PHONE_IDS);
        if (datas == null || datas.size() == 0) {
            return;
        }
        String baseUri = data.getStringExtra(ContactContantsSmt.BASE_URI);
        ArrayList<Uri> uris = new ArrayList<Uri>();
        for (long id : datas) {
            uris.add(Uri.withAppendedPath(Uri.parse(baseUri), String.valueOf(id)));
        }
        new AllEmailContactsTask(this.getApplicationContext()).execute(uris);
    }

    private class AllEmailContactsTask extends AsyncTask<List<Uri>, Integer, List<ContactItem>> {
        private Context mContext;

        public AllEmailContactsTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected List<ContactItem> doInBackground(List<Uri>... params) {
            List<ContactItem> ret = new ArrayList<ContactItem>();
            for (Uri uri : params[0]) {
                ret.addAll(getAllContactsWithEmail(mContext, uri));
            }
            return ret;
        }

        @Override
        protected void onPostExecute(List<ContactItem> result) {
            for (ContactItem ci : result) {
                ContactManager.getInstance(mContext).addContact(ci);
            }
        }
    }

    private List<MailContact> getAllContactsWithEmail(Context context, Uri uri) {
        List<MailContact> result = new ArrayList<MailContact>();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri,
                            new String[] {
                                    ContactsContract.Contacts._ID,
                                    ContactsContract.Contacts.LOOKUP_KEY,
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Email.ADDRESS, },
                            null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String email = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS));
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        result.add(new MailContact(context, name, email));
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }
}

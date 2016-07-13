package com.smartisanos.sidebar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.smartisanos.sidebar.util.ContactManager;
import com.smartisanos.sidebar.util.MailContact;

public class SelectMailContactActivity extends Activity {

    private static final int REQUEST_SELECT_MAIL = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setPackage("com.android.contacts");
        intent.setType("vnd.android.cursor.dir/email_v2");
        /**
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
            intent.putExtra(ContactsContractSmt.get_BASE_URI(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI.toString());
            intent.putExtra(ContactsContractSmt.get_EXTRA_PHONE_IDS(), ids);
        }
        */
        startActivityForResult(intent, REQUEST_SELECT_MAIL);
        overridePendingTransition(smartisanos.R.anim.pop_up_in, smartisanos.R.anim.fake_anim);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_SELECT_MAIL) {
            if(resultCode == Activity.RESULT_OK){
                addContacts(data);
            }
            finish();
        }
    }

    private void addContacts(Intent result) {
        if (result == null|| result.getData() == null) {
            return;
        }
        new AllEmailContactsTask().execute(result.getData());
    }


    private class AllEmailContactsTask extends AsyncTask<Uri, Integer, MailContact> {
        @Override
        protected void onPostExecute(MailContact contact) {
            if (contact != null) {
                ContactManager.getInstance(getApplicationContext()).addContact(contact);
            }
        }

        @Override
        protected MailContact doInBackground(Uri... params) {
            Uri uri = params[0];
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE));
                        if (TextUtils.equals(mimeType, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                            String email = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS));
                            String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(name)) {
                                return new MailContact(getApplicationContext(),name, email);
                            }
                        }
                    } while (cursor.moveToNext());
                }
            } catch (Exception ex){
                ex.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return null;
        }
    }
}

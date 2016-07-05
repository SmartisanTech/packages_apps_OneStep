package com.smartisanos.sidebar;

import com.smartisanos.sidebar.util.BitmapUtils;
import com.smartisanos.sidebar.util.ContactManager;
import com.smartisanos.sidebar.util.MmsContact;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

public class SelectContactActivity extends Activity {
    private static final String TAG = SelectContactActivity.class.getName();

    private static final int REQUEST_Code = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()...");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setPackage("com.android.contacts");
        intent.setType("vnd.android.cursor.dir/phone_v2");
        try{
            startActivityForResult(intent, REQUEST_Code);
        }catch(ActivityNotFoundException e){
            // TODO show dialog
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult()...");
        if(requestCode == REQUEST_Code){
            if(data != null && data.getData() != null){
                Uri uri = data.getData();
                Log.d(TAG, "uri -> " + uri);
                new PickContactNumberAsyncTask().execute(uri);
            }
            finish();
        }
    }

    class PickContactNumberAsyncTask extends AsyncTask<Uri, Integer, MmsContact> {
        @Override
        protected void onPostExecute(MmsContact contact) {
            if (contact != null) {
                ContactManager.getInstance(getApplicationContext()).addContact(contact);
            }
        }

        private Bitmap getAvatarById(long id){
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
            return BitmapFactory.decodeStream(ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), uri));
        }

        @Override
        protected MmsContact doInBackground(Uri... params) {
            Uri uri = params[0];
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        int mimeTypeIndex = cursor.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE);
                        final String mimeType = cursor.getString(mimeTypeIndex);
                        if (TextUtils.equals(mimeType, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                            String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            if (!TextUtils.isEmpty(number)) {
                                number = number.replace("-", "");
                                number = number.replace(" ", "");
                            }
                            String displayName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            int contactId = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                            Bitmap avatar = getAvatarById(contactId);
                            if (contactId > 0 && !TextUtils.isEmpty(number)) {
                                if (avatar == null) {
                                    return new MmsContact(getApplicationContext(), contactId, number, displayName);
                                } else {
                                    return new MmsContact(getApplicationContext(), contactId, number,
                                            BitmapUtils.getRoundedCornerBitmap(avatar), displayName);
                                }
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

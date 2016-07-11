package com.smartisanos.sidebar.view;

import java.util.ArrayList;
import java.util.List;

import com.smartisanos.sidebar.util.BitmapUtils;
import com.smartisanos.sidebar.util.ContactItem;
import com.smartisanos.sidebar.util.ContactManager;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.RecentUpdateListener;
import com.smartisanos.sidebar.util.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartisanos.sidebar.R;

public class ContactListAdapter extends DragEventAdapter {
    private static float SCALE_SIZE = 1.4f;
    private static final LOG log = LOG.getInstance(ContactListAdapter.class);

    private Context mContext;
    private ContactManager mManager;
    private List<ContactItem> mContacts;
    private List<ContactItem> mAcceptableContacts = new ArrayList<ContactItem>();
    private DragEvent mDragEvent;

    public ContactListAdapter(Context context) {
        mContext = context;
        mManager = ContactManager.getInstance(mContext);
        mContacts = mManager.getContactList();
        mAcceptableContacts.addAll(mContacts);
        mManager.addListener(new RecentUpdateListener() {
            @Override
            public void onUpdate() {
                mContacts = mManager.getContactList();
                updateAcceptableResolveInfos();
            }
        });
    }

    private void updateAcceptableResolveInfos() {
        mAcceptableContacts.clear();
        for (ContactItem ci : mContacts) {
            if (mDragEvent == null || ci.accptDragEvent(mDragEvent)) {
                mAcceptableContacts.add(ci);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onDragStart(DragEvent event) {
        if (mDragEvent != null) {
            mDragEvent.recycle();
            mDragEvent = null;
        }
        mDragEvent = DragEvent.obtain(event);
        updateAcceptableResolveInfos();
    }

    @Override
    public void onDragEnd() {
        if (mDragEvent == null) {
            return;
        }
        mDragEvent.recycle();
        mDragEvent = null;
        updateAcceptableResolveInfos();
    }

    @Override
    public int getCount() {
        return mAcceptableContacts.size();
    }

    @Override
    public Object getItem(int position) {
        return mAcceptableContacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ContactItem item = mAcceptableContacts.get(position);
        if (convertView == null) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.contact_item, null);
            holder = new ViewHolder();
            holder.contactAvatar = (ImageView) view.findViewById(R.id.contact_avatar);
            holder.typeIcon = (ImageView) view.findViewById(R.id.type_icon);
            holder.displayName = (TextView) view.findViewById(R.id.display_name);
            holder.view = view;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.restore();
        holder.setItem(item, mDragEvent != null);
        holder.view.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                ViewHolder vh = (ViewHolder) v.getTag();
                final int action = event.getAction();
                switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    vh.view.animate().scaleX(SCALE_SIZE).scaleY(SCALE_SIZE)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setStartDelay(0)
                    .setDuration(100).start();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    vh.view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;
                case DragEvent.ACTION_DROP:
                    vh.view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    boolean ret =  vh.mItem.handleDragEvent(event);
                    if(ret){
                        Utils.dismissAllDialog(mContext);
                    }
                    return ret;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
                }
                return false;
            }
        });
        return holder.view;
    }

    public static class ViewHolder {
        public View view;
        public ImageView contactAvatar;
        public ImageView typeIcon;
        public TextView displayName;

        public ContactItem mItem;

        public void setItem(ContactItem item, boolean draging) {
            mItem = item;
            if (item == null) {
                return;
            }
            typeIcon.setImageResource(item.getTypeIcon());
            displayName.setText(item.getDisplayName());
            if(draging){
                contactAvatar.setImageBitmap(mItem.getAvatar());
                displayName.setVisibility(View.VISIBLE);
            }else{
                contactAvatar.setImageBitmap(mItem.getAvatarWithGray());
                displayName.setVisibility(View.GONE);
            }
        }

        public void restore(){
            if (view.getVisibility() == View.INVISIBLE) {
                view.setVisibility(View.VISIBLE);
            }
            view.setTranslationY(0);
        }
    }

    public int objectIndex(ContactItem item) {
        if (item == null) {
            return -1;
        }
        if (mContacts == null) {
            return -1;
        }
        return mContacts.indexOf(item);
    }

    public void moveItemPostion(ContactItem item, int index) {
        if(index < 0){
            index = 0;
        }
        if(index >= mContacts.size()){
            index = mContacts.size() - 1;
        }
        int now = mContacts.indexOf(item);
        if(now == -1 || now == index){
            return ;
        }
        mContacts.remove(now);
        mContacts.add(index, item);
        onOrderChange();
    }

    private void onOrderChange(){
        for(int i = 0; i < mContacts.size(); ++ i){
            mContacts.get(i).setIndex(mContacts.size() - 1 - i);
        }
        mManager.updateOrder();
    }

    public void dumpAdapter() {
        if (mContacts == null) {
            return;
        }
        int count = mContacts.size();
        for (int i = 0; i < count; i++) {
            ContactItem item = mContacts.get(i);
            log.error("contact item index ["+i+"], name ["+item.getDisplayName()+"]");
        }
    }
}

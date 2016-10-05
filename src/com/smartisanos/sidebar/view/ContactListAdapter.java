package com.smartisanos.sidebar.view;

import java.util.ArrayList;
import java.util.List;

import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarMode;
import com.smartisanos.sidebar.util.ContactItem;
import com.smartisanos.sidebar.util.ContactManager;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.Vector3f;

public class ContactListAdapter extends SidebarAdapter {
    private static float SCALE_SIZE = 1.4f;
    private static final LOG log = LOG.getInstance(ContactListAdapter.class);

    private Context mContext;
    private ContactManager mManager;
    private List<ContactItem> mContacts;
    private List<ContactItem> mAcceptableContacts = new ArrayList<ContactItem>();
    private DragEvent mDragEvent;
    private Handler mHandler;
    public boolean isEnableIconShadow = false;

    public ContactListAdapter(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mManager = ContactManager.getInstance(mContext);
        mContacts = mManager.getContactList();
        mAcceptableContacts.addAll(mContacts);
        mManager.addListener(new ContactManager.RecentUpdateListener() {
            @Override
            public void onUpdate() {
                updateData();
            }
        });
    }

    private void updateAcceptableResolveInfos() {
        mAcceptableContacts.clear();
        for (ContactItem ci : mContacts) {
            if (mDragEvent == null || ci.acceptDragEvent(mContext, mDragEvent)) {
                mAcceptableContacts.add(ci);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void moveItemPostion(Object object, int index) {
        ContactItem item = (ContactItem) object;
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
    public void updateData() {
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                mContacts = mManager.getContactList();
                updateAcceptableResolveInfos();
            }
        });
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

    private Anim mIconTouchedAnim;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        ContactItem item = mAcceptableContacts.get(position);
        if (convertView == null) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.contact_item, null);
            holder = new ViewHolder();
            holder.contactAvatar = (ImageView) view.findViewById(R.id.contact_avatar);
            holder.typeIcon = (ImageView) view.findViewById(R.id.type_icon);
            holder.displayName = (TextView) view.findViewById(R.id.display_name);
            holder.view = view;
            if (isEnableIconShadow) {
                view.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(final View view, MotionEvent motionEvent) {
                        if (view == null || motionEvent == null) {
                            return false;
                        }
                        int action = motionEvent.getAction();
                        if (action != MotionEvent.ACTION_DOWN) {
                            return false;
                        }
                        if (mIconTouchedAnim != null) {
                            mIconTouchedAnim.cancel();
                        }
                        view.setAlpha(0.4f);
                        mIconTouchedAnim = new Anim(view, Anim.TRANSPARENT, 100, Anim.CUBIC_OUT, new Vector3f(0, 0, 0.4f), new Vector3f(0, 0, 1));
                        mIconTouchedAnim.setListener(new AnimListener() {
                            @Override
                            public void onStart() {

                            }

                            @Override
                            public void onComplete(int type) {
                                if (mIconTouchedAnim != null) {
                                    view.setAlpha(1);
                                    mIconTouchedAnim = null;
                                }
                            }
                        });
                        mIconTouchedAnim.setDelay(200);
                        mIconTouchedAnim.start();
                        return false;
                    }
                });
            }
            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.restore();
        boolean isLeftMode = SidebarController.getInstance(mContext).getSidebarMode() == SidebarMode.MODE_LEFT;
        holder.setItem(item, mDragEvent != null, isLeftMode);
        Utils.setAlwaysCanAcceptDrag(holder.view, true);
        holder.view.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                ViewHolder vh = (ViewHolder) v.getTag();
                final int action = event.getAction();
                switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    FloatText.getInstance(mContext).show(holder.view, holder.mItem.getDisplayName());
                    vh.view.animate().scaleX(SCALE_SIZE).scaleY(SCALE_SIZE)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setStartDelay(0)
                    .setDuration(100).start();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    FloatText.getInstance(mContext).hide();
                    vh.view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;
                case DragEvent.ACTION_DROP:
                    vh.view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    boolean ret =  vh.mItem.handleDragEvent(mContext, event);
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

        public void setItem(ContactItem item, boolean dragging, boolean isLeftMode) {
            mItem = item;
            if (item == null) {
                return;
            }
            typeIcon.setImageResource(item.getTypeIcon());
            displayName.setText(item.getDisplayName());
            contactAvatar.setImageDrawable(mItem.getAvatar());
            if(dragging) {
                contactAvatar.setScaleX(0.8f);
                contactAvatar.setScaleY(0.8f);
                displayName.setVisibility(View.VISIBLE);
            } else {
                contactAvatar.setScaleX(1);
                contactAvatar.setScaleY(1);
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

package com.smartisanos.sidebar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.CopyHistoryItem;
import android.content.res.Configuration;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.RecentClipManager;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimStatusManager;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.Vector3f;
import com.smartisanos.sidebar.view.ContentView.ContentType;

import java.util.ArrayList;
import java.util.List;

import smartisanos.util.SidebarUtils;

public class ClipboardViewGroup extends RoundCornerFrameLayout implements IEmpty, ContentView.ISubView {
    private static final LOG log = LOG.getInstance(ClipboardViewGroup.class);

    private ContentView mContentView;
    private View mClearClipboard;
    private ListView mClipList;
    private ClipboardAdapter mClipboardAdapter;

    private View mContainer;
    private TextView mTitle;

    private EmptyView mEmptyView;
    private boolean mIsEmpty = true;
    private Context mContext;

    public ClipboardViewGroup(Context context) {
        this(context, null);
    }

    public ClipboardViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipboardViewGroup(Context context, AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ClipboardViewGroup(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    public void setContentView(ContentView cv){
        mContentView = cv;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mEmptyView = (EmptyView) findViewById(R.id.empty_view);
        mEmptyView.setImageView(R.drawable.clipboard_blank);
        mEmptyView.setText(R.string.clipboard_empty_text);
        mEmptyView.setHint(R.string.clipboard_empty_hint);

        mContainer = findViewById(R.id.clipboard_container);
        mTitle = (TextView) findViewById(R.id.clipboard_title);
        mClearClipboard = findViewById(R.id.clear);
        mClipList = (ListView) findViewById(R.id.clipboard_listview);
        mClipboardAdapter = new ClipboardAdapter(mContext, this);
        mClipList.setAdapter(mClipboardAdapter);
        mClipList.setOnItemClickListener(mOnClipBoardItemClickListener);
        mClipList.setOnItemLongClickListener(mOnClipBoardItemLongClickListener);
        mClearClipboard.setOnClickListener(mClearListener);
    }

    private ClearListener mClearListener = new ClearListener(new Runnable() {
        @Override
        public void run() {
            Anim clearContentViewAnim = new Anim(ClipboardViewGroup.this, Anim.TRANSPARENT, 200, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());
            int width = mClipList.getWidth();
            Anim clipListAnim = new Anim(mClipList, Anim.TRANSLATE, 125, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(width, 0));
            AnimTimeLine timeLine = new AnimTimeLine();
            timeLine.addAnim(clipListAnim);
            timeLine.addAnim(clearContentViewAnim);
            timeLine.setAnimListener(new AnimListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onComplete(int type) {
                    mClipList.setX(0);
                    ClipboardViewGroup.this.setAlpha(1);
                    RecentClipManager.getInstance(mContext).clear();
                    ClipboardViewGroup.this.setVisibility(View.GONE);
                }
            });
            timeLine.start();
            SidebarController.getInstance(mContext).resumeTopView();
            mContentView.setCurrent(ContentType.NONE);
        }
    }, R.string.title_confirm_delete_history_clipboard);

    @Override
    public void setEmpty(boolean isEmpty) {
        if (mIsEmpty != isEmpty) {
            mIsEmpty = isEmpty;
            if (mIsEmpty) {
                mContainer.setVisibility(GONE);
                mEmptyView.setVisibility(VISIBLE);
            } else {
                mContainer.setVisibility(VISIBLE);
                mEmptyView.setVisibility(GONE);
            }
        }
    }

    public void show(boolean anim) {
        mClipList.setSelectionAfterHeaderView();
        mClipList.requestLayout();
        post(new Runnable() {
            @Override
            public void run() {
                RecentClipManager.getInstance(mContext).refresh();
            }
        });
        setVisibility(View.VISIBLE);
        if (anim) {
            int time = 200;
            AnimTimeLine timeLine = new AnimTimeLine();
            final View view;
            if (mIsEmpty) {
                view = mEmptyView;
            } else {
                view = mClipList;
            }
            int height = view.getHeight();
            view.setPivotY(0);
            Anim moveAnim = new Anim(view, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(0, -height / 2), new Vector3f());
            moveAnim.setListener(new AnimListener() {
                @Override
                public void onStart() {
                }
                @Override
                public void onComplete(int type) {
                    view.setTranslationY(0);
                }
            });
            timeLine.addAnim(moveAnim);
            setPivotY(0);
            Anim scaleAnim = new Anim(this, Anim.SCALE, time, Anim.CUBIC_OUT, new Vector3f(0, 0.6f), new Vector3f(0, 1));
            timeLine.addAnim(scaleAnim);
            timeLine.setAnimListener(new AnimListener() {
                @Override
                public void onStart() {
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_CLIPBOARD_LIST_ANIM, true);
                }

                @Override
                public void onComplete(int type) {
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_CLIPBOARD_LIST_ANIM, false);
                    mClipList.setTranslationX(0);
                    setScaleY(1);
                }
            });
            timeLine.start();
        }
    }

    public void dismiss(boolean anim) {
        mClearListener.dismiss();
        if (anim) {
            AnimTimeLine timeLine = new AnimTimeLine();
            final View view;
            if (mIsEmpty) {
                view = mEmptyView;
            } else {
                view = mClipList;
            }
            view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            view.setPivotY(0);
            Anim alphaAnim = new Anim(view, Anim.TRANSPARENT, 200, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());
            Anim scaleAnim = new Anim(view, Anim.SCALE, 200, Anim.CUBIC_OUT, new Vector3f(1, 1), new Vector3f(1, 0.6f));
            timeLine.addAnim(alphaAnim);
            timeLine.addAnim(scaleAnim);
            timeLine.setAnimListener(new AnimListener() {
                @Override
                public void onStart() {
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_CLIPBOARD_LIST_ANIM, true);
                }

                @Override
                public void onComplete(int type) {
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_CLIPBOARD_LIST_ANIM, false);
                    view.setScaleY(1);
                    view.setAlpha(1);
                    setVisibility(View.GONE);
                }
            });
            timeLine.start();
        } else {
            setVisibility(View.GONE);
        }
    }

    private Toast mClipboardCopyToast = null;
    private AdapterView.OnItemClickListener mOnClipBoardItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (mClipboardAdapter == null) {
                return;
            }
            Object obj = mClipboardAdapter.getItem(position);
            if (obj == null) {
                return;
            }
            if (obj instanceof ClipboardAdapter.DataItem) {
                ClipboardAdapter.DataItem item = (ClipboardAdapter.DataItem) obj;
                String text = item.mText;
                Utils.copyText(mContext, text, false);
                Utils.resumeSidebar(mContext);
                if (mClipboardCopyToast != null) {
                    mClipboardCopyToast.cancel();
                }
                mClipboardCopyToast = Toast.makeText(mContext, R.string.text_copied, Toast.LENGTH_SHORT);
                mClipboardCopyToast.show();
            } else if (obj instanceof ArrayList) {
                List<ClipboardAdapter.DataItem> list = (List<ClipboardAdapter.DataItem>) obj;
                mClipboardAdapter.addItems(position, list);
                mClipboardAdapter.removeItem(obj);
                mClipboardAdapter.notifyDataSetChanged();
            }
        }
    };

    private AdapterView.OnItemLongClickListener mOnClipBoardItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (view == null) {
                log.info("onItemLongClick view is null ["+position+"] ["+id+"]");
                return false;
            }
            if (mClipboardAdapter == null) {
                return false;
            }
            Object obj = mClipboardAdapter.getItem(position);
            if (obj != null && obj instanceof ClipboardAdapter.DataItem) {
                ClipboardAdapter.DataItem item = (ClipboardAdapter.DataItem) obj;
                SidebarUtils.dragText(view, mContext, item.mText);
            }
            return false;
        }
    };

    private void updateUI(){
        mTitle.setText(R.string.title_clipboard);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateUI();
        mClearListener.onConfigurationChanged(newConfig);
    }
}

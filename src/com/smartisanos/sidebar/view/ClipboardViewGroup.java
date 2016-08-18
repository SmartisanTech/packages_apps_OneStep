package com.smartisanos.sidebar.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ScrollView;
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
        mClipList = (ListView)findViewById(R.id.clipboard_listview);
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
        setVisibility(View.VISIBLE);
        if (anim) {
            int time = 200;
            AnimTimeLine timeLine = new AnimTimeLine();
            if (mIsEmpty) {
                int height = mEmptyView.getHeight();
                mEmptyView.setPivotY(0);
                Anim moveAnim = new Anim(mEmptyView, Anim.TRANSLATE, time, Anim.CUBIC_OUT, new Vector3f(0, -height), new Vector3f());
                moveAnim.setListener(new AnimListener() {
                    @Override
                    public void onStart() {
                    }
                    @Override
                    public void onComplete(int type) {
                        mEmptyView.setY(0);
                    }
                });
                timeLine.addAnim(moveAnim);
            } else {
                int height = mClipList.getHeight();
                mClipList.setPivotY(0);
                Anim moveAnim = new Anim(mClipList, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(0, -height), new Vector3f());
                timeLine.addAnim(moveAnim);
            }
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
            log.info("onItemClick [" + position + "] [" + id + "]");
            CopyHistoryItem item = (CopyHistoryItem) mClipboardAdapter.getItem(position);
            CharSequence text = item.mContent;
            Utils.copyText(mContext, text, false);
            if (mClipboardCopyToast != null) {
                mClipboardCopyToast.cancel();
            }
            mClipboardCopyToast = Toast.makeText(mContext, R.string.text_copied, Toast.LENGTH_SHORT);
            mClipboardCopyToast.getWindowParams().type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
            mClipboardCopyToast.getWindowParams().token = view.getWindowToken();
            mClipboardCopyToast.show();
        }
    };

    private AdapterView.OnItemLongClickListener mOnClipBoardItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            log.info("onItemLongClick ["+position+"] ["+id+"]");
            if (view == null) {
                log.info("onItemLongClick view is null ["+position+"] ["+id+"]");
                return false;
            }
            TextView textView = (TextView) view.findViewById(R.id.text);
            SidebarUtils.dragText(textView, mContext, textView.getText());
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

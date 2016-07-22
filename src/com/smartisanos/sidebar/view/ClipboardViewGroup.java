package com.smartisanos.sidebar.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
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
import com.smartisanos.sidebar.util.anim.ExpandableCollapsedTextViewHeightAnim;
import com.smartisanos.sidebar.util.anim.Vector3f;
import com.smartisanos.sidebar.view.ContentView.ContentType;

import smartisanos.util.SidebarUtils;

public class ClipboardViewGroup extends RoundCornerFrameLayout implements IEmpty, ContentView.ISubView {
    private static final LOG log = LOG.getInstance(ClipboardViewGroup.class);

    private ContentView mContentView;
    private View mClearClipboard;
    private ListView mClipList;
    private ClipboardAdapter mClipboardAdapter;
    private FrameLayout mClipboardContentArea;
    private TextView mClipboardFullText;
    private ScrollView mClipboardFullTextScrollView;
    private Button mClipboardCopyItemButton;
    private TextView mClipboardItemBack;

    private View mClipboardListTitle;
    private View mClipboardItemTitle;

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

        mClipboardItemBack = (TextView) findViewById(R.id.back_button);
        mClipboardCopyItemButton = (Button) findViewById(R.id.copy_icon);
        mClipboardListTitle = findViewById(R.id.clipboard_list_title);
        mClipboardItemTitle = findViewById(R.id.clipboard_item_title);
        mClipboardContentArea = (FrameLayout) findViewById(R.id.clipboard_content_area);
        mClipboardFullTextScrollView = (ScrollView) findViewById(R.id.full_text_scroll_view);
        mClipboardFullText = (TextView) findViewById(R.id.clipboard_full_content);

        mClearClipboard.setOnClickListener(mClearListener);
        mClipboardItemBack.setOnClickListener(mOnClickClipboardFullTextTitleBackButton);
        mClipboardCopyItemButton.setOnClickListener(mOnClickClipboardFullTextTitleCopyButton);

        mClipboardFullText.setOnLongClickListener(mOnClipBoardFullTextItemLongClickListener);
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
                Anim moveAnim = new Anim(mClipList, Anim.TRANSLATE, time, Anim.CUBIC_OUT, new Vector3f(0, -height), new Vector3f());
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
                    mClipList.setY(0);
                    setScaleY(1);
                }
            });
            timeLine.start();
        }
    }

    public void dismiss(boolean anim) {
        mClearListener.dismiss();
        if (anim) {
            boolean isFullTextShown = false;
            if (mClipboardFullTextScrollView != null
                    && mClipboardFullTextScrollView.getVisibility() == View.VISIBLE) {
                isFullTextShown = true;
            }
            AnimTimeLine timeLine = new AnimTimeLine();
            final View view;
            if (mIsEmpty) {
                view = mEmptyView;
            } else if (isFullTextShown) {
                view = mClipboardFullTextScrollView;
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
                    resetClipboardFullTextStatus();
                    setVisibility(View.GONE);
                }
            });
            timeLine.start();
        } else {
            resetClipboardFullTextStatus();
            setVisibility(View.GONE);
        }
    }

    private void resetClipboardFullTextStatus() {
        mClipboardItemTitle.setVisibility(View.GONE);
        mClipboardListTitle.setVisibility(View.VISIBLE);

        mClipboardFullText.setText("");
        mClipList.setVisibility(View.VISIBLE);
        mClipboardFullTextScrollView.setVisibility(View.GONE);

        mClipList.setTranslationX(0);
        mClipList.setTranslationY(0);
        clipboardItemClicked = false;
        switchViewAnimRunning = false;
        mEnableClipboardFullTextTitleBackButton = false;
    }

    private boolean clipboardItemClicked = false;
    private boolean switchViewAnimRunning = false;

    private AdapterView.OnItemClickListener mOnClipBoardItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Log.i("Sidebar", "onItemClick [" + position + "] [" + id + "]");
            CopyHistoryItem item = (CopyHistoryItem) mClipboardAdapter.getItem(position);
            if (item == null) {
                Log.i("Sidebar", "onItemClick lose CopyHistoryItem at [" + position + "]");
                return;
            }
            if (clipboardItemClicked) {
                return;
            }
            clipboardItemClicked = true;
            String content = item.mContent;
            mClipboardFullText.setText(content);
            showClipboardFullTextWithAnim();
        }
    };

    private static int getTextViewRealHeight(TextView view) {
        int textHeight = view.getLayout().getLineTop(view.getLineCount());
        int padding = view.getCompoundPaddingTop() + view.getCompoundPaddingBottom();
        return textHeight + padding;
    }

    private AdapterView.OnItemLongClickListener mOnClipBoardItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            Log.i("Sidebar", "onItemLongClick ["+position+"] ["+id+"]");
            if (view == null) {
                Log.i("Sidebar", "onItemLongClick view is null ["+position+"] ["+id+"]");
                return false;
            }
            TextView textView = (TextView) view.findViewById(R.id.text);
            Utils.dismissAllDialog(mContext);
            SidebarUtils.dragText(textView, mContext, textView.getText());
            return false;
        }
    };
    private ExpandableCollapsedTextViewHeightAnim animation = new ExpandableCollapsedTextViewHeightAnim();
    private int horizontalScrollTime = 200;
    private int expandableOrCollapsedTime = 250;

    private OnLongClickListener mOnClipBoardFullTextItemLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            if (view == null) {
                return false;
            }
            if (mClipboardFullText == null) {
                return false;
            }
            if (mClipboardFullTextScrollView.getVisibility() != VISIBLE) {
                return false;
            }
            CharSequence text = mClipboardFullText.getText();
            Utils.dismissAllDialog(mContext);
            SidebarUtils.dragText(mClipboardFullText, mContext, text);
            return true;
        }
    };

    private boolean mEnableClipboardFullTextTitleBackButton = false;

    private View.OnClickListener mOnClickClipboardFullTextTitleBackButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (switchViewAnimRunning) {
                return;
            }
            if (!mEnableClipboardFullTextTitleBackButton) {
                return;
            }
            hideClipboardFullTextWithAnim();
        }
    };

    private Toast mClipboardCopyToast = null;

    private View.OnClickListener mOnClickClipboardFullTextTitleCopyButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CharSequence text = mClipboardFullText.getText();
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

    private void showClipboardFullTextWithAnim() {
        if (switchViewAnimRunning) {
            return;
        }
        switchViewAnimRunning = true;

        mClipboardListTitle.setVisibility(View.GONE);
        mClipboardItemTitle.setVisibility(View.VISIBLE);

        Rect rect = new Rect();
        mClipList.getDrawingRect(rect);
        int viewWidth = mClipList.getWidth();
        final int viewHeight = (rect.bottom - rect.top);//mClipList.getHeight();
        mClipboardFullTextScrollView.setTranslationX(-viewWidth);
        mClipboardFullTextScrollView.getLayoutParams().height = viewHeight;
        mClipboardFullTextScrollView.requestLayout();
        mClipboardFullTextScrollView.setVisibility(View.VISIBLE);

        mClipboardFullTextScrollView.setVerticalScrollBarEnabled(false);

        AnimatorSet set = new AnimatorSet();
        ObjectAnimator listAnim = ObjectAnimator.ofFloat(mClipList, Anim.TRANSLATE_X, 0, -viewWidth);
        ObjectAnimator textAnim = ObjectAnimator.ofFloat(mClipboardFullTextScrollView, Anim.TRANSLATE_X, viewWidth, 0);
        set.play(listAnim).with(textAnim);
        set.setDuration(horizontalScrollTime);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mClipList.setVisibility(View.GONE);
                switchViewAnimRunning = false;
                int from = viewHeight;
                int to = getTextViewRealHeight(mClipboardFullText);
                if (to > mContentView.getHeight()) {
                    to = mContentView.getHeight();
                }
                if (from != to) {
                    //do expandable or Collapsed anim
                    animation.init(mClipboardFullTextScrollView, from, to, expandableOrCollapsedTime);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mEnableClipboardFullTextTitleBackButton = true;
                            switchViewAnimRunning = false;
                            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.gravity = Gravity.TOP;
                            mClipboardContentArea.updateViewLayout(mClipboardFullTextScrollView, params);
                            mClipboardFullTextScrollView.setVerticalScrollBarEnabled(true);
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    animation.setStartTime(50);
                    animation.start();
                    switchViewAnimRunning = true;
                } else {
                    mEnableClipboardFullTextTitleBackButton = true;
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        set.start();
    }

    private void hideClipboardFullTextWithAnim() {
        if (switchViewAnimRunning) {
            return;
        }
        switchViewAnimRunning = true;
        mEnableClipboardFullTextTitleBackButton = false;
        mClipboardItemTitle.setVisibility(View.GONE);
        mClipboardFullTextScrollView.setVerticalScrollBarEnabled(false);
        Rect rect = new Rect();
        mClipList.getDrawingRect(rect);
        final int viewWidth = mClipList.getWidth();
        final int viewHeight = (rect.bottom - rect.top);
        int from = mClipboardFullTextScrollView.getHeight();
        animation.init(mClipboardFullTextScrollView, from, viewHeight, expandableOrCollapsedTime);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mClipList.setTranslationX(viewWidth);
                mClipList.setVisibility(View.VISIBLE);
                mClipList.requestLayout();

                AnimatorSet set = new AnimatorSet();
                ObjectAnimator listAnim = ObjectAnimator.ofFloat(mClipList, Anim.TRANSLATE_X, -viewWidth, 0);
                ObjectAnimator textAnim = ObjectAnimator.ofFloat(mClipboardFullTextScrollView, Anim.TRANSLATE_X, 0, viewWidth);
                set.play(listAnim).with(textAnim);
                set.setDuration(horizontalScrollTime);
                set.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mClipboardListTitle.setVisibility(View.VISIBLE);
                        mClipboardFullTextScrollView.setVerticalScrollBarEnabled(true);
                        mClipboardFullTextScrollView.setVisibility(View.GONE);
                        switchViewAnimRunning = false;
                        clipboardItemClicked = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
                set.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animation.start();
        switchViewAnimRunning = true;
    }

    private void updateUI(){
        mTitle.setText(R.string.title_clipboard);
        mClipboardItemBack.setText(R.string.clipboard_fulltext_back);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateUI();
    }
}

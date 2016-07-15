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
import android.view.View.OnLongClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.CopyHistoryItem;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.IClear;
import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.RecentClipManager;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimUtils;
import com.smartisanos.sidebar.util.anim.ExpandableCollapsedTextViewHeightAnim;
import com.smartisanos.sidebar.view.ContentView.ContentType;

import smartisanos.util.SidebarUtils;

public class ClipboardViewGroup extends FrameLayout implements IEmpty {

    private ContentView mContentView;
    private View mClearClipboard;
    private ListView mClipList;
    private ClipboardAdapter mClipboardAdapter;
    private FrameLayout mClipboardContentArea;
    private TextView mClipboardFullText;
    private ScrollView mClipboardFullTextScrollView;
    private Button mClipboardCopyItemButton;
    private LinearLayout mClipboardItemBack;

    private LinearLayout mClipboardListTitle;
    private LinearLayout mClipboardItemTitle;

    private View mContainer;
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
        mClearClipboard = findViewById(R.id.clear);
        mClearClipboard.setOnClickListener(new ClearListener(new Runnable() {
            @Override
            public void run() {
                mClipList.setLayoutAnimation(AnimUtils.getClearLayoutAnimationForListView());
                mClipList.startLayoutAnimation();
                startAnimation(AnimUtils.getClearAnimationForContainer(ClipboardViewGroup.this, RecentClipManager.getInstance(mContext)));
                SidebarController.getInstance(mContext).resumeTopView();
                mContentView.setCurrent(ContentType.NONE);
            }
        }));
        mClipList = (ListView)findViewById(R.id.clipboard_listview);
        mClipboardAdapter = new ClipboardAdapter(mContext, this);
        mClipList.setAdapter(mClipboardAdapter);

        mClipList.setOnItemClickListener(mOnClipBoardItemClickListener);
        mClipList.setOnItemLongClickListener(mOnClipBoardItemLongClickListener);

        mClipboardItemBack = (LinearLayout) findViewById(R.id.back_button);
        mClipboardCopyItemButton = (Button) findViewById(R.id.copy_icon);
        mClipboardListTitle = (LinearLayout) findViewById(R.id.clipboard_list_title);
        mClipboardItemTitle = (LinearLayout) findViewById(R.id.clipboard_item_title);
        mClipboardContentArea = (FrameLayout) findViewById(R.id.clipboard_content_area);
        mClipboardFullTextScrollView = (ScrollView) findViewById(R.id.full_text_scroll_view);
        mClipboardFullText = (TextView) findViewById(R.id.clipboard_full_content);

        mClearClipboard.setOnClickListener(new ClearListener(new Runnable() {
            @Override
            public void run() {
                mClipList.setLayoutAnimation(AnimUtils.getClearLayoutAnimationForListView());
                mClipList.startLayoutAnimation();
                startAnimation(AnimUtils.getClearAnimationForContainer(ClipboardViewGroup.this, RecentClipManager.getInstance(mContext)));
                SidebarController.getInstance(mContext).resumeTopView();
                mContentView.setCurrent(ContentType.NONE);
            }
        }));

        mClipboardItemBack.setOnClickListener(mOnClickClipboardFullTextTitleBackButton);
        mClipboardCopyItemButton.setOnClickListener(mOnClickClipboardFullTextTitleCopyButton);

        mClipboardFullText.setOnLongClickListener(mOnClipBoardFullTextItemLongClickListener);
    }

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
        if (anim) {
            if (mIsEmpty) {
                mClipList.setLayoutAnimation(AnimUtils.getEnterLayoutAnimationForListView());
                mClipList.startLayoutAnimation();
            }
            startAnimation(AnimUtils.getEnterAnimationForContainer(this));
        } else {
            setVisibility(View.VISIBLE);
        }
    }

    public void dismiss(boolean anim) {
        resetClipboardFullTextStatus();
        if (anim) {
            if (mIsEmpty) {
                mClipList.setLayoutAnimation(AnimUtils.getExitLayoutAnimationForListView());
                mClipList.startLayoutAnimation();
            }
            startAnimation(AnimUtils.getExitAnimationForContainer(this));
        } else {
            setVisibility(View.INVISIBLE);
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
    private int expandableOrCollapsedTime = 200;

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
        Rect rect = new Rect();
        mClipList.getDrawingRect(rect);
        final int viewWidth = mClipList.getWidth();
        final int viewHeight = (rect.bottom - rect.top);//mClipList.getHeight();
        int from = mClipboardFullText.getHeight();
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
}

package com.smartisanos.sidebar.view;

import smartisanos.app.MenuDialog;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.CopyHistoryItem;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.IClear;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.RecentClipManager;
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.RecentPhotoManager;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.ExpandableCollapsedTextViewHeightAnim;

import smartisanos.util.SidebarUtils;

public class ContentView extends RelativeLayout {
    private static final LOG log = LOG.getInstance(ContentView.class);

    public enum ContentType{
        NONE,
        PHOTO,
        FILE,
        CLIPBOARD,
        ADDTOSIDEBAR
    }

    private ViewStub mViewStubAddToSidebar;

    private View mPhotoContainer, mFileContainer, mClipboardContainner;
    private RecentPhotoGridView mPhotos;
    private ListView mRecentFileList;
    private ListView mClipList;

    private View mClearPhoto, mClearFile, mClearClipboard;
    private ClipboardAdapter mClipboardAdapter;
    private TextView mClipboardTitle;
    private TextView mClipboardFullText;
    private Button mClipboardCopyItemButton;
    private Button mClipboardShareItemButton;
    private LinearLayout mClipboardItemBack;

    private LinearLayout mClipboardListTitle;
    private LinearLayout mClipboardItemTitle;

    private Context mViewContext;

    private ContentType mCurType = ContentType.NONE;

    // add content related
    private View mAddContainner;

    public ContentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ContentView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ContentType getCurrentContent(){
        return mCurType;
    }

    public void show(ContentType ct, boolean anim) {
        if (mCurType != ContentType.NONE) {
            return;
        }
        setVisibility(View.VISIBLE);
        SidebarController.getInstance(mContext).addContentView();
        mCurType = ct;
        this.animate().alpha(1.0f).setDuration(ANIMATION_DURA).start();
        switch (ct) {
        case PHOTO:
            if (anim) {
                mPhotos.setLayoutAnimation(getEnterLayoutAnimationForListView());
                mPhotos.startLayoutAnimation();
                mPhotoContainer.startAnimation(getEnterAnimationForContainer(mPhotoContainer));
            } else {
                mPhotoContainer.setVisibility(View.VISIBLE);
            }
            break;
        case FILE:
            if (anim) {
                mRecentFileList.setLayoutAnimation(getEnterLayoutAnimationForListView());
                mRecentFileList.startLayoutAnimation();
                mFileContainer.startAnimation(getEnterAnimationForContainer(mFileContainer));
            } else {
                mFileContainer.setVisibility(View.VISIBLE);
            }
            break;
        case CLIPBOARD:
            if (anim) {
                mClipList.setLayoutAnimation(getEnterLayoutAnimationForListView());
                mClipList.startLayoutAnimation();
                mClipboardContainner.startAnimation(getEnterAnimationForContainer(mClipboardContainner));
            } else {
                mClipboardContainner.setVisibility(View.VISIBLE);
            }
            break;
        case ADDTOSIDEBAR:
            if(mAddContainner == null){
                initAddToSidebar();
            }
            if(anim){
                mAddContainner.startAnimation(getEnterAnimationForContainer(mAddContainner));
            }else{
                mAddContainner.setVisibility(View.VISIBLE);
            }
            break;
        default:
            break;
        }
    }

    public void dismiss(ContentType ct, boolean anim) {
        if (mCurType != ct) {
            return;
        }
        mCurType = ContentType.NONE;
        if(anim){
            this.animate().alpha(0.0f).setDuration(ANIMATION_DURA).start();
        }else{
            this.setAlpha(0.0f);
        }
        switch (ct) {
        case PHOTO:
            if (anim) {
                mPhotos.setLayoutAnimation(getExitLayoutAnimationForListView());
                mPhotos.startLayoutAnimation();
                mPhotoContainer.startAnimation(getExitAnimationForContainer(mPhotoContainer));
            } else {
                mPhotoContainer.setVisibility(View.INVISIBLE);
            }
            break;
        case FILE:
            if (anim) {
                mRecentFileList.setLayoutAnimation(getExitLayoutAnimationForListView());
                mRecentFileList.startLayoutAnimation();
                mFileContainer.startAnimation(getExitAnimationForContainer(mFileContainer));
            } else {
                mFileContainer.setVisibility(View.INVISIBLE);
            }
            break;
        case CLIPBOARD:
            resetClipboardFullTextStatus();
            if (anim) {
                mClipList.setLayoutAnimation(getExitLayoutAnimationForListView());
                mClipList.startLayoutAnimation();
                mClipboardContainner.startAnimation(getExitAnimationForContainer(mClipboardContainner));
            } else {
                mClipboardContainner.setVisibility(View.INVISIBLE);
            }
            break;
        case ADDTOSIDEBAR:
            if(mAddContainner == null){
                initAddToSidebar();
            }
            if (anim) {
                mAddContainner.startAnimation(getExitAnimationForContainer(mAddContainner));
            } else {
                mAddContainner.setVisibility(View.INVISIBLE);
            }
            break;
        case NONE:
            break;
        default:
            break;
        }
    }

    private void initAddToSidebar(){
        if(mAddContainner == null){
            mViewStubAddToSidebar.inflate();
            mAddContainner = findViewById(R.id.addtosidebar_container);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mViewStubAddToSidebar = (ViewStub)findViewById(R.id.viewstub_addtosidebar);

        // view container
        mPhotoContainer = findViewById(R.id.photo_container);
        mFileContainer = findViewById(R.id.file_container);
        mClipboardContainner = findViewById(R.id.clipboard_container);
        mClearPhoto = mPhotoContainer.findViewById(R.id.clear);
        mClearFile = mFileContainer.findViewById(R.id.clear);
        mClearClipboard = mClipboardContainner.findViewById(R.id.clear);

        // content
        mPhotos = (RecentPhotoGridView)findViewById(R.id.recentphoto_gridview);

        mRecentFileList = (ListView)findViewById(R.id.recentfile_listview);
        mRecentFileList.setAdapter(new RecentFileAdapter(this.mContext));

        mClipList = (ListView)findViewById(R.id.clipboard_listview);
        mClipboardAdapter = new ClipboardAdapter(this.mContext);
        mClipList.setAdapter(mClipboardAdapter);

//        mClipList.setOnClickListener();
        mClipList.setOnItemClickListener(mOnClipBoardItemClickListener);
        mClipList.setOnItemLongClickListener(mOnClipBoardItemLongClickListener);

        mClipboardTitle = (TextView) findViewById(R.id.clipboard_title);
        mClipboardItemBack = (LinearLayout) findViewById(R.id.back_button);
        mClipboardCopyItemButton = (Button) findViewById(R.id.copy_icon);
        mClipboardShareItemButton = (Button) findViewById(R.id.share_icon);
        mClipboardListTitle = (LinearLayout) findViewById(R.id.clipboard_list_title);
        mClipboardItemTitle = (LinearLayout) findViewById(R.id.clipboard_item_title);
        mClipboardFullText = (TextView) findViewById(R.id.clipboard_full_content);

        mClearPhoto.setOnClickListener(new ClearListener(new Runnable(){
            @Override
            public void run() {
                mPhotos.setLayoutAnimation(getClearLayoutAnimationForListView());
                mPhotos.startLayoutAnimation();
                mPhotoContainer.startAnimation(getClearAnimationForContainer(mPhotoContainer, RecentPhotoManager.getInstance(mContext)));
                SidebarController.getInstance(mContext).resumeTopView();
                mCurType = ContentType.NONE;
            }
        }));

        mClearFile.setOnClickListener(new ClearListener(new Runnable() {
            @Override
            public void run() {
                mRecentFileList.setLayoutAnimation(getClearLayoutAnimationForListView());
                mRecentFileList.startLayoutAnimation();
                mFileContainer.startAnimation(getClearAnimationForContainer(mFileContainer, RecentFileManager.getInstance(mContext)));
                SidebarController.getInstance(mContext).resumeTopView();
                mCurType = ContentType.NONE;
            }
        }));

        mClearClipboard.setOnClickListener(new ClearListener(new Runnable() {
            @Override
            public void run() {
                mClipList.setLayoutAnimation(getClearLayoutAnimationForListView());
                mClipList.startLayoutAnimation();
                mClipboardContainner.startAnimation(getClearAnimationForContainer(mClipboardContainner, RecentClipManager.getInstance(mContext)));
                SidebarController.getInstance(mContext).resumeTopView();
                mCurType = ContentType.NONE;
            }
        }));

        mClipboardItemBack.setOnClickListener(mOnClickClipboardFullTextTitleBackButton);
        mClipboardCopyItemButton.setOnClickListener(mOnClickClipboardFullTextTitleCopyButton);
        mClipboardShareItemButton.setOnClickListener(mOnClickClipboardFullTextTitleShareButton);

        mClipboardFullText.setOnLongClickListener(mOnClipBoardFullTextItemLongClickListener);
    }

    private static final int ANIMATION_DURA = 314;
    private LayoutAnimationController getEnterLayoutAnimationForListView(){
        Animation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        anim.setDuration(ANIMATION_DURA);
        LayoutAnimationController controller = new LayoutAnimationController(anim, 0);
        return controller;
    }

    private LayoutAnimationController getExitLayoutAnimationForListView(){
        Animation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
        anim.setDuration(ANIMATION_DURA);
        LayoutAnimationController controller = new LayoutAnimationController(anim, 0);
        return controller;
    }

    private LayoutAnimationController getClearLayoutAnimationForListView(){
        Animation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        anim.setDuration(ANIMATION_DURA / 2);
        anim.setFillAfter(true);
        LayoutAnimationController controller = new LayoutAnimationController(anim, 0.125f);
        controller.setOrder(LayoutAnimationController.ORDER_REVERSE);
        return controller;
    }

    private Animation getClearAnimationForContainer(View container, IClear clear){
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(ANIMATION_DURA);
        anim.setAnimationListener(new DismissAnimationListener(container, clear));
        return anim;
    }

    private Animation getEnterAnimationForContainer(View container){
        Animation scaleAnim = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f);
        scaleAnim.setDuration(ANIMATION_DURA);
        scaleAnim.setAnimationListener(new ShowAnimationListener(container));
        return scaleAnim;
    }

    private Animation getExitAnimationForContainer(View container){
        Animation scaleAnim = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f);
        scaleAnim.setDuration(ANIMATION_DURA);
        scaleAnim.setAnimationListener(new DismissAnimationListener(container));
        return scaleAnim;
    }

    private static final class ShowAnimationListener implements Animation.AnimationListener{

        private View view;
        public ShowAnimationListener(View view){
            this.view = view;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            view.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            //NA
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            //NA
        }
    }

    private final class DismissAnimationListener implements Animation.AnimationListener{

        private View view;
        private IClear clear;

        public DismissAnimationListener(View view){
            this.view = view;
        }

        public DismissAnimationListener(View view, IClear clear){
            this.view = view;
            this.clear = clear;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            //NA
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            view.setVisibility(View.INVISIBLE);
            if(clear != null){
                clear.clear();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            //NA
        }
    }

    @Override
    protected void onChildVisibilityChanged(View child, int oldVisibility,
            int newVisibility) {
        super.onChildVisibilityChanged(child, oldVisibility, newVisibility);
        if(newVisibility != View.VISIBLE){
            int count = getChildCount();
            for(int i = 0;i < count; ++ i){
                if(getChildAt(i).getVisibility() == View.VISIBLE){
                    // do nothing
                    return ;
                }
            }
            setVisibility(View.INVISIBLE);
            SidebarController.getInstance(mContext).resumeTopView();
            SidebarController.getInstance(mContext).removeContentView();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            Utils.resumeSidebar(mContext);
            return true;
        default:
            break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_BACK:
            boolean isUp = event.getAction() == KeyEvent.ACTION_UP;
            if (isUp && getCurrentContent() != ContentType.NONE) {
                Utils.resumeSidebar(mContext);
            }
            break;
        default:
            break;
        }
        return super.dispatchKeyEvent(event);
    }

    class ClearListener implements View.OnClickListener {
        private Runnable action;
        public ClearListener(Runnable action) {
            this.action = action;
        }

        @Override
        public void onClick(View v) {
            MenuDialog dialog = new MenuDialog(mContext);
            dialog.setTitle(R.string.title_confirm_delete_history);
            dialog.setPositiveButton(R.string.delete, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    action.run();
                }
            });
            dialog.getWindow().getAttributes().type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
            dialog.getWindow().getAttributes().token = getWindowToken();
            dialog.show();
        }
    }

    private void resetClipboardFullTextStatus() {
        mClipboardItemTitle.setVisibility(View.GONE);
        mClipboardListTitle.setVisibility(View.VISIBLE);

        mClipboardFullText.setText("");
        mClipList.setVisibility(View.VISIBLE);
        mClipboardFullText.setVisibility(View.GONE);

        mClipList.setTranslationX(0);
        mClipList.setTranslationY(0);
        clipboardItemClicked = false;
        switchViewAnimRunning = false;
        mEnableClipboardFullTextTitleBackButton = false;
    }

    private ExpandableCollapsedTextViewHeightAnim animation = new ExpandableCollapsedTextViewHeightAnim();
    private int horizontalScrollTime = 200;
    private int expandableOrCollapsedTime = 200;

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
                log.error("clipboardItemClicked true");
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

    private OnLongClickListener mOnClipBoardFullTextItemLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            if (view == null) {
                return false;
            }
            if (mClipboardFullText == null) {
                return false;
            }
            if (mClipboardFullText.getVisibility() != View.VISIBLE) {
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
            log.error("onClick mOnClickClipboardFullTextTitleBackButton");
            if (switchViewAnimRunning) {
                log.error("switchViewAnimRunning true");
                return;
            }
            if (!mEnableClipboardFullTextTitleBackButton) {
                log.error("mEnableClipboardFullTextTitleBackButton is false");
                return;
            }
            hideClipboardFullTextWithAnim();
        }
    };

    private View.OnClickListener mOnClickClipboardFullTextTitleShareButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            log.error("onClick mOnClickClipboardFullTextTitleShareButton");
        }
    };

    private View.OnClickListener mOnClickClipboardFullTextTitleCopyButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            log.error("onClick mOnClickClipboardFullTextTitleCopyButton");
        }
    };

    private void showClipboardFullTextWithAnim() {
        if (switchViewAnimRunning) {
            log.error("showClipboardFullTextWithAnim return by anim is running");
            return;
        }
        switchViewAnimRunning = true;

        mClipboardListTitle.setVisibility(View.GONE);
        mClipboardItemTitle.setVisibility(View.VISIBLE);

        Rect rect = new Rect();
        mClipList.getDrawingRect(rect);
        int viewWidth = mClipList.getWidth();
        final int viewHeight = (rect.bottom - rect.top);//mClipList.getHeight();
        mClipboardFullText.setTranslationX(-viewWidth);
        mClipboardFullText.setHeight(viewHeight);
        mClipboardFullText.requestLayout();
        mClipboardFullText.setVisibility(View.VISIBLE);

        AnimatorSet set = new AnimatorSet();
        ObjectAnimator listAnim = ObjectAnimator.ofFloat(mClipList, Anim.TRANSLATE_X, 0, -viewWidth);
        ObjectAnimator textAnim = ObjectAnimator.ofFloat(mClipboardFullText, Anim.TRANSLATE_X, viewWidth, 0);
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
                log.error("mClipboardFullText height ["+from+"], text height ["+to+"]");
                if (from != to) {
                    //do expandable or Collapsed anim
                    animation.init(mClipboardFullText, from, to, expandableOrCollapsedTime);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            log.error("animation start !!!");
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mEnableClipboardFullTextTitleBackButton = true;
                            switchViewAnimRunning = false;
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
            log.error("hideClipboardFullTextWithAnim return by anim is running");
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
        log.error("hideClipboardFullTextWithAnim height from ["+from+"], to ["+viewHeight+"]");
        animation.init(mClipboardFullText, from, viewHeight, expandableOrCollapsedTime);
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
                ObjectAnimator textAnim = ObjectAnimator.ofFloat(mClipboardFullText, Anim.TRANSLATE_X, 0, viewWidth);
                set.play(listAnim).with(textAnim);
                set.setDuration(horizontalScrollTime);
                set.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mClipboardListTitle.setVisibility(View.VISIBLE);
                        mClipboardFullText.setVisibility(View.GONE);
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
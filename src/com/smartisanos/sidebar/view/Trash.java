package com.smartisanos.sidebar.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.action.UninstallAction;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.Vector3f;

public class Trash {
    private static final LOG log = LOG.getInstance(Trash.class);
    private static final boolean DBG_ANIM = false;
    static {
        if (!DBG_ANIM) log.close();
    }

    private Context mContext;
    private FrameLayout mTrashView;

    public SidebarRootView mRootView;

    private int mTrashWidth;
    private int mTrashHeight;
    private int mWindowWidth;
    private int mWindowHeight;
    private int mTrashDisplayHeight;
    private int mTrashFloatUpHeight;
    private int [] trash_react_area = new int[4];
    private int [] trash_uninstall_react_area = new int[4];


    public static final int TRASH_HIDE = 1;
    public static final int TRASH_SHOW = 2;
    public static final int TRASH_FLOAT = 3;

    public int mTrashStatus = TRASH_HIDE;

    public Trash(Context context, FrameLayout trashView) {
        mContext = context;
        mTrashView = trashView;
        Resources resources = mContext.getResources();
        mTrashWidth         = resources.getInteger(R.integer.trash_width);;
        mTrashHeight        = resources.getInteger(R.integer.trash_height);
        mTrashDisplayHeight = resources.getInteger(R.integer.trash_display_height);
        mTrashFloatUpHeight = resources.getInteger(R.integer.trash_float_up_height);
    }

    public boolean inTrashReactArea(float x, float y) {
        if (trash_react_area[0] == 0) {
            return false;
        }
        if (trash_react_area[0] < x && x < trash_react_area[2]) {
            if (y > trash_react_area[1]) {
                return true;
            }
        }
        return false;
    }

    public boolean inTrashUninstallReactArea(float x, float y) {
        if (trash_uninstall_react_area[0] == 0) {
            return false;
        }
        if (trash_uninstall_react_area[0] < x && x < trash_uninstall_react_area[2]) {
            if (y > trash_uninstall_react_area[1]) {
                return true;
            }
        }
        return false;
    }

    public void dragObjectMoveTo(float x, float y) {
        if (inTrashReactArea(x, y)) {
            //in trash area
            trashFloatUpWithAnim();
        } else {
            //out trash area
            trashFallDownWithAnim();
        }
    }

    public boolean dragObjectUpOnUp(float x, float y) {
        boolean processUninstall = false;
        log.error("dragObjectUpOnUp ["+x+"], ["+y+"]");
        if (!inTrashUninstallReactArea(x, y)) {
            return processUninstall;
        }
        SidebarRootView.DragView dragView = mRootView.getDraggedView();
        if (dragView == null) {
            log.error("dragObjectUpOnUp return by dragView is null");
            return processUninstall;
        }
        SidebarRootView.DragItem item = dragView.getDragItem();
        if (item == null) {
            log.error("dragObjectUpOnUp return by dragItem is null");
            return processUninstall;
        }
        if (item.getItemType() == SidebarRootView.DragItem.TYPE_APPLICATION && item.isSystemApp()) {
            //check is system app !
            boolean isSystemApp = false;
            if (isSystemApp) {
                Toast toast = Toast.makeText(mContext, R.string.uninstall_system_app_text, Toast.LENGTH_XLONG);
                toast.show();
            }
            log.error("dragObjectUpOnUp return by isSystemApp");
            return processUninstall;
        }
        UninstallAction action = new UninstallAction(mContext);
        action.showUninstallDialog();
        processUninstall = true;
        log.error("handle uninstall process");
        return processUninstall;
    }

    public void initTrashView() {
        mTrashView.setVisibility(View.GONE);
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int widthPixels;
        int heightPixels;
        if (metrics.heightPixels > metrics.widthPixels) {
            widthPixels = metrics.widthPixels;
            heightPixels = metrics.heightPixels;
        } else {
            widthPixels = metrics.heightPixels;
            heightPixels = metrics.widthPixels;
        }
        mWindowWidth = widthPixels;
        mWindowHeight = heightPixels;
        int locX = widthPixels / 2 - mTrashWidth / 2;
        int locY = heightPixels;
        mTrashView.setTranslationX(locX);
        mTrashView.setTranslationY(locY);
        mTrashView.setVisibility(View.VISIBLE);

        trash_react_area = new int[4];
        //left-top, right-bottom
        int left   = mWindowWidth / 2 - mTrashWidth / 2;
        int top    = mWindowHeight - mTrashHeight / 2;
        int right  = mWindowWidth / 2 + mTrashWidth / 2;
        int bottom = mWindowHeight;
        trash_react_area[0] = left;
        trash_react_area[1] = top;
        trash_react_area[2] = right;
        trash_react_area[3] = bottom;

        trash_uninstall_react_area[0] = left;
        trash_uninstall_react_area[1] = mWindowHeight - mTrashHeight;
        trash_uninstall_react_area[2] = right;
        trash_uninstall_react_area[3] = bottom;
    }

    public void hieTrashView() {
        if (mTrashView == null) {
            return;
        }
        mTrashView.setVisibility(View.GONE);
    }

    private boolean trashAppearAnimRunning = false;

    public void trashAppearWithAnim() {
        if (mTrashStatus != TRASH_HIDE) {
            return;
        }
        if (trashAppearAnimRunning) {
            log.error("trashAppearWithAnim return by trashAppearAnimRunning true");
            return;
        }
        trashAppearAnimRunning = true;
        int fromY = mWindowHeight;
        int toY = mWindowHeight - mTrashDisplayHeight;
        Vector3f from = new Vector3f(0, fromY);
        Vector3f to = new Vector3f(0, toY);
        Anim anim = new Anim(mTrashView, Anim.TRANSLATE, 200, Anim.CUBIC_OUT, from, to);
        anim.setListener(new AnimListener() {
            @Override
            public void onStart() {
            }
            @Override
            public void onComplete() {
                trashAppearAnimRunning = false;
                mTrashStatus = TRASH_SHOW;
                log.error("trashAppearWithAnim onComplete");
            }
        });
        anim.start();
    }

    private boolean trashDisappearAnimRunning = false;

    public void trashDisappearWithAnim(final SidebarRootView rootView) {
        if (mTrashStatus == TRASH_HIDE) {
            return;
        }
        if (trashDisappearAnimRunning) {
            log.error("trashDisappearWithAnim return by trashDisappearAnimRunning true");
            return;
        }
        trashDisappearAnimRunning = true;
        Vector3f from = new Vector3f(0, mTrashView.getTranslationY());
        Vector3f to = new Vector3f(0, mWindowHeight);
        Anim anim = new Anim(mTrashView, Anim.TRANSLATE, 200, Anim.CUBIC_OUT, from, to);
        anim.setListener(new AnimListener() {
            @Override
            public void onStart() {
            }
            @Override
            public void onComplete() {
                mTrashStatus = TRASH_HIDE;
                trashDisappearAnimRunning = false;
                if (rootView != null) {
                    rootView.resetSidebarWindow();
                }
            }
        });
        anim.start();
    }

    private boolean mTrashUpAnimRunning = false;
    private boolean mTrashDownAnimRunning = false;

    public void trashFloatUpWithAnim() {
        if (mTrashStatus == TRASH_FLOAT) {
            return;
        }
        if (mTrashUpAnimRunning) {
            log.error("trashFloatUpWithAnim return by mTrashUpAnimRunning true");
            return;
        }
        mTrashUpAnimRunning = true;
        int fromY = (int) mTrashView.getY();
        int toY = mWindowHeight - mTrashDisplayHeight - mTrashFloatUpHeight;
        Vector3f from = new Vector3f(0, fromY);
        Vector3f to = new Vector3f(0, toY);
        Anim anim = new Anim(mTrashView, Anim.TRANSLATE, 100, Anim.CUBIC_OUT, from, to);
        anim.setListener(new AnimListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onComplete() {
                mTrashStatus = TRASH_FLOAT;
                mTrashUpAnimRunning = false;
            }
        });
        anim.start();
    }

    public void trashFallDownWithAnim() {
        if (mTrashStatus != TRASH_FLOAT) {
            return;
        }
        if (mTrashDownAnimRunning) {
            log.error("trashFallDownWithAnim return by mTrashDownAnimRunning true");
            return;
        }
        mTrashDownAnimRunning = true;
        Vector3f from = new Vector3f(0, mTrashView.getY());
        Vector3f to = new Vector3f(0, mWindowHeight - mTrashDisplayHeight);

        Anim anim = new Anim(mTrashView, Anim.TRANSLATE, 100, Anim.CUBIC_OUT, from, to);
        anim.setListener(new AnimListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onComplete() {
                mTrashDownAnimRunning = false;
                mTrashStatus = TRASH_SHOW;
            }
        });
        anim.start();
    }
}
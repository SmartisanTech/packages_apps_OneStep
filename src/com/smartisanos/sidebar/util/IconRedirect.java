package com.smartisanos.sidebar.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.smartisanos.sidebar.R;

public class IconRedirect {
    private static final LOG log = LOG.getInstance(IconRedirect.class);

    public static boolean useRedirectIcon(String pkg, String cmp) {
        for (Component component : REDIRECT_ARR) {
            if (component.isMatch(pkg, cmp)) {
                return true;
            }
        }
        return false;
    }

    public static Drawable getRedirectIcon(String pkg, String cmp, Context context) {
        if (context == null || pkg == null || cmp == null) {
            return null;
        }
        try {
            for (Component component : REDIRECT_ARR) {
                if (component.isMatch(pkg, cmp)) {
                    Resources resources = context.getResources();
                    return resources.getDrawable(component.iconRes);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class Component {
        public String packageName;
        public String[] componentArr;
        public int iconRes;

        public Component(String pkg, String[] cmps, int icon) {
            packageName = pkg;
            componentArr = cmps;
            iconRes = icon;
        }

        public boolean isMatch(String pkg, String cmp) {
            if (pkg == null || cmp == null) {
                return false;
            }
            if (pkg.equals(packageName)) {
                if (componentArr != null) {
                    for (String str : componentArr) {
                        if (str != null && str.equals(cmp)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    public static final String WECHAT = "com.tencent.mm";
    public static final String QQ = "com.tencent.mobileqq";

    public static Component QQ_FACE_TRANSFER = new Component(QQ, new String[] {"cooperation.qlink.QlinkShareJumpActivity"}, R.drawable.redirect_icon_qq_face_transfer);
    public static Component QQ_FAVORITE = new Component(QQ, new String[] {"cooperation.qqfav.widget.QfavJumpActivity"}, R.drawable.redirect_icon_qq_favorite);
    public static Component QQ_PC = new Component(QQ, new String[] {"com.tencent.mobileqq.activity.qfileJumpActivity"}, R.drawable.redirect_icon_qq_pc);
    public static Component WECHAT_FAVORITE = new Component(WECHAT, new String[] {"com.tencent.mm.ui.tools.AddFavoriteUI"}, R.drawable.redirect_icon_wechat_favorite);
    public static Component WECHAT_SHARE_TO_TIME_LINE = new Component(WECHAT, new String[] {"com.tencent.mm.ui.tools.ShareToTimeLineUI"}, R.drawable.redirect_icon_wechat_timeline);

    public static final Component[] REDIRECT_ARR = new Component[] {
            QQ_FACE_TRANSFER, QQ_FAVORITE, QQ_PC, WECHAT_FAVORITE, WECHAT_SHARE_TO_TIME_LINE
    };
}
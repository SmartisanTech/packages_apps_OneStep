package com.smartisanos.sidebar.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;

import com.smartisanos.sidebar.util.BitmapUtils;
import com.smartisanos.sidebar.util.ContactManager;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.WechatContact;
import com.smartisanos.sidebar.R;

import java.util.ArrayList;

public class ShortcutReceiver extends BroadcastReceiver {
    private static final LOG log = LOG.getInstance(ShortcutReceiver.class);

    public static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    public static final String ACTION_UNINSTALL_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT";

    public static final String WECHAT = "com.tencent.mm";
    public static final String[] SUPPORTED_APPS = new String[] {
            WECHAT
    };

    public static boolean isSupported(String pkg) {
        for (String str : SUPPORTED_APPS) {
            if (str.equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_INSTALL_SHORTCUT.equals(action)) {
            handleInstallShortcut(context, intent);
        } else if (ACTION_UNINSTALL_SHORTCUT.equals(action)) {
            // NA
        }
    }

    //handle install shortcuts
    public static void handleInstallShortcut(Context context, Intent data) {
        Intent launchIntent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        if (launchIntent == null) {
            log.error("INSTALL_SHORTCUT failed, lose EXTRA_SHORTCUT_INTENT !");
            return;
        }
        String pkg = launchIntent.getPackage();
        if (pkg == null || !isSupported(pkg)) {
            log.error("don't support pkg ["+pkg+"]");
            return;
        }
        if (launchIntent.getAction() == null) {
            launchIntent.setAction(Intent.ACTION_VIEW);
        } else if (Intent.ACTION_MAIN.equals(launchIntent.getAction()) &&
                launchIntent.getCategories() != null &&
                launchIntent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }
        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
        int avatarSize = context.getResources().getDimensionPixelSize(R.dimen.contact_avatar_size);
        Bitmap icon = null;
        if (bitmap != null && bitmap instanceof Bitmap) {
            icon = (Bitmap) bitmap;
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof Intent.ShortcutIconResource) {
                try {
                    Intent.ShortcutIconResource iconResource = (Intent.ShortcutIconResource) extra;
                    Resources resources = context.getPackageManager().getResourcesForApplication(iconResource.packageName);
                    final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    Drawable drawable = getShortcutResIcon(resources, id);
                    if (drawable != null) {
                        icon = BitmapUtils.drawableToBitmap(drawable, avatarSize, avatarSize);
                    }
                } catch (Exception e) {
                    log.error("Could not load shortcut icon: " + extra);
                }
            }
        }

        if (icon == null) {
            icon = BitmapUtils.drawableToBitmap(getDefaultShortcutIcon(), avatarSize, avatarSize);;
            if (icon == null) {
                // this should never happen !
                return;
            }
        }

        icon = BitmapUtils.getRoundedCornerBitmap(icon);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        name = ensureValidName(context, launchIntent, name).toString();
        String intentUri = launchIntent.toUri(0);
        log.error("handleInstallShortcut ["+name+"]");
        log.error("lunch intent ["+intentUri+"]");
        WechatContact contact = new WechatContact(context, name, intentUri, icon);
        ContactManager.getInstance(context).addContact(contact);
    }

    private static CharSequence ensureValidName(Context context, Intent intent, CharSequence name) {
        if (name != null) {
            return name;
        }
        try {
            PackageManager pm = context.getPackageManager();
            ActivityInfo info = pm.getActivityInfo(intent.getComponent(), 0);
            return info.loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException nnfe) {
            // NA
        } catch (Exception e) {
            // NA
        }
        return "";
    }

    public static Drawable getShortcutResIcon(Resources resources, int iconId) {
        try {
            return resources.getDrawable(iconId);
        } catch (Resources.NotFoundException e) {
            // NA
        }
        return null;
    }

    public static Drawable getDefaultShortcutIcon() {
        try {
            return Resources.getSystem().getDrawable(
                    android.R.mipmap.sym_def_app_icon);
        } catch (NotFoundException e) {
            // NA
        }
        return null;
    }
}
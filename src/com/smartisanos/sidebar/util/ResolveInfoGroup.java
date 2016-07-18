package com.smartisanos.sidebar.util;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ClipDescription;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.DragEvent;

public class ResolveInfoGroup extends ArrayList<ResolveInfo> implements SidebarItem{
    private static final long serialVersionUID = 1L;
    private static final String TAG = ResolveInfoGroup.class.getName();

    private Context mContext;
    private SoftReference<Bitmap> mBlackWhiteIcon = null;
    private int mIndex = -1;

    public ResolveInfoGroup(Context context){
        super();
        mContext = context;
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    public int getIndex() {
        return mIndex;
    }

    public String getPackageName(){
        if(size() > 0){
            return get(0).activityInfo.packageName;
        }else{
            return null;
        }
    }

    //format : name_1|name_2| .. |name_n
    public String getComponentNames(){
        if(size() <= 0){
            return null;
        }
        List<String> ls = new ArrayList<String>();
        for(ResolveInfo ri : this){
            ls.add(ri.activityInfo.name);
        }
        Collections.sort(ls);
        StringBuilder sb = new StringBuilder();
        sb.append(ls.get(0));
        for(int i = 1; i < ls.size(); ++ i){
            sb.append("|" + ls.get(i));
        }
        return sb.toString();
    }

    public boolean valid(Context context){
        if(size() <= 0){
            return false;
        }
        for(ResolveInfo ri : this){
            try {
                context.getPackageManager().getActivityInfo(new ComponentName(ri.activityInfo.packageName,ri.activityInfo.name), 0);
            } catch (NameNotFoundException e) {
                return false;
            }
        }
        SameGroupComparator sgc = new SameGroupComparator();
        for (int i = 1; i < size(); ++i) {
            if (sgc.compare(get(i - 1), get(i)) != 0) {
                return false;
            }
        }
        return true;
    }

    public Drawable loadIcon(PackageManager pm){
        if(size() <= 0){
            return null;
        }else{
            return get(0).loadIcon(pm);
        }
    }

    public Bitmap loadBlackWhiteIcon(PackageManager pm) {
        if (mBlackWhiteIcon != null) {
            Bitmap ret = mBlackWhiteIcon.get();
            if (ret != null) {
                return ret;
            }
        }
        Bitmap ret = BitmapUtils.convertToBlackWhite(loadIcon(pm));
        mBlackWhiteIcon = new SoftReference<Bitmap>(ret);
        return ret;
    }

    public void onIconChanged(){
        if(mBlackWhiteIcon != null){
            mBlackWhiteIcon.clear();
            mBlackWhiteIcon = null;
        }
    }

    public boolean acceptDragEvent(Context context, DragEvent event) {
        if (event == null) {
            return true;
        }
        if (event.getClipDescription().getMimeTypeCount() <= 0 || size() <= 0) {
            return false;
        }

        String mimeType = event.getClipDescription().getMimeType(0);
        if (ClipDescription.MIMETYPE_TEXT_PLAIN.equals(mimeType)) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(sharingIntent, 0);
            if (infos != null) {
                for (ResolveInfo ri1 : this) {
                    for (ResolveInfo ri2 : infos) {
                        if (sameComponet(ri1, ri2)) {
                            return true;
                        }
                    }
                }
            }
        } else {
            for (String action : ResolveInfoManager.ACTIONS) {
                Intent intent = new Intent(action);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setType(mimeType);
                intent.setPackage(getPackageName());
                List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(intent, 0);
                if (infos != null) {
                    for (ResolveInfo ri1 : this) {
                        for (ResolveInfo ri2 : infos) {
                            if (sameComponet(ri1, ri2)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean handleEvent(Context context, DragEvent event){
        if (event.getClipData().getItemCount() <= 0
                || event.getClipDescription() == null
                || event.getClipDescription().getMimeTypeCount() <= 0
                || size() <= 0) {
            return false;
        }

        String mimeType = event.getClipDescription().getMimeType(0);
        if (ClipDescription.MIMETYPE_TEXT_PLAIN.equals(mimeType) && !TextUtils.isEmpty(event.getClipData().getItemAt(0).getText())) {
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setPackage(getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                    | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_TEXT, event.getClipData().getItemAt(0).getText());
            List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(intent, 0);
            if (infos != null) {
                for (ResolveInfo ri1 : this) {
                    for (ResolveInfo ri2 : infos) {
                        if (sameComponet(ri1, ri2)) {
                            intent.setComponent(new ComponentName(
                                    ri1.activityInfo.packageName,
                                    ri1.activityInfo.name));
                            Utils.dismissAllDialog(mContext);
                            context.startActivity(intent);
                            return true;
                        }
                    }
                }
            }
        }else{
            if(event.getClipData().getItemAt(0).getUri() == null){
                return false;
            }
            for (String action : ResolveInfoManager.ACTIONS) {
                Intent intent = new Intent(action);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setPackage(getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                        | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                if(Intent.ACTION_VIEW.equals(action)){
                    intent.setDataAndType(event.getClipData().getItemAt(0).getUri(), mimeType);
                }else{
                    intent.setType(mimeType);
                    intent.putExtra(Intent.EXTRA_STREAM, event.getClipData().getItemAt(0).getUri());
                }

                List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(intent, 0);
                if (infos != null) {
                    for (ResolveInfo ri1 : this) {
                        for (ResolveInfo ri2 : infos) {
                            if (sameComponet(ri1, ri2)) {
                                intent.setComponent(new ComponentName(ri1.activityInfo.packageName, ri1.activityInfo.name));
                                Utils.dismissAllDialog(mContext);
                                context.startActivity(intent);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static ResolveInfoGroup fromData(Context context, String pkgName, String componentNames){
        ResolveInfoGroup rig = new ResolveInfoGroup(context);
        String[] names = componentNames.split("\\|");
        if(names != null){
            for(String name : names){
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(pkgName, name));
                List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
                if(list != null && list.size() > 0){
                    rig.add(list.get(0));
                }else{
                    return null;
                }
            }
        }
        SameGroupComparator sgc = new SameGroupComparator();
        for (int i = 1; i < rig.size(); ++i) {
            if (sgc.compare(rig.get(i - 1), rig.get(i)) != 0) {
                return null;
            }
        }
        return rig;
    }

    public static boolean sameComponet(ResolveInfo ri1, ResolveInfo ri2){
        if(ri1.activityInfo == null || ri2.activityInfo == null){
            return false;
        }
        return ri1.activityInfo.packageName.equals(ri2.activityInfo.packageName) &&
                ri1.activityInfo.name.equals(ri2.activityInfo.name);
    }

    public static class IndexComparator implements Comparator<ResolveInfoGroup> {

        @Override
        public int compare(ResolveInfoGroup lhs, ResolveInfoGroup rhs) {
            if (lhs.getIndex() > rhs.getIndex()) {
                return -1;
            }
            if (lhs.getIndex() < rhs.getIndex()) {
                return 1;
            }
            return 0;
        }
    }

    public static class SameGroupComparator implements Comparator<ResolveInfo> {
        private static Set<String> sPACKAGES;
        static {
            sPACKAGES = new HashSet<String>();
            sPACKAGES.add("com.android.contacts");
        }

        public static boolean notNeedSplit(String packageName) {
            /**
            if (sPACKAGES.contains(packageName)) {
                return true;
            }
            */
            return packageName.startsWith("com.smartisan");
        }

        public final int compare(ResolveInfo a, ResolveInfo b) {
            String pkgA = a.activityInfo.packageName;
            String pkgB = b.activityInfo.packageName;
            if (!pkgA.equals(pkgB)) {
                return pkgA.compareTo(pkgB);
            }
            if (notNeedSplit(pkgA)) {
                return 0;
            }
            int la = getLabel(a);
            int lb = getLabel(b);
            if (la != lb) {
                if (la < lb) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return 0;
            }
        }

        public static final int getLabel(ResolveInfo ri) {
            if (ri.labelRes != 0) {
                return ri.labelRes;
            }
            if (ri.activityInfo != null) {
                if (ri.activityInfo.labelRes != 0) {
                    return ri.activityInfo.labelRes;
                } else if (ri.activityInfo.applicationInfo.labelRes != 0) {
                    return ri.activityInfo.applicationInfo.labelRes;
                }
            }
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        ResolveInfoGroup rig = (ResolveInfoGroup) o;
        if (!TextUtils.equals(this.getPackageName(), rig.getPackageName())) {
            return false;
        }
        if (!TextUtils.equals(this.getComponentNames(), rig.getComponentNames())) {
            return false;
        }
        return true;
    }

    @Override
    public CharSequence getDisplayName() {
        if(size() <= 0){
            return null;
        }else{
            return get(0).loadLabel(mContext.getPackageManager());
        }
    }

    @Override
    public void delete() {
        ResolveInfoManager.getInstance(mContext).delete(this);
    }
}

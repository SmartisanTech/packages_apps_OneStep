package com.smartisanos.sidebar.setting;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import com.smartisanos.sidebar.R;

import smartisanos.api.IntentSmt;

public class BaseActivity extends Activity {

    public void startActivityForResultWithAnimation(Intent intent, int requestCode) {
        intent.putExtra(IntentSmt.get_EXTRA_SMARTISAN_ANIM_RESOURCE_ID(), new int[] {
                0, smartisanos.R.anim.slide_down_out});
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(smartisanos.R.anim.pop_up_in, smartisanos.R.anim.fake_anim);
    }

    public void startActivity(Intent intent, boolean isPopup) {
        if(isPopup){
            intent.putExtra(IntentSmt.get_EXTRA_SMARTISAN_ANIM_RESOURCE_ID(), new int[] {
                    0, smartisanos.R.anim.slide_down_out});
            super.startActivity(intent);
            overridePendingTransition(smartisanos.R.anim.pop_up_in, smartisanos.R.anim.fake_anim);
        }else{
            intent.putExtra(IntentSmt.get_EXTRA_SMARTISAN_ANIM_RESOURCE_ID(), new int[] {
                    smartisanos.R.anim.slide_in_from_left, smartisanos.R.anim.slide_out_to_right});
            super.startActivity(intent);
            overridePendingTransition(smartisanos.R.anim.slide_in_from_right, smartisanos.R.anim.slide_out_to_left);
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (getIntent() != null) {
            int[] anims = getIntent().getIntArrayExtra(
                    IntentSmt.get_EXTRA_SMARTISAN_ANIM_RESOURCE_ID());
            if (anims != null) {
                overridePendingTransition(anims[0],anims[1]);
            }
        }
    }

    protected void setupBackBtnOnTitle() {
        smartisanos.widget.Title title = (smartisanos.widget.Title) findViewById(R.id.title_bar);
        title.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        try {
            title.setBackButtonTextByIntent(getIntent());
        } catch (RuntimeException e) {}
    }

    protected void setupBackBtnOnTitleByIntent() {
        smartisanos.widget.Title title = (smartisanos.widget.Title) findViewById(R.id.title_bar);
        title.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        if (intent != null) {
            boolean fromNotification = false;
            boolean fromSearch = false;
            boolean fromStatusBar = false;
            try {
                fromNotification = intent.getBooleanExtra("notification", false);
                fromSearch = intent.getBooleanExtra("from_search", false);
                fromStatusBar = intent.getBooleanExtra("status_bar", false);
            } catch (RuntimeException e) {}
            if (fromNotification || fromSearch || fromStatusBar) {
                title.setBackButtonText(getResources().getString(R.string.back_text));
            }
        }
    }
}
package com.tanjungdev.rentoutdoor.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.tanjungdev.rentoutdoor.R;




public class AboutActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        findViewById(R.id.crossImgView).setOnClickListener(this);

    }
    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.crossImgView:
                onBackPressed();
                break;
        }
    }
}

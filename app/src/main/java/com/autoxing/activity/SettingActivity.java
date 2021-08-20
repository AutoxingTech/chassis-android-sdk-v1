package com.autoxing.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.autoxing.controller.R;
import com.autoxing.robot_core.AXRobotPlatform;
import com.autoxing.util.GlobalUtil;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;

public class SettingActivity extends BaseActivity {

    private NiceSpinner mSpinner;

    private SharedPreferences mChassisSp = null;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mChassisSp = getSharedPreferences("chassis", SettingActivity.MODE_PRIVATE);

        initView();
        setListener();
    }

    private void initView() {
        mSpinner = findViewById(R.id.spinner);

        mSpinner.attachDataSource(GlobalUtil.serverDataset);
        int serverIndex = mChassisSp.getInt("serverIndex", 0);
        mSpinner.setSelectedIndex(serverIndex);
    }

    private void setListener() {
        mSpinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                String[] pais = item.split(":");
                SharedPreferences.Editor editor = mChassisSp.edit();
                editor.putInt("serverIndex", position);
                editor.commit();
                String token = "Token " + GlobalUtil.getToken(position);
                // String token = "dRw6JGyzFFwKNfFPQ8FFF";
                AXRobotPlatform.getInstance().connect(pais[0], Integer.parseInt(pais[1]), token);
            }
        });
    }
}

package com.autoxing.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import com.autoxing.controller.R;
import com.autoxing.fragment.MapListFragment;
import com.autoxing.fragment.MapRemoteFragment;
import com.autoxing.fragment.MappingFragment;
import com.autoxing.fragment.MappingListFragment;
import com.autoxing.robot_core.AXRobotPlatform;
import com.autoxing.util.GlobalUtil;
import com.autoxing.view.CustomViewPager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private String[] mTabs = {"Mapping", "Mapping List", "Map List", "Remote"};
    private String[] mPageTitles = {"Mapping", "Manage Mappings", "Manage Maps", "Remote Mode"};
    private List<Fragment> mTabFragmentList = new ArrayList<>();

    private TabLayout mTabLayout;
    private TextView mTitleName;
    private CustomViewPager mViewPager;
    private ImageButton mIbtnRight;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkWriteAndReadPermission();

        SharedPreferences mChassisSp = getSharedPreferences("chassis", SettingActivity.MODE_PRIVATE);
        int serverIndex = mChassisSp.getInt("serverIndex", 0);
        String server = GlobalUtil.serverDataset.get(serverIndex);
        String[] pairs = server.split(":");

        // String token = "Token " + GlobalUtil.getToken(serverIndex);
        String token = "dRw6JGyzFFwKNfFPQ8FFF";
        AXRobotPlatform.getInstance().connect(pairs[0], Integer.parseInt(pairs[1]), token);
        AXRobotPlatform.getInstance().enableBlockThread(false);

        initView();
        setListener();
    }

    private void initView() {
        mTitleName = (TextView) findViewById(R.id.tv_title_name);
        mIbtnRight = findViewById(R.id.ibtn_right);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (CustomViewPager) findViewById(R.id.main_tab_content);
        mViewPager.setPagingEnabled(false);

        for (int i = 0; i < mTabs.length; i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(mTabs[i]));
        }

        mTabFragmentList.add(new MappingFragment());
        mTabFragmentList.add(new MappingListFragment());
        mTabFragmentList.add(new MapListFragment());
        mTabFragmentList.add(new MapRemoteFragment());

        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return mTabFragmentList.get(position);
            }

            @Override
            public int getCount() {
                return mTabFragmentList.size();
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return mTabs[position];
            }
        });

        mTitleName.setText(mPageTitles[0]);
        mTabLayout.setupWithViewPager(mViewPager,false);
    }

    private void setListener() {
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mTitleName.setText(mPageTitles[tab.getPosition()]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mIbtnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkWriteAndReadPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            {
                String[] permissions = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
                requestPermissions(permissions, 1000); } }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int permission : grantResults)
        {
            if (permission == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "failed to request storage permission", Toast.LENGTH_LONG).show(); break;
            }
        }
    }
}

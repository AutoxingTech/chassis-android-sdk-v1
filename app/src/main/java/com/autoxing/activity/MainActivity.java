package com.autoxing.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.autoxing.controller.R;
import com.autoxing.fragment.MapListFragment;
import com.autoxing.fragment.MapRemoteFragment;
import com.autoxing.fragment.MappingFragment;
import com.autoxing.fragment.MappingListFragment;
import com.autoxing.robot_core.AXRobotPlatform;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private String[] mTabs = {"Mapping", "Mapping List", "Map List", "Remote"};
    private String[] mPageTitles = {"Mapping", "Manage Mappings", "Manage Maps", "Remote Mode"};
    private List<Fragment> mTabFragmentList = new ArrayList<>();

    private TabLayout mTabLayout;
    private TextView mTitleName;
    private ViewPager mViewPager;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // AXRobotPlatform.getInstance().connect("192.168.43.92", 8000);
        AXRobotPlatform.getInstance().startWebSocket();
        initView();
        setListener();
    }

    private void initView() {
        mTitleName = (TextView) findViewById(R.id.tv_title_name);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.main_tab_content);

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
    }
}

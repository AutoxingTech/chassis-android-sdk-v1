package com.autoxing.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.autoxing.controller.R;
import com.autoxing.fragment.MapAutoFragment;
import com.autoxing.fragment.MapRemoteFragment;
import com.autoxing.robot_core.AXRobotPlatform;
import com.autoxing.robot_core.bean.ChassisStatus;
import com.autoxing.robot_core.bean.Map;
import com.autoxing.util.RobotUtil;
import com.autoxing.view.CustomViewPager;
import com.autoxing.view.TabEntity;
import com.autoxing.x.util.CommonCallBack;
import com.autoxing.x.util.ThreadPoolUtil;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;

import java.util.ArrayList;
import java.util.List;

public class MapDetailActivity extends BaseActivity {

    public static Map selMap = null;

    private String mTitles[] = { "Auto", "Remote" };
    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private List<Fragment> mTabFragmentList = new ArrayList<>();

    private int[] mIconUnselectIds = {
            R.mipmap.tab_home_unselect, R.mipmap.tab_more_unselect};
    private int[] mIconSelectIds = {
            R.mipmap.tab_home_select, R.mipmap.tab_more_select};

    private View mLayout = null;
    private CommonTabLayout mTabLayout;
    private CustomViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_detail);

        initView();
        setListener();
    }

    @SuppressLint("ResourceAsColor")
    private void initView() {
        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.main_tab_content);

        for (int i = 0; i < mTitles.length; i++) {
            mTabEntities.add(new TabEntity(mTitles[i], mIconSelectIds[i], mIconUnselectIds[i]));
        }

        mTabLayout.setTabData(mTabEntities);
        mViewPager.setPagingEnabled(false);

        mTabFragmentList.add(new MapAutoFragment(selMap));
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
                return mTitles[position];
            }
        });
    }

    private void setListener() {
        mTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mViewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTabLayout.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeCurrentMap();
        RobotUtil.setChassisStatus(this, ChassisStatus.MANUAL);
    }

    private void removeCurrentMap() {
        ThreadPoolUtil.run(new CommonCallBack() {
            @Override
            public void run() {
                boolean succ = AXRobotPlatform.getInstance().removeCurrentMap();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!succ) {
                            Toast.makeText(MapDetailActivity.this, "failed to remove current map!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}

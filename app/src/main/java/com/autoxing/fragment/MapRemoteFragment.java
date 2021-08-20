package com.autoxing.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.autoxing.controller.R;
import com.autoxing.robot_core.bean.ChassisControlMode;
import com.autoxing.util.RobotUtil;
import com.autoxing.view.CustomViewPager;
import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;

import java.util.ArrayList;
import java.util.List;

public class MapRemoteFragment extends Fragment {

    private String mTitles[] = { "Action", "Twist" };
    private List<Fragment> mTabFragmentList = new ArrayList<>();

    private View mLayout = null;
    private SegmentTabLayout mTabLayout;
    private CustomViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mLayout == null) {
            mLayout = inflater.inflate(R.layout.map_remote_layout, container,false);
            initView(mLayout);
            setListener();
        } else {
            ViewGroup viewGroup = (ViewGroup) mLayout.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(mLayout);
            }
        }
        return mLayout;
    }

    private void initView(View view) {
        mTabLayout = view.findViewById(R.id.tab_layout);
        mViewPager = view.findViewById(R.id.main_tab_content);

        mTabLayout.setTabData(mTitles);
        mViewPager.setPagingEnabled(false);
        mViewPager.setCurrentItem(1);

        mTabFragmentList.add(new MapActionFragment());
        mTabFragmentList.add(new MapTwistFragment());
        mViewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager(),
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
    public void onResume() {
        super.onResume();
        RobotUtil.setChassisControlMode(getActivity(), ChassisControlMode.REMOTE);
    }
}

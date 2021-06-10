package com.autoxing.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.autoxing.controller.R;
import com.kongqw.rockerlibrary.view.RockerView;

public class MapTwistFragment extends Fragment {

    private View mLayout = null;
    private RockerView mRockerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mLayout == null) {
            mLayout = inflater.inflate(R.layout.map_twist_layout, container, false);
            initView(mLayout);

        } else {
            ViewGroup viewGroup = (ViewGroup) mLayout.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(mLayout);
            }
        }

        return mLayout;
    }

    private void initView(View view) {
        mRockerView = view.findViewById(R.id.rockerView);
    }

    private void setListener() {
        mRockerView.setOnShakeListener(RockerView.DirectionMode.DIRECTION_8, new RockerView.OnShakeListener() {

            @Override
            public void onStart() {

            }

            @Override
            public void direction(RockerView.Direction direction) {

            }

            @Override
            public void onFinish() {

            }
        });

        mRockerView.setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void angle(double angle) {

            }

            @Override
            public void onFinish() {

            }
        });
    }
}

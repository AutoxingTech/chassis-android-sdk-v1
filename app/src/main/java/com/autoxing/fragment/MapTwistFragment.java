package com.autoxing.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.autoxing.controller.R;
import com.autoxing.robot_core.AXRobotPlatform;
import com.autoxing.view.PlaneControlView;
import com.autoxing.robot_core.util.CommonCallback;
import com.autoxing.robot_core.util.ThreadPoolUtil;

import java.text.DecimalFormat;

public class MapTwistFragment extends Fragment {

    private View mLayout = null;
    //private RockerView mRockerView;
    private PlaneControlView mRockerView;
    private boolean mCanContinue = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mLayout == null) {
            mLayout = inflater.inflate(R.layout.map_twist_layout, container, false);
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
        mRockerView = view.findViewById(R.id.rockerView);
    }

    private void setListener() {
        mRockerView.setOnLocaListener(new PlaneControlView.OnLocaListener() {
            @Override
            public void getLocation(float x, float y) {
                DecimalFormat fnum = new DecimalFormat("##0.00");
                System.out.println("------------x = " + x + ", y = " + y);

            }
        });
    }

    private void moveWithTwist(float velocityY, float angularVelocityZ) {
        mCanContinue = false;
        ThreadPoolUtil.runAsync(new CommonCallback() {
            @Override
            public void run() {
                boolean succ = AXRobotPlatform.getInstance().moveWithTwist(velocityY, angularVelocityZ);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!succ) {
                            Toast.makeText(getActivity(),"failed to move with twist",1200).show();
                        } else {
                            mCanContinue = true;
                        }
                    }
                });
            }
        });
    }
}

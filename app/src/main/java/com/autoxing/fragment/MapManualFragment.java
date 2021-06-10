package com.autoxing.fragment;

import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.autoxing.controller.R;
import com.autoxing.robot_core.AXRobotPlatform;
import com.autoxing.robot_core.IMappingListener;
import com.autoxing.robot_core.bean.ChassisStatus;
import com.autoxing.robot_core.bean.Map;
import com.autoxing.robot_core.bean.PoseTopic;
import com.autoxing.robot_core.bean.TopicBase;
import com.autoxing.util.DensityUtil;
import com.autoxing.util.GlobalUtil;
import com.autoxing.util.RobotUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;

public class MapManualFragment extends Fragment implements IMappingListener {

    private Map mMap;
    private View mLayout = null;

    private ImageView mMappingImage;
    private ImageView mCurrentPos;

    private Point mScreenSize = null;
    private Bitmap mBitmap = null;

    public MapManualFragment(Map map) {
        super();
        mMap = map;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mLayout == null) {
            mLayout = inflater.inflate(R.layout.map_manual_layout, container,false);
            mScreenSize = GlobalUtil.getScreenSize(getContext());
            AXRobotPlatform.getInstance().addLisener(this);
            initView(mLayout);
            initData();
        } else {
            ViewGroup viewGroup = (ViewGroup) mLayout.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(mLayout);
            }
        }
        return mLayout;
    }

    private void initView(View view) {
        mMappingImage = view.findViewById(R.id.iv_mapping);
        mCurrentPos = view.findViewById(R.id.iv_current_pos);
    }

    private void initData() {
        Glide.with(getContext()).asBitmap().load(mMap.getUrl() + ".png").into(new SimpleTarget<Bitmap>() {

            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                mBitmap = resource;
                int mapWidth = resource.getWidth();
                int mapHeight = resource.getHeight();
                float scale = (float)mapHeight / mapWidth;

                int imageViewHeight = (int)(mScreenSize.x * scale);

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mMappingImage.getLayoutParams();
                params.width = mScreenSize.x;
                params.height = imageViewHeight;
                mMappingImage.setLayoutParams(params);
                mMappingImage.setScaleType(ImageView.ScaleType.FIT_XY);
                mMappingImage.setImageBitmap(resource);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        RobotUtil.setChassisStatus(getActivity(), ChassisStatus.MANUAL);
    }

    @Override
    public void onConnected(String status) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder();
                sb.append("webscoket connected, status is ");
                sb.append(status);
                Toast.makeText(getActivity(), sb.toString(),1200).show();
            }
        });
    }

    @Override
    public void onDataChanged(List<TopicBase> topics) {
        for (int i = 0; i < topics.size(); ++i) {
            TopicBase topic = topics.get(i);
            if (topic instanceof PoseTopic && mMap != null) {
                PoseTopic poseTopic = (PoseTopic)topic;
                Point pt = mMap.worldToScreen(poseTopic.getPose().getLocation());

                int viewWidth = mMappingImage.getWidth();
                int viewHeight = mMappingImage.getHeight();

                int bitmapWidth = mBitmap.getWidth();
                int bitmapHeight = mBitmap.getHeight();
                float scaleX = (float)bitmapWidth / viewWidth;
                float scaleY = (float)bitmapHeight / viewHeight;

                int curPosRadiusPx = DensityUtil.dip2px(getContext(),10.f);
                int screenX = (int)(pt.x / scaleX) + (int)mMappingImage.getX() - curPosRadiusPx;
                int screenY = (int)(((bitmapHeight - pt.y) / scaleY) + (int)mMappingImage.getY() - curPosRadiusPx);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCurrentPos.setX(screenX);
                        mCurrentPos.setY(screenY);
                        float degree = (float) Math.toDegrees(poseTopic.getPose().getYaw());
                        mCurrentPos.setRotation(degree);
                        mCurrentPos.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    @Override
    public void onError(Exception e) {
        Activity activity = getActivity();
        if (activity == null)
            return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(),"webscoket connected error",1200).show();
            }
        });
    }
}

package com.autoxing.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.autoxing.controller.R;
import com.autoxing.robot_core.AXRobotPlatform;
import com.autoxing.robot_core.IMappingListener;
import com.autoxing.robot_core.bean.ChassisStatus;
import com.autoxing.robot_core.bean.Mapping;
import com.autoxing.robot_core.bean.OccupancyGridTopic;
import com.autoxing.robot_core.bean.PoseTopic;
import com.autoxing.robot_core.bean.TopicBase;
import com.autoxing.util.DensityUtil;
import com.autoxing.util.GlobalUtil;
import com.autoxing.util.RobotUtil;
import com.autoxing.x.util.CommonCallBack;
import com.autoxing.x.util.ThreadPoolUtil;

import org.buraktamturk.loadingview.LoadingView;

import java.util.List;

public class MappingFragment extends Fragment implements IMappingListener, View.OnClickListener {

    private View mLayout;
    private ImageView mMappingImage;
    private ImageView mCurrentPos;
    private Button mBtnStart;
    private Button mBtnStop;

    private Point mScreenSize;
    private OccupancyGridTopic mOccuTopic = null;
    private Mapping mMapping = null;
    private LoadingView mLoadingView;

    public MappingFragment() {
        AXRobotPlatform.getInstance().addLisener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mLayout == null) {
            mLayout = inflater.inflate(R.layout.mapping_layout, container,false);
            mScreenSize = GlobalUtil.getScreenSize(getContext());
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
        mMappingImage = view.findViewById(R.id.iv_mapping);
        mCurrentPos = view.findViewById(R.id.iv_current_pos);
        mBtnStart = view.findViewById(R.id.btn_start);
        mBtnStop = view.findViewById(R.id.btn_stop);
        mLoadingView = view.findViewById(R.id.loading_view);
    }

    private void setListener() {
        mBtnStart.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);
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
            if (topic instanceof OccupancyGridTopic) {
                mOccuTopic = (OccupancyGridTopic) topic;
                Bitmap bitmap = mOccuTopic.getBitmap();
                int mapWidth = bitmap.getWidth();
                int mapHeight = bitmap.getHeight();
                float scale = (float)mapHeight / mapWidth;

                int imageViewHeight = (int)(mScreenSize.x * scale);

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mMappingImage.getLayoutParams();
                params.width = mScreenSize.x;
                params.height = imageViewHeight;
                mMappingImage.setLayoutParams(params);
                mMappingImage.setScaleType(ImageView.ScaleType.FIT_XY);
                mMappingImage.setImageBitmap(bitmap);
            } else if (topic instanceof PoseTopic && mOccuTopic != null) {
                PoseTopic poseTopic = (PoseTopic)topic;
                Point pt = mOccuTopic.worldToScreen(poseTopic.getPose().getLocation());

                int viewWidth = mMappingImage.getWidth();
                int viewHeight = mMappingImage.getHeight();

                Bitmap bitmap = mOccuTopic.getBitmap();
                int bitmapWidth = bitmap.getWidth();
                int bitmapHeight = bitmap.getHeight();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                mLoadingView.setLoading(true);

                ThreadPoolUtil.run(new CommonCallBack() {
                    @Override
                    public void run() {
                        mMapping =  AXRobotPlatform.getInstance().startMapping();

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String content = "mapping task is null";
                                if (mMapping != null) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("mapping task ");
                                    sb.append(mMapping.getId());
                                    sb.append(" state is ");
                                    sb.append(mMapping.getState());
                                    content = sb.toString();
                                }
                                Toast.makeText(getActivity(), content,1200).show();
                                mLoadingView.setLoading(false);
                            }
                        });
                    }
                });
                break;
            case R.id.btn_stop:
                if (mMapping == null) {
                    Toast.makeText(getActivity(),"mapping task is null!",1200).show();
                    return;
                }

                mLoadingView.setLoading(true);
                ThreadPoolUtil.run(new CommonCallBack() {
                    @Override
                    public void run() {
                        boolean succ = AXRobotPlatform.getInstance().stopCurrentMapping();

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder sb = new StringBuilder();
                                sb.append(succ ? "succeeded" : "failed");
                                sb.append(" to stop mapping task!");
                                Toast.makeText(getActivity(), sb.toString(),1200).show();
                                mLoadingView.setLoading(false);
                            }
                        });
                    }
                });
                break;
            default: break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        RobotUtil.setChassisStatus(getActivity(), ChassisStatus.MANUAL);
    }
}

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
import com.autoxing.robot_core.geometry.PointF;
import com.autoxing.robot_core.util.CoordinateUtil;
import com.autoxing.util.DensityUtil;
import com.autoxing.util.GlobalUtil;
import com.autoxing.util.RobotUtil;
import com.autoxing.robot_core.util.CommonCallback;
import com.autoxing.robot_core.util.ThreadPoolUtil;

import org.buraktamturk.loadingview.LoadingView;

public class MappingFragment extends Fragment implements IMappingListener, View.OnClickListener {

    private View mLayout;
    private ImageView mMappingImage;
    private ImageView mCurrentPos;
    private Button mBtnStart;
    private Button mBtnCancel;
    private Button mBtnStop;

    private Point mScreenSize;
    private OccupancyGridTopic mOccuTopic = null;
    private Mapping mMapping = null;
    private LoadingView mLoadingView;
    private CoordinateUtil mCoordinateUtil = new CoordinateUtil();

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
        mBtnCancel = view.findViewById(R.id.btn_cancel);
        mBtnStop = view.findViewById(R.id.btn_stop);
        mLoadingView = view.findViewById(R.id.loading_view);
    }

    private void setListener() {
        mBtnStart.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
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
    public void onDataChanged(TopicBase topic) {
        if (topic instanceof OccupancyGridTopic) {
            mOccuTopic = (OccupancyGridTopic) topic;
            mCoordinateUtil.setOrigin(mOccuTopic.getOriginX(), mOccuTopic.getOriginY());
            mCoordinateUtil.setResolution(mOccuTopic.getResolution());

            Bitmap bitmap = mOccuTopic.getBitmap();
            int mapWidth = bitmap.getWidth();
            int mapHeight = bitmap.getHeight();
            float scale = (float)mapHeight / mapWidth;

            int imageViewHeight = (int)(mScreenSize.x * scale);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mMappingImage.getLayoutParams();
                    params.width = mScreenSize.x;
                    params.height = imageViewHeight;
                    mMappingImage.setLayoutParams(params);
                    mMappingImage.setScaleType(ImageView.ScaleType.FIT_XY);
                    mMappingImage.setImageBitmap(bitmap);
                }
            });
        } else if (topic instanceof PoseTopic && mOccuTopic != null) {
            PoseTopic poseTopic = (PoseTopic)topic;
            PointF pt = mCoordinateUtil.worldToScreen(poseTopic.getPose().getLocation());

            int viewWidth = mMappingImage.getWidth();
            int viewHeight = mMappingImage.getHeight();

            Bitmap bitmap = mOccuTopic.getBitmap();
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            float scaleX = (float)bitmapWidth / viewWidth;
            float scaleY = (float)bitmapHeight / viewHeight;

            float curPosRadiusPx = DensityUtil.dip2px(getContext(),10.f);
            float screenX = (pt.getX() / scaleX) + mMappingImage.getX() - curPosRadiusPx;
            float screenY = ((bitmapHeight - pt.getY()) / scaleY) + mMappingImage.getY() - curPosRadiusPx;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCurrentPos.setX(screenX);
                    mCurrentPos.setY(screenY);
                    float degree = -(float) Math.toDegrees(poseTopic.getPose().getYaw());
                    mCurrentPos.setRotation(degree);
                    mCurrentPos.setVisibility(View.VISIBLE);
                }
            });
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
                ThreadPoolUtil.runAsync(new CommonCallback() {
                    @Override
                    public void run() {
                        mMapping =  AXRobotPlatform.getInstance().startMapping();

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String content = "failed to start mapping";
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
            case R.id.btn_cancel:
                mLoadingView.setLoading(true);
                ThreadPoolUtil.runAsync(new CommonCallback() {
                    @Override
                    public void run() {
                        int tmpRetCode = 0;
                        Mapping mapping = AXRobotPlatform.getInstance().getCurrentMapping();
                        if (mapping == null)
                            tmpRetCode = 1;
                        else if (!mapping.stop())
                            tmpRetCode = 2;

                        int finalTmpRetCode = tmpRetCode;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String content = "no tasking!";
                                if (finalTmpRetCode == 0 || finalTmpRetCode == 2) {
                                    StringBuilder sb = new StringBuilder();
                                    if (finalTmpRetCode == 0) {
                                        sb.append("succeeded");
                                    } else if (finalTmpRetCode == 2) {
                                        sb.append("failed");
                                    }
                                    sb.append(" to cancel mapping task!");
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
                mLoadingView.setLoading(true);
                ThreadPoolUtil.runAsync(new CommonCallback() {
                    @Override
                    public void run() {
                        int tmpRetCode = 0;
                        Mapping mapping = AXRobotPlatform.getInstance().getCurrentMapping();
                        if (mapping == null)
                            tmpRetCode = 1;
                        else if (!mapping.stop())
                            tmpRetCode = 2;

                        int finalTmpRetCode = tmpRetCode;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String content = "no tasking!";
                                if (finalTmpRetCode == 0 || finalTmpRetCode == 2) {
                                    StringBuilder sb = new StringBuilder();
                                    if (finalTmpRetCode == 0) {
                                        sb.append("succeeded");
                                    } else if (finalTmpRetCode == 2) {
                                        sb.append("failed");
                                    }
                                    sb.append(" to stop mapping task!");
                                    content = sb.toString();
                                }
                                Toast.makeText(getActivity(), content,1200).show();
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

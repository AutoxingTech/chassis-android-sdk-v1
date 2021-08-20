package com.autoxing.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
import com.autoxing.robot_core.bean.ChassisControlMode;
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
import com.autoxing.view.PinchImageView;

import org.buraktamturk.loadingview.LoadingView;

public class MappingFragment extends Fragment implements IMappingListener, View.OnClickListener {

    private View mLayout;
    private PinchImageView mMappingImage;
    private ImageView mCurrentPos;
    private Button mBtnStart;
    private Button mBtnCancel;
    private Button mBtnStop;

    private Point mScreenSize;
    private Bitmap mBitmap = null;
    private Mapping mMapping = null;
    private LoadingView mLoadingView;
    private CoordinateUtil mCoordinateUtil = new CoordinateUtil();

    private float mScale;
    private Matrix mMatrix = null;

    private float mCurPospRadiusPx;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mLayout == null) {
            mLayout = inflater.inflate(R.layout.mapping_layout, container,false);
            mCurPospRadiusPx = DensityUtil.dip2px(getContext(),10.f);
            AXRobotPlatform.getInstance().addLisener(this);
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

        mMappingImage.addOuterMatrixChangedListener(new PinchImageView.OuterMatrixChangedListener() {
            @Override
            public void onOuterMatrixChanged(PinchImageView pinchImageView) {
                Matrix outerMatrix = pinchImageView.getCurrentImageMatrix(null);
                float scale = PinchImageView.MathUtils.getMatrixScale(outerMatrix)[0];
                mScale = scale;
                mMatrix = outerMatrix;

                /*if (mCurrentPos.getVisibility() == View.VISIBLE) {
                    // 获取像素坐标
                    float imageX = mCurrentPos.getX();
                    float imageY = mCurrentPos.getY();

                    // 像素坐标转 view 坐标
                    float[] src = { imageX, imageY };
                    float[] dest = { .0f, .0f };
                    mMatrix.mapPoints(dest, src);
                    float viewX = dest[0];
                    float viewY = dest[1];

                    mCurrentPos.setX(viewX - mCurPospRadiusPx);
                    mCurrentPos.setY(viewY - mCurPospRadiusPx);
                }*/
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AXRobotPlatform.getInstance().removeLisener(this);
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
            OccupancyGridTopic occuTopic = (OccupancyGridTopic) topic;
            mCoordinateUtil.setOrigin(occuTopic.getOriginX(), occuTopic.getOriginY());
            mCoordinateUtil.setResolution(occuTopic.getResolution());

            mBitmap = occuTopic.getBitmap();
            int mapWidth = mBitmap.getWidth();
            int mapHeight = mBitmap.getHeight();
            float bitmapScale = (float)mapHeight / mapWidth;

            int imageViewHeight = (int)(mScreenSize.x * bitmapScale);
            mScale = imageViewHeight / mapHeight;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mMappingImage.getLayoutParams();
                    params.width = mScreenSize.x;
                    params.height = imageViewHeight;
                    mMappingImage.setLayoutParams(params);
                    mMappingImage.setScaleType(ImageView.ScaleType.FIT_XY);
                    mMappingImage.setImageBitmap(mBitmap);
                }
            });
        } else if (topic instanceof PoseTopic && mBitmap != null) {
            PoseTopic poseTopic = (PoseTopic)topic;
            PointF pt = mCoordinateUtil.worldToScreen(poseTopic.getPose().getLocation());

            int bitmapHeight = mBitmap.getHeight();

            // 像素坐标转 view 坐标
            float screenX = .0f;
            float screenY = .0f;
            if (mMatrix == null) {
                screenX = pt.getX() * mScale;
                screenY = (bitmapHeight - pt.getY()) * mScale;
            } else {
                float[] src = { pt.getX(), bitmapHeight - pt.getY() };
                float[] dest = { .0f, .0f };
                mMatrix.mapPoints(dest, src);
                screenX = dest[0];
                screenY = dest[1];
            }

            screenX -= mCurPospRadiusPx;
            screenY -= mCurPospRadiusPx;

            // 逆时针转顺时针
            float degree = -(float) Math.toDegrees(poseTopic.getPose().getYaw());

            float finalScreenX = screenX;
            float finalScreenY = screenY;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCurrentPos.setX(finalScreenX);
                    mCurrentPos.setY(finalScreenY);

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
                Toast.makeText(getActivity(),"webscoket connecte error",1200).show();
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
        RobotUtil.setChassisControlMode(getActivity(), ChassisControlMode.MANUAL);
    }
}

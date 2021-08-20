package com.autoxing.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.autoxing.controller.R;
import com.autoxing.robot_core.AXRobotPlatform;
import com.autoxing.robot_core.IMappingListener;
import com.autoxing.robot_core.action.ActionStatus;
import com.autoxing.robot_core.bean.ChassisStatus;
import com.autoxing.robot_core.bean.Location;
import com.autoxing.robot_core.bean.Map;
import com.autoxing.robot_core.action.MoveAction;
import com.autoxing.robot_core.bean.MoveOption;
import com.autoxing.robot_core.bean.PoseTopic;
import com.autoxing.robot_core.bean.TopicBase;
import com.autoxing.robot_core.geometry.PointF;
import com.autoxing.robot_core.util.CoordinateUtil;
import com.autoxing.robot_core.util.NetUtil;
import com.autoxing.util.DensityUtil;
import com.autoxing.util.GlobalUtil;
import com.autoxing.util.RobotUtil;
import com.autoxing.robot_core.util.CommonCallback;
import com.autoxing.robot_core.util.ThreadPoolUtil;
import com.autoxing.view.OnSingleClickListener;
import com.autoxing.view.PinchImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.victor.loading.rotate.RotateLoading;

import org.buraktamturk.loadingview.LoadingView;

import java.text.DecimalFormat;
import java.util.UUID;

import static com.bumptech.glide.request.RequestOptions.signatureOf;

public class MapAutoFragment extends Fragment implements View.OnClickListener, IMappingListener {

    private Map mMap;
    private View mLayout = null;

    private LoadingView mLoadingView;
    private FrameLayout mContainer;
    private TextView mPosValue;
    private TextView mTargetValue;
    private TextView mTestValue;
    private PinchImageView mIvMap;
    private ImageView mCurrentPos;
    private ImageView mAnchor = null;
    private Button mBtnMove;
    private Button mBtnCancel;
    private RotateLoading mRotateLoading;

    private float mScale;
    private Matrix mMatrix = null;

    private PointF mTargetViewPoint = new PointF();

    private Location mLocation = null;
    private Point mScreenSize = null;
    private Bitmap mBitmap = null;

    private MoveAction mMoveAction = null;
    private CoordinateUtil mCoordinateUtil = null;

    private DecimalFormat mDf = new DecimalFormat("##0.00");

    private float mTargetPospRadiusPx;
    private float mCurPospRadiusPx;

    public MapAutoFragment(Map map) {
        super();
        mMap = map;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mLayout == null) {
            mLayout = inflater.inflate(R.layout.map_auto_layout, container,false);
            mScreenSize = GlobalUtil.getScreenSize(getContext());
            if (mMap.isDetailLoaded()) {
                mCoordinateUtil = new CoordinateUtil();
                mCoordinateUtil.setOrigin(mMap.getOriginX(), mMap.getOriginY());
                mCoordinateUtil.setResolution(mMap.getResolution());
            }
            mTargetPospRadiusPx = DensityUtil.dip2px(getContext(),5.f);
            mCurPospRadiusPx = DensityUtil.dip2px(getContext(),10.f);
            AXRobotPlatform.getInstance().addLisener(this);
            initView(mLayout);
            initData();
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
        mLoadingView = (LoadingView) view.findViewById(R.id.loading_view);
        mContainer = (FrameLayout) view.findViewById(R.id.fl_continer);
        mIvMap = (PinchImageView) view.findViewById(R.id.iv_map);
        mPosValue = (TextView) view.findViewById(R.id.tv_pos_value);
        mTargetValue = (TextView) view.findViewById(R.id.tv_target_value);
        mTestValue = (TextView) view.findViewById(R.id.tv_test_value);
        mCurrentPos = (ImageView) view.findViewById(R.id.iv_current_pos);
        mBtnMove = (Button) view.findViewById(R.id.btn_move);
        mBtnCancel = (Button) view.findViewById(R.id.btn_cancel);
        mRotateLoading = (RotateLoading) view.findViewById(R.id.rotate_loading);
    }

    private void initData() {
        GlideUrl glideUrl = new GlideUrl(mMap.getUrl() + ".png", new LazyHeaders.Builder()
                .addHeader(NetUtil.getServiceTokenKey(), NetUtil.getServiceTokenValue())
                .build());

        Glide.with(getContext()).asBitmap().load(glideUrl).apply(signatureOf(new ObjectKey(UUID.randomUUID().toString()))
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).into(new SimpleTarget<Bitmap>() {

            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                mBitmap = resource;
                float mapWidth = (float)resource.getWidth();
                float mapHeight = (float)resource.getHeight();
                float bitmapScale = mapHeight / mapWidth;

                int imageViewHeight = (int)(mScreenSize.x * bitmapScale);
                mScale = imageViewHeight / mapHeight;

                /*StringBuilder sb = new StringBuilder();
                sb.append("" + mDf.format(mapWidth));
                sb.append(", " + mDf.format(mapHeight));
                sb.append("\n");

                sb.append(mDf.format(mScreenSize.x));
                sb.append(", " + mDf.format(imageViewHeight));
                sb.append("\n");

                sb.append("" + mDf.format(mScale));

                mTestValue.setText(sb.toString());*/

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mIvMap.getLayoutParams();
                params.width = mScreenSize.x;
                params.height = imageViewHeight;
                mIvMap.setLayoutParams(params);
                mIvMap.setScaleType(ImageView.ScaleType.FIT_XY);
                mIvMap.setImageBitmap(resource);
            }
        });
    }

    private void setListener() {
        mBtnMove.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);

        mIvMap.setOnSingleClickListener(new OnSingleClickListener() {
            @Override
            public void onClick(View v, MotionEvent e) {
                if(e.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = e.getX();
                    float y = e.getY();

                    float viewX = mIvMap.getX();
                    float viewY = mIvMap.getY();

                    /*int viewWidth = mIvMap.getWidth();
                    int viewHeight = mIvMap.getHeight();*/
                    int bitmapWidth = mBitmap.getWidth();
                    int bitmapHeight = mBitmap.getHeight();
                    /*float scaleX = (float)bitmapWidth / viewWidth;
                    float scaleY = (float)bitmapHeight / viewHeight;*/

                    //  view 坐标
                    float imageX = x - viewX;
                    float imageY = y - viewY;

                    //  view 坐标转像素坐标
                    float moveX = .0f;
                    float moveY = .0f;
                    if (mMatrix != null) {
                        float[] src = { imageX, imageY };
                        float[] dest = { .0f, .0f };
                        Matrix invertMatrix = new Matrix();
                        mMatrix.invert(invertMatrix);
                        invertMatrix.mapPoints(dest, src);
                        moveX = dest[0];
                        moveY = dest[1];
                    } else {
                        moveX = imageX / mScale;
                        moveY = imageY / mScale;
                    }

                    // 像素坐标转世界坐标
                    mLocation = mCoordinateUtil.screenToWorld(moveX,bitmapHeight - moveY);

                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("" + mDf.format(moveX));
                        sb.append(", " + mDf.format(bitmapHeight - moveY));
                        sb.append("\n");

                        sb.append(mDf.format(mLocation.getX()));
                        sb.append(", " + mDf.format(mLocation.getY()));

                        mTargetValue.setText(sb.toString());
                    }

                    mTargetViewPoint.setX(moveX);
                    mTargetViewPoint.setY(moveY);

                    imageX -= mTargetPospRadiusPx;
                    imageY -= mTargetPospRadiusPx;

                    if (mAnchor != null) {
                        mAnchor.setX(imageX);
                        mAnchor.setY(imageY);
                    } else {
                        mAnchor = new ImageView(getContext());
                        mAnchor.setImageDrawable(getResources().getDrawable((R.drawable.anchor)));
                        mContainer.addView(mAnchor);

                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mAnchor.getLayoutParams();
                        params.width = (int) mTargetPospRadiusPx * 2;
                        params.height = (int) mTargetPospRadiusPx * 2;
                        mAnchor.setLayoutParams(params);
                        mAnchor.setScaleType(ImageView.ScaleType.FIT_XY);

                        mAnchor.setX(imageX);
                        mAnchor.setY(imageY);
                    }
                }
            }
        });

        mIvMap.addOuterMatrixChangedListener(new PinchImageView.OuterMatrixChangedListener() {
            @Override
            public void onOuterMatrixChanged(PinchImageView pinchImageView) {
                Matrix outerMatrix = pinchImageView.getCurrentImageMatrix(null);
                float scale = PinchImageView.MathUtils.getMatrixScale(outerMatrix)[0];
                mScale = scale;
                mMatrix = outerMatrix;

                if (mAnchor != null) {
                    // 获取像素坐标
                    float imageX = mTargetViewPoint.getX();
                    float imageY = mTargetViewPoint.getY();

                    // 像素坐标转 view 坐标
                    float[] src = { imageX, imageY };
                    float[] dest = { .0f, .0f };
                    mMatrix.mapPoints(dest, src);
                    float viewX = dest[0];
                    float viewY = dest[1];

                    mAnchor.setX(viewX - mTargetPospRadiusPx);
                    mAnchor.setY(viewY - mTargetPospRadiusPx);
                }

                {
                    int bitmapHeight = mBitmap.getHeight();
                    float[] src = { mCurrentPos.getX(), bitmapHeight - mCurrentPos.getY() };
                    float[] dest = { .0f, .0f };
                    mMatrix.mapPoints(dest, src);
                    float screenX = dest[0];
                    float screenY = dest[1];

                    screenX -= mCurPospRadiusPx;
                    screenY -= mCurPospRadiusPx;
                    mCurrentPos.setX(screenX);
                    mCurrentPos.setY(screenY);
                }

                /*
                int viewWidth = mBitmap.getWidth();
                int viewHeight = mBitmap.getHeight();

                int width1 = (int)(viewWidth * scale);
                int height1 = (int)(viewHeight * scale);

                StringBuilder sb = new StringBuilder();
                sb.append("" + mDf.format(viewWidth));
                sb.append(", " + mDf.format(viewHeight));
                sb.append("\n");

                sb.append(mDf.format(width1));
                sb.append(", " + mDf.format(height1));
                sb.append("\n");

                sb.append("" + mDf.format(mScale));

                mTestValue.setText(sb.toString());*/
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        RobotUtil.setChassisStatus(getActivity(), ChassisStatus.AUTO);
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
                Toast.makeText(getContext(), sb.toString(),1200).show();
            }
        });
    }

    @Override
    public void onDataChanged(TopicBase topic) {
        Activity activity = getActivity();
        if (activity == null)
            return;

        if (topic instanceof PoseTopic && mBitmap != null && mCoordinateUtil != null) {
            PoseTopic poseTopic = (PoseTopic)topic;
            Location location = poseTopic.getPose().getLocation();
            // 世界坐标转像素坐标
            PointF pt = mCoordinateUtil.worldToScreen(location);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
                    mCurrentPos.setX(screenX);
                    mCurrentPos.setY(screenY);

                    // 逆时针转顺时针
                    float degree = -(float) Math.toDegrees(poseTopic.getPose().getYaw());
                    mCurrentPos.setRotation(degree);

                    {
                        StringBuilder sb = new StringBuilder();

                        sb.append("" + mDf.format(pt.getX()));
                        sb.append(", " + mDf.format(bitmapHeight - pt.getY()));
                        sb.append("\n");

                        sb.append(mDf.format(location.getX()));
                        sb.append(", " + mDf.format(location.getY()));
                        sb.append(", " + mDf.format(-degree));

                        mPosValue.setText(sb.toString());
                    }
                }
            });
        }
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();

        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "webscoket connected error", 1200).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_move:
                move();
                break;
            case R.id.btn_cancel:
                cancel();
                break;
            default:break;
        }
    }

    private void cancel() {
        if (mMoveAction == null) {
            Toast.makeText(getContext(),"no move action!", Toast.LENGTH_SHORT).show();
            return;
        }

        mLoadingView.setLoading(true);
        ThreadPoolUtil.runAsync(new CommonCallback() {
            @Override
            public void run() {
                boolean succ = mMoveAction.cancel();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String content = "failed to cancel current action!";
                        if (succ) {
                            content = "action status is canceled";
                            mMoveAction = null;
                        }
                        Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();
                        mLoadingView.setLoading(false);
                    }
                });
            }
        });
    }

    private void move() {
        if (mLocation == null) {
            Toast.makeText(getContext(),"please set dest position firstly!", Toast.LENGTH_SHORT).show();
        }

        mLoadingView.setLoading(true);
        ThreadPoolUtil.runAsync(new CommonCallback() {
            @Override
            public void run() {
                mMoveAction = AXRobotPlatform.getInstance().moveTo(mLocation, new MoveOption(), .0f);
                if (mMoveAction != null) {
                    if (mMoveAction.getStatus() == ActionStatus.MOVING) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRotateLoading.start();
                                mLoadingView.setLoading(false);
                            }
                        });

                        ActionStatus status = mMoveAction.waitUntilDone();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRotateLoading.stop();
                                Toast.makeText(getContext(), "robot status is " + status.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        ActionStatus status = mMoveAction.getStatus();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "robot status is " + status.toString(), Toast.LENGTH_SHORT).show();
                                mLoadingView.setLoading(false);
                            }
                        });
                    }
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "failed to move to dest!", Toast.LENGTH_SHORT).show();
                            mLoadingView.setLoading(false);
                        }
                    });
                }
            }
        });
    }
}

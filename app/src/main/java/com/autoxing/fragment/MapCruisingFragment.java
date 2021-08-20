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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.autoxing.controller.R;
import com.autoxing.robot_core.AXRobotPlatform;
import com.autoxing.robot_core.IMappingListener;
import com.autoxing.robot_core.action.ActionStatus;
import com.autoxing.robot_core.action.MoveAction;
import com.autoxing.robot_core.bean.ChassisStatus;
import com.autoxing.robot_core.bean.Location;
import com.autoxing.robot_core.bean.Map;
import com.autoxing.robot_core.bean.MoveOption;
import com.autoxing.robot_core.bean.PoseTopic;
import com.autoxing.robot_core.bean.TopicBase;
import com.autoxing.robot_core.geometry.PointF;
import com.autoxing.robot_core.util.CommonCallback;
import com.autoxing.robot_core.util.CoordinateUtil;
import com.autoxing.robot_core.util.NetUtil;
import com.autoxing.robot_core.util.ThreadPoolUtil;
import com.autoxing.util.DensityUtil;
import com.autoxing.util.GlobalUtil;
import com.autoxing.util.RobotUtil;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.bumptech.glide.request.RequestOptions.signatureOf;

public class MapCruisingFragment extends Fragment implements View.OnClickListener, IMappingListener {

    private Map mMap;
    private View mLayout = null;

    private LoadingView mLoadingView;
    private FrameLayout mContainer;
    private EditText mMoveCount;
    private PinchImageView mIvMap;
    private ImageView mCurrentPos;
    private List<ImageView> mTargetPointViews = new ArrayList<>();
    private Button mBtnMove;
    private Button mBtnCancel;
    private Button mBtnClear;
    private RotateLoading mRotateLoading;

    private float mScale;
    private Matrix mMatrix = null;

    private List<PointF> mTargetViewPoints = new ArrayList<>();
    private List<Location> mLocations = new ArrayList<>();

    private Point mScreenSize = null;
    private Bitmap mBitmap = null;

    private CoordinateUtil mCoordinateUtil = null;

    private float mCurPosRadiusPx;

    private int mCircleIndex = 0;     // start from 1

    private boolean mStopCircle = false;
    private int mCircleCount = 1000;

    private boolean mRunning = false;

    private boolean mBackground = false;

    public MapCruisingFragment(Map map) {
        super();
        mMap = map;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mLayout == null) {
            mLayout = inflater.inflate(R.layout.map_cruising_layout, container,false);
            mScreenSize = GlobalUtil.getScreenSize(getContext());
            if (mMap.isDetailLoaded()) {
                mCoordinateUtil = new CoordinateUtil();
                mCoordinateUtil.setOrigin(mMap.getOriginX(), mMap.getOriginY());
                mCoordinateUtil.setResolution(mMap.getResolution());
            }
            mCurPosRadiusPx = DensityUtil.dip2px(getContext(),5.f);
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
        mMoveCount = (EditText) view.findViewById(R.id.et_move_count);
        mCurrentPos = (ImageView) view.findViewById(R.id.iv_current_pos);
        mBtnMove = (Button) view.findViewById(R.id.btn_move);
        mBtnCancel = (Button) view.findViewById(R.id.btn_cancel);
        mBtnClear = (Button) view.findViewById(R.id.btn_clear);
        mRotateLoading = (RotateLoading) view.findViewById(R.id.rotate_loading);
    }

    private void initData() {
        mMoveCount.setText("" + mCircleCount);

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
        mBtnClear.setOnClickListener(this);

        mIvMap.setOnSingleClickListener(new OnSingleClickListener() {
            @Override
            public void onClick(View v, MotionEvent e) {
                if(e.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = e.getX();
                    float y = e.getY();

                    float viewX = mIvMap.getX();
                    float viewY = mIvMap.getY();

                    int bitmapWidth = mBitmap.getWidth();
                    int bitmapHeight = mBitmap.getHeight();

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
                    Location location = mCoordinateUtil.screenToWorld(moveX,bitmapHeight - moveY);
                    mLocations.add(location);

                    PointF targetViewPoint = new PointF();
                    targetViewPoint.setX(moveX);
                    targetViewPoint.setY(moveY);
                    mTargetViewPoints.add(targetViewPoint);

                    imageX -= mCurPosRadiusPx;
                    imageY -= mCurPosRadiusPx;

                    ImageView anchor = new ImageView(getContext());
                    anchor.setImageDrawable(getResources().getDrawable((R.drawable.anchor)));
                    mContainer.addView(anchor);

                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) anchor.getLayoutParams();
                    params.width = (int) mCurPosRadiusPx * 2;
                    params.height = (int) mCurPosRadiusPx * 2;
                    anchor.setLayoutParams(params);
                    anchor.setScaleType(ImageView.ScaleType.FIT_XY);

                    anchor.setX(imageX);
                    anchor.setY(imageY);

                    mTargetPointViews.add(anchor);
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

                for (int i = 0; i < mTargetPointViews.size(); ++i) {
                    ImageView anchor = mTargetPointViews.get(i);

                    PointF targetViewPoint = mTargetViewPoints.get(i);
                    // 获取像素坐标
                    float imageX = targetViewPoint.getX();
                    float imageY = targetViewPoint.getY();

                    // 像素坐标转 view 坐标
                    float[] src = { imageX, imageY };
                    float[] dest = { .0f, .0f };
                    mMatrix.mapPoints(dest, src);
                    float viewX = dest[0];
                    float viewY = dest[1];

                    anchor.setX(viewX - mCurPosRadiusPx);
                    anchor.setY(viewY - mCurPosRadiusPx);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        RobotUtil.setChassisStatus(getActivity(), ChassisStatus.AUTO);
        mBackground = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mBackground = true;
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
                    float curPospRadiusPx = DensityUtil.dip2px(activity,10.f);

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

                    screenX -= curPospRadiusPx;
                    screenY -= curPospRadiusPx;
                    mCurrentPos.setX(screenX);
                    mCurrentPos.setY(screenY);

                    // 逆时针转顺时针
                    float degree = -(float) Math.toDegrees(poseTopic.getPose().getYaw());
                    mCurrentPos.setRotation(degree);
                }
            });
        }
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();

        Activity activity = getActivity();
        if (activity == null)
            return;

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
            case R.id.btn_clear:
                clear();
                break;
            default:break;
        }
    }

    private void reset() {
        mCircleIndex = 0;
        mCircleCount = 1000;
        mMoveCount.setText("" + mCircleCount);
        mStopCircle = false;
    }

    private void clear() {
        for (ImageView targetView : mTargetPointViews) {
            ViewGroup parent = (ViewGroup)targetView.getParent();
            parent.removeView(targetView);
        }
        mTargetPointViews.clear();
        mTargetViewPoints.clear();
        mLocations.clear();

        if (mRunning)
            mStopCircle = true;
    }

    private void cancel() {
        if (mRunning)
            mStopCircle = true;

        ThreadPoolUtil.runAsync(new CommonCallback() {

            @Override
            public void run() {
                MoveAction moveAction = AXRobotPlatform.getInstance().getCurrentAction();
                if (moveAction != null) {
                    boolean succ = moveAction.cancel();
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            String content = "failed to cancel current action!";
                            if (succ)
                                content = "action status is canceled";
                            Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void move() {
        if (mLocations.isEmpty()) {
            Toast.makeText(getContext(),"please set dest position firstly!", Toast.LENGTH_SHORT).show();
        }

        //mLoadingView.setLoading(true);
        ThreadPoolUtil.runAsync(new CommonCallback() {
            @Override
            public void run() {

                mRunning = true;
                for (;;) {

                    mCircleIndex++;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMoveCount.setText("" + (mCircleCount - mCircleIndex));
                        }
                    });

                    for (Location location : mLocations) {
                        if (mStopCircle)
                            break;

                        while (mBackground) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        MoveAction moveAction = AXRobotPlatform.getInstance().moveTo(location, new MoveOption(), .0f);

                        if (moveAction != null) {
                            if (moveAction.getStatus() == ActionStatus.MOVING) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRotateLoading.start();
                                        //mLoadingView.setLoading(false);
                                    }
                                });

                                ActionStatus status = moveAction.waitUntilDone();
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRotateLoading.stop();
                                        Toast.makeText(getContext(), "2robot status is " + status.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } else {
                                ActionStatus status = moveAction.getStatus();
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "1robot status is " + status.toString(), Toast.LENGTH_SHORT).show();
                                        //mLoadingView.setLoading(false);
                                    }
                                });
                            }
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "failed to move to dest!", Toast.LENGTH_SHORT).show();
                                    //mLoadingView.setLoading(false);
                                }
                            });
                        }
                    }

                    if (mStopCircle || mCircleIndex == mCircleCount)
                    {
                        mRunning = false;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                reset();
                            }
                        });

                        break;
                    }
                }
            }
        });
    }
}

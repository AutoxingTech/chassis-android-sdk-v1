package com.autoxing.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
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
import com.autoxing.robot_core.bean.ChassisStatus;
import com.autoxing.robot_core.bean.Location;
import com.autoxing.robot_core.bean.Map;
import com.autoxing.robot_core.action.MoveAction;
import com.autoxing.robot_core.bean.MoveOption;
import com.autoxing.robot_core.bean.PoseTopic;
import com.autoxing.robot_core.bean.TopicBase;
import com.autoxing.robot_core.geometry.PointF;
import com.autoxing.robot_core.util.CoordinateUtil;
import com.autoxing.util.DensityUtil;
import com.autoxing.util.GlobalUtil;
import com.autoxing.util.RobotUtil;
import com.autoxing.robot_core.util.CommonCallback;
import com.autoxing.robot_core.util.ThreadPoolUtil;
import com.autoxing.view.HugeImageRegionLoader;
import com.autoxing.view.PinchImageView;
import com.autoxing.view.TileDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.buraktamturk.loadingview.LoadingView;

import java.text.DecimalFormat;

public class MapAutoFragment extends Fragment implements View.OnClickListener, IMappingListener {

    private Map mMap;
    private View mLayout = null;

    private LoadingView mLoadingView;
    private FrameLayout mContainer;
    private TextView mCoordinateValue;
    private PinchImageView mIvMap;
    private ImageView mCurrentPos;
    private ImageView mAnchor = null;
    private Button mBtnMove;
    private Button mBtnCancel;

    private Location mLocation = null;
    private Point mScreenSize = null;
    private Bitmap mBitmap = null;

    private MoveAction mMoveAction = null;
    private CoordinateUtil mCoordinateUtil = null;

    // private TileDrawable mMapDrawable;

    private DecimalFormat mDf = new DecimalFormat("##0.00");

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
        mCoordinateValue = (TextView) view.findViewById(R.id.tv_screen_value);
        mCurrentPos = (ImageView) view.findViewById(R.id.iv_current_pos);
        mBtnMove = (Button) view.findViewById(R.id.btn_move);
        mBtnCancel = (Button) view.findViewById(R.id.btn_cancel);
    }

    private void initData() {
        /*Glide.with(getContext()).asBitmap().load(mMap.getUrl() + ".png").into(new SimpleTarget<Bitmap>() {

            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                mBitmap = resource;
                int mapWidth = resource.getWidth();
                int mapHeight = resource.getHeight();
                float scale = (float)mapHeight / mapWidth;

                int imageViewHeight = (int)(mScreenSize.x * scale);

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mIvMap.getLayoutParams();
                params.width = mScreenSize.x;
                params.height = imageViewHeight;
                mIvMap.setLayoutParams(params);
                mIvMap.setScaleType(ImageView.ScaleType.FIT_XY);
                mIvMap.setImageBitmap(resource);
            }
        });*/

        /*mIvMap.post(new Runnable() {
            @Override
            public void run() {
                mMapDrawable = new TileDrawable();
                mMapDrawable.setInitCallback(new TileDrawable.InitCallback() {
                    @Override
                    public void onInit() {
                        mIvMap.setImageDrawable(mMapDrawable);
                    }
                });
                mMapDrawable.init(new HugeImageRegionLoader(getActivity(), Uri.parse(mMap.getUrl() + ".png")), new Point(mIvMap.getWidth(), mIvMap.getHeight()));
            }
        });*/


        final DisplayImageOptions thumbOptions = new DisplayImageOptions.Builder().resetViewBeforeLoading(true).cacheInMemory(true).build();
        GlobalUtil.getImageLoader(getActivity()).displayImage(mMap.getUrl() + ".png", mIvMap, thumbOptions, new ImageLoadingListener(){

            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mBitmap = loadedImage;
                int mapWidth = loadedImage.getWidth();
                int mapHeight = loadedImage.getHeight();
                float scale = (float)mapHeight / mapWidth;

                int imageViewHeight = (int)(mScreenSize.x * scale);

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mIvMap.getLayoutParams();
                params.width = mScreenSize.x;
                params.height = imageViewHeight;
                mIvMap.setLayoutParams(params);
                mIvMap.setScaleType(ImageView.ScaleType.FIT_XY);
                mIvMap.setImageBitmap(loadedImage);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });

        loadMapDetail();
    }

    private void setListener() {
        mBtnMove.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
        /*mIvMap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = event.getX();
                    float y = event.getY();

                    float viewX = mIvMap.getX();
                    float viewY = mIvMap.getY();

                    int viewWidth = mIvMap.getWidth();
                    int viewHeight = mIvMap.getHeight();

                    int bitmapWidth = mBitmap.getWidth();
                    int bitmapHeight = mBitmap.getHeight();
                    float scaleX = (float)bitmapWidth / viewWidth;
                    float scaleY = (float)bitmapHeight / viewHeight;

                    float imageX = x - viewX;
                    float imageY = y - viewY;

                    float moveX = imageX * scaleX;
                    float moveY = imageY * scaleY;
                    StringBuilder sb = new StringBuilder();
                    sb.append("x = " + mDf.format(moveX));
                    sb.append(", y = " + mDf.format(moveY));
                    sb.append("\n");

                    mLocation = mCoordinateUtil.screenToWorld(moveX,bitmapHeight - moveY);
                    sb.append("x = " + mDf.format(mLocation.getX()));
                    sb.append(", y = " + mDf.format(mLocation.getY()));
                    sb.append(", z = " + mDf.format(mLocation.getZ()));

                    mCoordinateValue.setText(sb.toString());

                    float curPospRadiusPx = DensityUtil.dip2px(getContext(),5.f);
                    imageX -= curPospRadiusPx;
                    imageY -= curPospRadiusPx;

                    if (mAnchor != null) {
                        mAnchor.setX(imageX);
                        mAnchor.setY(imageY);
                    } else {
                        mAnchor = new ImageView(getContext());
                        mAnchor.setImageDrawable(getResources().getDrawable((R.drawable.anchor)));
                        mContainer.addView(mAnchor);

                        float anchorDiameterPx = DensityUtil.dip2px(getContext(),10.f);

                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mAnchor.getLayoutParams();
                        params.width = (int)anchorDiameterPx;
                        params.height = (int)anchorDiameterPx;
                        mAnchor.setLayoutParams(params);
                        mAnchor.setScaleType(ImageView.ScaleType.FIT_XY);

                        mAnchor.setX(imageX);
                        mAnchor.setY(imageY);
                    }
                }
                return true;
            }
        });*/
    }

    @Override
    public void onResume() {
        super.onResume();
        RobotUtil.setChassisStatus(getActivity(), ChassisStatus.AUTO);
    }

    @Override
    public void onDestroy() {
        /*if (mMapDrawable != null) {
            mMapDrawable.recycle();
        }*/
        super.onDestroy();
        AXRobotPlatform.getInstance().removeLisener(this);
    }

    private void loadMapDetail() {
        ThreadPoolUtil.runAsync(new CommonCallback() {
            @Override
            public void run() {
                boolean succ = mMap.loadDetail();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!succ) {
                            Toast.makeText(getContext(), "failed to load map detail", Toast.LENGTH_SHORT).show();
                        } else {
                            mCoordinateUtil = new CoordinateUtil();
                            mCoordinateUtil.setOrigin(mMap.getOriginX(), mMap.getOriginY());
                            mCoordinateUtil.setResolution(mMap.getResolution());
                        }
                    }
                });
            }
        });
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
            System.out.println("---***------------ received ");
            PoseTopic poseTopic = (PoseTopic)topic;
            PointF pt = mCoordinateUtil.worldToScreen(poseTopic.getPose().getLocation());

            int viewWidth = mIvMap.getWidth();
            int viewHeight = mIvMap.getHeight();

            int bitmapWidth = mBitmap.getWidth();
            int bitmapHeight = mBitmap.getHeight();
            float scaleX = (float)bitmapWidth / viewWidth;
            float scaleY = (float)bitmapHeight / viewHeight;

            float curPospRadiusPx = DensityUtil.dip2px(activity,10.f);
            float screenX = (pt.getX() / scaleX) - curPospRadiusPx;
            float screenY = ((bitmapHeight - pt.getY()) / scaleY) - curPospRadiusPx;

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCurrentPos.setX(screenX);
                    mCurrentPos.setY(screenY);
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

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String content = "failed to move to dest!";
                        if (mMoveAction != null) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("robot status is ");
                            sb.append(mMoveAction.getStatus().toString());
                            content = sb.toString();
                        }
                        Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();
                        mLoadingView.setLoading(false);
                    }
                });
            }
        });
    }
}

package com.autoxing.activity;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.autoxing.controller.R;
import com.autoxing.robot_core.AXRobotPlatform;
import com.autoxing.robot_core.IMappingListener;
import com.autoxing.robot_core.bean.ChassisStatus;
import com.autoxing.robot_core.bean.Location;
import com.autoxing.robot_core.bean.Map;
import com.autoxing.robot_core.bean.MoveAction;
import com.autoxing.robot_core.bean.MoveOption;
import com.autoxing.robot_core.bean.PoseTopic;
import com.autoxing.robot_core.bean.TopicBase;
import com.autoxing.util.DensityUtil;
import com.autoxing.util.GlobalUtil;
import com.autoxing.util.RobotUtil;
import com.autoxing.x.util.CommonCallBack;
import com.autoxing.x.util.ThreadPoolUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.buraktamturk.loadingview.LoadingView;

import java.util.List;

public class MapDetailActivity extends BaseActivity implements View.OnClickListener, IMappingListener {

    public static Map mMap = null;

    private LoadingView mLoadingView;
    private FrameLayout mContainer;
    private TextView mCoordinateValue;
    private ImageView mIvMap;
    private ImageView mCurrentPos;
    private ImageView mAnchor = null;
    private Button mBtnMove;

    private Location mLocation = null;
    private Point mScreenSize = null;
    private Bitmap mBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_detail);

        mScreenSize = GlobalUtil.getScreenSize(this);

        initView();
        initData();
        setListener();

        AXRobotPlatform.getInstance().addLisener(this);
        RobotUtil.setChassisStatus(this, ChassisStatus.REMOTE);
        RobotUtil.setChassisStatus(this, ChassisStatus.AUTO);
    }

    private void initView() {
        mLoadingView = (LoadingView) findViewById(R.id.loading_view);
        mContainer = (FrameLayout) findViewById(R.id.fl_continer);
        mIvMap = (ImageView) findViewById(R.id.iv_map);
        mCoordinateValue = (TextView) findViewById(R.id.tv_screen_value);
        mCurrentPos = (ImageView) findViewById(R.id.iv_current_pos);
        mBtnMove = (Button) findViewById(R.id.btn_move);
    }

    private void initData() {
        Glide.with(this).asBitmap().load(mMap.getUrl() + ".png").into(new SimpleTarget<Bitmap>() {

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
        });

        loadMapDetail();
    }

    private void setListener() {
        mBtnMove.setOnClickListener(this);

        mIvMap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    int x = (int)event.getX();
                    int y = (int)event.getY();

                    int viewX = (int)mIvMap.getX();
                    int viewY = (int)mIvMap.getY();

                    int viewWidth = mIvMap.getWidth();
                    int viewHeight = mIvMap.getHeight();

                    int bitmapWidth = mBitmap.getWidth();
                    int bitmapHeight = mBitmap.getHeight();
                    float scaleX = (float)bitmapWidth / viewWidth;
                    float scaleY = (float)bitmapHeight / viewHeight;

                    int imageX = x - viewX;
                    int imageY = y - viewY;

                    int moveX = (int)(imageX * scaleX);
                    int moveY = (int)(imageY * scaleY);
                    StringBuilder sb = new StringBuilder();
                    sb.append("x = " + moveX);
                    sb.append(", y = " + moveY);
                    sb.append("\n");

                    mLocation = mMap.screenToWorld(moveX,bitmapHeight - moveY);
                    sb.append("x = " + mLocation.getX());
                    sb.append(", y = " + mLocation.getY());
                    sb.append(", z = " + mLocation.getZ());

                    mCoordinateValue.setText(sb.toString());

                    int curPospRadiusPx = DensityUtil.dip2px(MapDetailActivity.this,5.f);
                    imageX -= curPospRadiusPx;
                    imageY -= curPospRadiusPx;

                    if (mAnchor != null) {
                        mAnchor.setX(imageX);
                        mAnchor.setY(imageY);
                    } else {
                        mAnchor = new ImageView(MapDetailActivity.this);
                        mAnchor.setImageDrawable(getResources().getDrawable((R.drawable.anchor)));
                        mContainer.addView(mAnchor);

                        int anchorDiameterPx = DensityUtil.dip2px(MapDetailActivity.this,10.f);

                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mAnchor.getLayoutParams();
                        params.width = anchorDiameterPx;
                        params.height = anchorDiameterPx;
                        mAnchor.setLayoutParams(params);
                        mAnchor.setScaleType(ImageView.ScaleType.FIT_XY);

                        mAnchor.setX(imageX);
                        mAnchor.setY(imageY);
                    }
                }
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeCurrentMap();
        RobotUtil.setChassisStatus(this, ChassisStatus.MANUAL);
    }

    private void removeCurrentMap() {
        ThreadPoolUtil.run(new CommonCallBack() {
            @Override
            public void run() {
                boolean succ = AXRobotPlatform.getInstance().removeCurrentMap();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder sb = new StringBuilder();
                        sb.append(succ ? "succeeded" : "failed");
                        sb.append(" to remove current map!");
                        Toast.makeText(MapDetailActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadMapDetail() {
        ThreadPoolUtil.run(new CommonCallBack() {
            @Override
            public void run() {
                boolean succ = mMap.loadDetail();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder sb = new StringBuilder();
                        sb.append(succ ? "succeeded" : "failed");
                        sb.append(" to load map detail");
                        Toast.makeText(MapDetailActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onConnected(String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder();
                sb.append("webscoket connected, status is ");
                sb.append(status);
                Toast.makeText(MapDetailActivity.this, sb.toString(),1200).show();
            }
        });
    }

    @Override
    public void onDataChanged(List<TopicBase> topics) {
        for (int i = 0; i < topics.size(); ++i) {
            TopicBase topic = topics.get(i);
            if (topic instanceof PoseTopic && mBitmap != null) {
                PoseTopic poseTopic = (PoseTopic)topic;
                Point pt = mMap.worldToScreen(poseTopic.getPose().getLocation());

                int viewWidth = mIvMap.getWidth();
                int viewHeight = mIvMap.getHeight();

                int bitmapWidth = mBitmap.getWidth();
                int bitmapHeight = mBitmap.getHeight();
                float scaleX = (float)bitmapWidth / viewWidth;
                float scaleY = (float)bitmapHeight / viewHeight;

                int curPospRadiusPx = DensityUtil.dip2px(MapDetailActivity.this,5.f);
                int screenX = (int)(pt.x / scaleX) - curPospRadiusPx;
                int screenY = (int)(((bitmapHeight - pt.y) / scaleY) - curPospRadiusPx);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCurrentPos.setX(screenX);
                        mCurrentPos.setY(screenY);
                        mCurrentPos.setRotation(poseTopic.getPose().getYaw());
                        mCurrentPos.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MapDetailActivity.this, "webscoket connected error", 1200).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_move:
                move();
                break;
            default:break;
        }
    }

    private void move() {
        if (mLocation == null) {
            Toast.makeText(this,"please set dest position firstly!", Toast.LENGTH_SHORT).show();
        }

        mLoadingView.setLoading(true);
        ThreadPoolUtil.run(new CommonCallBack() {
            @Override
            public void run() {
                MoveAction moveAction = AXRobotPlatform.getInstance().moveTo(mLocation, new MoveOption(), .0f);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder sb = new StringBuilder();
                        sb.append("robot status is ");
                        sb.append(moveAction.getStatus().toString());
                        Toast.makeText(MapDetailActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                        mLoadingView.setLoading(false);
                    }
                });
            }
        });
    }
}

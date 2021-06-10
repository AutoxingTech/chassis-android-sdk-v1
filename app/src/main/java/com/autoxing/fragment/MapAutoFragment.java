package com.autoxing.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
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

public class MapAutoFragment extends Fragment implements View.OnClickListener, IMappingListener {

    private Map mMap;
    private View mLayout = null;

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
        mIvMap = (ImageView) view.findViewById(R.id.iv_map);
        mCoordinateValue = (TextView) view.findViewById(R.id.tv_screen_value);
        mCurrentPos = (ImageView) view.findViewById(R.id.iv_current_pos);
        mBtnMove = (Button) view.findViewById(R.id.btn_move);
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

                    int curPospRadiusPx = DensityUtil.dip2px(getContext(),5.f);
                    imageX -= curPospRadiusPx;
                    imageY -= curPospRadiusPx;

                    if (mAnchor != null) {
                        mAnchor.setX(imageX);
                        mAnchor.setY(imageY);
                    } else {
                        mAnchor = new ImageView(getContext());
                        mAnchor.setImageDrawable(getResources().getDrawable((R.drawable.anchor)));
                        mContainer.addView(mAnchor);

                        int anchorDiameterPx = DensityUtil.dip2px(getContext(),10.f);

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
    public void onResume() {
        super.onResume();
        RobotUtil.setChassisStatus(getActivity(), ChassisStatus.AUTO);
    }

    private void loadMapDetail() {
        ThreadPoolUtil.run(new CommonCallBack() {
            @Override
            public void run() {
                boolean succ = mMap.loadDetail();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!succ) {
                            Toast.makeText(getContext(), "failed to load map detail", Toast.LENGTH_SHORT).show();
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
    public void onDataChanged(List<TopicBase> topics) {
        Activity activity = getActivity();
        if (activity == null)
            return;

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

                int curPospRadiusPx = DensityUtil.dip2px(activity,5.f);
                int screenX = (int)(pt.x / scaleX) - curPospRadiusPx;
                int screenY = (int)(((bitmapHeight - pt.y) / scaleY) - curPospRadiusPx);

                activity.runOnUiThread(new Runnable() {
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

        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "webscoket connected error", 1200).show();
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
            Toast.makeText(getContext(),"please set dest position firstly!", Toast.LENGTH_SHORT).show();
        }

        mLoadingView.setLoading(true);
        ThreadPoolUtil.run(new CommonCallBack() {
            @Override
            public void run() {
                MoveAction moveAction = AXRobotPlatform.getInstance().moveTo(mLocation, new MoveOption(), .0f);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String content = "failed to move to dest!";
                        if (moveAction != null) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("robot status is ");
                            sb.append(moveAction.getStatus().toString());
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

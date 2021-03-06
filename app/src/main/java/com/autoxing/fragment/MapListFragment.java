package com.autoxing.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.autoxing.adapter.IMapAdapterListener;
import com.autoxing.activity.MapDetailActivity;
import com.autoxing.adapter.MapRvAdapter;
import com.autoxing.controller.R;
import com.autoxing.robot_core.AXRobotPlatform;
import com.autoxing.robot_core.bean.Map;
import com.autoxing.robot_core.bean.Pose;
import com.autoxing.robot_core.util.CommonCallback;
import com.autoxing.robot_core.util.ThreadPoolUtil;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.buraktamturk.loadingview.LoadingView;

import java.util.List;

public class MapListFragment extends Fragment implements IMapAdapterListener {

    private View mLayout = null;
    private LoadingView mLoadingView;
    private RefreshLayout mSwipeRefreshWidget;
    private RecyclerView mRvMap;
    private MapRvAdapter mMapAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mLayout == null) {
            mLayout = inflater.inflate(R.layout.map_list_layout, container,false);
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
        mLoadingView = view.findViewById(R.id.loading_view);
        mRvMap = view.findViewById(R.id.rv_main);
        mSwipeRefreshWidget = view.findViewById(R.id.swipe_refresh_widget);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRvMap.setLayoutManager(layoutManager);
        mRvMap.setItemAnimator(new DefaultItemAnimator());

        mRvMap.setHasFixedSize(true);

        mMapAdapter = new MapRvAdapter(getContext());
        mMapAdapter.setListener(this);
        mRvMap.setAdapter(mMapAdapter);
    }

    private void initData() {
        initMaps();
    }

    private void setListener() {
        mSwipeRefreshWidget.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                initMaps();
            }
        });
    }

    private void initMaps() {
        ThreadPoolUtil.runAsync(new CommonCallback() {
            @Override
            public void run() {
                List<Map> maps = AXRobotPlatform.getInstance().getMaps();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (maps == null) {
                            mSwipeRefreshWidget.finishRefresh(false);
                            Toast.makeText(getContext(),"failed to load maps", Toast.LENGTH_SHORT).show();
                        } else {
                            mMapAdapter.setMaps(maps);
                            mMapAdapter.notifyDataSetChanged();
                            mSwipeRefreshWidget.finishRefresh(true);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void setMapOnClicked(Map map) {
        mLoadingView.setLoading(true);
        ThreadPoolUtil.runAsync(new CommonCallback() {
            @Override
            public void run() {
                int retCode = 0;
                boolean succ = map.loadDetail();
                if (succ) {
                    succ = AXRobotPlatform.getInstance().setCurrentMap(map.getUid(), new Pose());
                    if (!succ) {
                        retCode = 2;
                    }
                } else {
                    retCode = 1;
                }

                int finalRetCode = retCode;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalRetCode == 0) {
                            Intent intent = new Intent();
                            intent.putExtra("url", map.getUrl());
                            intent.putExtra("id",map.getId());
                            intent.setClass(getActivity(), MapDetailActivity.class);
                            MapDetailActivity.mSelMap = map;
                            startActivity(intent);
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append("failed to ");
                            sb.append(finalRetCode == 2 ? "set map " : "get map detail, map id is ");
                            sb.append(map.getId());
                            Toast.makeText(getContext(), sb.toString(), Toast.LENGTH_SHORT).show();
                        }
                        mLoadingView.setLoading(false);
                    }
                });
            }
        });
    }
}

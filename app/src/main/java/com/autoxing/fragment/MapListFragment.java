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
import com.autoxing.x.util.CommonCallBack;
import com.autoxing.x.util.ThreadPoolUtil;
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
        ThreadPoolUtil.run(new CommonCallBack() {
            @Override
            public void run() {
                List<Map> maps = AXRobotPlatform.getInstance().getMaps();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (maps == null) {
                            mSwipeRefreshWidget.finishRefresh(false);
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
        ThreadPoolUtil.run(new CommonCallBack() {
            @Override
            public void run() {
                boolean succ = AXRobotPlatform.getInstance().setCurrentMap(map, null);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (succ) {
                            Intent intent = new Intent();
                            intent.putExtra("url", map.getUrl());
                            intent.putExtra("id",map.getId());
                            intent.setClass(getActivity(), MapDetailActivity.class);
                            MapDetailActivity.selMap = map;
                            startActivity(intent);
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append("failed to set map");
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

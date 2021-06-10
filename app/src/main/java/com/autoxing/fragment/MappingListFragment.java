package com.autoxing.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.autoxing.view.SaveMapDialog;
import com.autoxing.adapter.IMappingAdapterListener;
import com.autoxing.adapter.MappingRvAdapter;
import com.autoxing.controller.R;
import com.autoxing.robot_core.AXRobotPlatform;
import com.autoxing.robot_core.bean.Map;
import com.autoxing.robot_core.bean.Mapping;
import com.autoxing.x.util.CommonCallBack;
import com.autoxing.x.util.ThreadPoolUtil;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.buraktamturk.loadingview.LoadingView;

import java.util.List;

public class MappingListFragment extends Fragment implements IMappingAdapterListener {

    private View mLayout = null;
    private LoadingView mLoadingView;
    private RefreshLayout mSwipeRefreshWidget;
    private RecyclerView mRvMap;
    private MappingRvAdapter mMappingAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mLayout == null) {
            mLayout = inflater.inflate(R.layout.mapping_list_layout, container,false);
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
        mSwipeRefreshWidget = view.findViewById(R.id.swipe_refresh_widget);
        mRvMap = view.findViewById(R.id.rv_main);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRvMap.setLayoutManager(layoutManager);
        mRvMap.setItemAnimator(new DefaultItemAnimator());

        mRvMap.setHasFixedSize(true);

        mMappingAdapter = new MappingRvAdapter(getContext());
        mMappingAdapter.setListener(this);
        mRvMap.setAdapter(mMappingAdapter);
    }

    private void initData() {
        initMappingTasks();
    }

    private void setListener() {
        mSwipeRefreshWidget.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                initMappingTasks();
            }
        });
    }

    private void initMappingTasks() {
        ThreadPoolUtil.run(new CommonCallBack() {
            @Override
            public void run() {
                List<Mapping> mappings = AXRobotPlatform.getInstance().getMappingTasks();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mappings == null) {
                            mSwipeRefreshWidget.finishRefresh(false);
                        } else {
                            mMappingAdapter.setMappingTasks(mappings);
                            mMappingAdapter.notifyDataSetChanged();
                            mSwipeRefreshWidget.finishRefresh(true);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void setMappingOnClicked(Mapping mapping) {
        showEditDialog(mapping);
    }

    private void showEditDialog(Mapping mapping) {
        final SaveMapDialog dialog = new SaveMapDialog(getActivity());
        dialog.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.btn_save_pop:
                        mLoadingView.setLoading(true);
                        String mapName = dialog.getMapName().getText().toString();

                        ThreadPoolUtil.run(new CommonCallBack() {
                            @Override
                            public void run() {
                                Map map = mapping.saveToMap(mapName);

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(map != null ? "succed" : "failed");
                                        sb.append(" to save mapping ");
                                        sb.append(mapping.getId());
                                        sb.append(" to map <");
                                        sb.append(mapName);
                                        sb.append(">");
                                        Toast.makeText(getActivity(), sb.toString(),1200).show();
                                        dialog.dismiss();
                                        mLoadingView.setLoading(false);
                                    }
                                });
                            }
                        });

                        break;
                    default:break;
                }
            }
        });
        dialog.show();
    }
}

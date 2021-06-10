package com.autoxing.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.autoxing.controller.R;
import com.autoxing.robot_core.bean.Map;
import com.autoxing.util.GlobalUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MapRvAdapter extends RecyclerView.Adapter<MapRvAdapter.MapTvHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<Map> mMaps;
    private IMapAdapterListener mListener;

    public MapRvAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setMaps(List<Map> maps) {
        mMaps = maps;
    }

    public void setListener(IMapAdapterListener listener) {
        mListener = listener;
    }

    @NonNull
    @NotNull
    @Override
    public MapTvHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new MapRvAdapter.MapTvHolder(mLayoutInflater.inflate(R.layout.map_rv_card_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MapTvHolder holder, int position) {
        Map map = mMaps.get(position);
        holder.mTitle.setText("Map " + map.getId() + ": " + map.getMapName());

        String dateStr = GlobalUtil.convertTimestampToFormatStr(map.getCreateTime());
        holder.mDate.setText(dateStr);

        holder.mSetMap.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.setMapOnClicked(map);
            }
        });

        Glide.with(mContext)
                .load(map.getUrl() + "/thumbnail")
                .apply(new RequestOptions().placeholder(R.drawable.ic_launcher_background).diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(holder.mMap);
    }

    @Override
    public int getItemCount() {
        return mMaps == null ? 0 : mMaps.size();
    }

    class MapTvHolder extends RecyclerView.ViewHolder {
        TextView mTitle;
        TextView mDate;
        ImageView mMap;
        Button mSetMap;

        public MapTvHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            mTitle = (TextView)itemView.findViewById(R.id.tv_item_title);
            mDate = (TextView)itemView.findViewById(R.id.tv_item_date);
            mMap = (ImageView)itemView.findViewById(R.id.iv_map);
            mSetMap = (Button)itemView.findViewById(R.id.btn_use_map);
        }
    }
}

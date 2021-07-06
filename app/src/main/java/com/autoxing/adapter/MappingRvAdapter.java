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
import com.autoxing.robot_core.bean.Mapping;
import com.autoxing.robot_core.util.NetUtil;
import com.autoxing.util.GlobalUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MappingRvAdapter extends RecyclerView.Adapter<MappingRvAdapter.MappingTvHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<Mapping> mMappingLists;
    private IMappingAdapterListener mListener;

    public MappingRvAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        }

    public void setMappingTasks(List<Mapping> mappings) {
        mMappingLists = mappings;
        }

    public void setListener(IMappingAdapterListener listener) {
        mListener = listener;
        }

    @NonNull
    @NotNull
    @Override
    public MappingTvHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new MappingRvAdapter.MappingTvHolder(mLayoutInflater.inflate(R.layout.mapping_rv_card_item, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MappingTvHolder holder, int position) {
        Mapping mapping = mMappingLists.get(position);
        holder.mTitle.setText("Mapping " + mapping.getId());

        String dateStr = GlobalUtil.convertTimestampToFormatStr(mapping.getStartTime());
        holder.mDate.setText(dateStr);

        holder.mSetMap.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.setMappingOnClicked(mapping);
            }
        });

        GlideUrl glideUrl = new GlideUrl(mapping.getUrl() + "/thumbnail", new LazyHeaders.Builder()
                .addHeader(NetUtil.getServiceTokenKey(), NetUtil.getServiceTokenValue())
                .build());

        Glide.with(mContext)
        .load(glideUrl)
        .apply(new RequestOptions().placeholder(R.drawable.ic_launcher_background).diskCacheStrategy(DiskCacheStrategy.NONE))
        .into(holder.mMap);
    }

    @Override
    public int getItemCount() {
        return mMappingLists == null ? 0 : mMappingLists.size();
    }

    class MappingTvHolder extends RecyclerView.ViewHolder {
        TextView mTitle;
        TextView mDate;
        ImageView mMap;
        Button mSetMap;

        public MappingTvHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            mTitle = (TextView)itemView.findViewById(R.id.tv_item_title);
            mDate = (TextView)itemView.findViewById(R.id.tv_item_date);
            mMap = (ImageView)itemView.findViewById(R.id.iv_map);
            mSetMap = (Button)itemView.findViewById(R.id.btn_save_to_map);
        }
    }
}


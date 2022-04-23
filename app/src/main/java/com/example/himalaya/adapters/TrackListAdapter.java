package com.example.himalaya.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.R;
import com.example.himalaya.utils.FormatBigNum;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.ViewHolder> {

    private List<Track> mDetailData = new ArrayList<>();

    private SimpleDateFormat mUpdateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private SimpleDateFormat mDurationFormat = new SimpleDateFormat("mm:ss");
    private ItemClickListener mItemClickListener = null;
    private OnTrackItemLongClickListener mItemLongClickListener = null;


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemVIew = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_detail, parent, false);
        return new ViewHolder(itemVIew);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // 找到控件
        View itemView = holder.itemView;
        itemView.setTag(position);
        // 顺序Id
        TextView orderTv = itemView.findViewById(R.id.order_text_tv);
        // 标题
        TextView titleTv = itemView.findViewById(R.id.detail_item_title_tv);
        // 播放次数
        TextView playCountTv = itemView.findViewById(R.id.detail_item_play_count_tv);
        // 时长
        TextView durationTv = itemView.findViewById(R.id.detail_item_duration_tv);
        // 更新日期
        TextView updateDateTv = itemView.findViewById(R.id.detail_item_update_time_tv);

        // 设置数据
        Track track = mDetailData.get(position);
        orderTv.setText(String.valueOf(position + 1));
        titleTv.setText(track.getTrackTitle());
        playCountTv.setText(FormatBigNum.formatBigNum(String.valueOf(track.getPlayCount()), false));
        String duration = mDurationFormat.format(track.getDuration() * 1000);
        durationTv.setText(duration);
        String updateTime = mUpdateFormat.format(track.getUpdatedAt());
        updateDateTv.setText(updateTime);


        // 设置itemView的点击事件
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toast.makeText(view.getContext(), "you clicked " + view.getTag() + " item.", Toast.LENGTH_SHORT).show();
                if (mItemClickListener != null) {
                    mItemClickListener.itemClick(mDetailData, position);
                }
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mItemLongClickListener != null) {
                    mItemLongClickListener.onItemLongClick(track);
                }
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDetailData.size();
    }

    public void setData(List<Track> tracks) {
        mDetailData.clear();
        mDetailData.addAll(tracks);        // 更新UI
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public interface ItemClickListener {
        public void itemClick(List<Track> detailData, int position);
    }

    public void setOnTrackItemLongClickListener(OnTrackItemLongClickListener listener) {
        this.mItemLongClickListener = listener;
    }

    public interface OnTrackItemLongClickListener {
        void onItemLongClick(Track track);
    }
}

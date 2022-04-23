package com.example.himalaya.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.R;
import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.views.PlayerListPopupWindow;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder> {

    private List<Track> mData = new ArrayList<>();
    private int playingIndex = 0;
    private int prePlayingIndex = 0;
    private PlayerListPopupWindow.PlayListItemClickListener mItemClickListener = null;

    @NonNull
    @Override
    public PlayListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 设置布局
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_play_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // 设置数据
        Track track = mData.get(position);
        TextView trackTitleTv = holder.itemView.findViewById(R.id.track_title_tv);
        trackTitleTv.setText(track.getTrackTitle());
        // 设置字体颜色
        trackTitleTv.setTextColor(BaseApplication.getContext().getResources().getColor(
                playingIndex == position ? R.color.main_color : R.color.black));
        // 找到播放状态
        View playingIconView = holder.itemView.findViewById(R.id.playing_icon_iv);
        playingIconView.setVisibility(playingIndex == position ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<Track> data) {
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void setCurrentPlayPosition(int position) {
        prePlayingIndex = playingIndex;
        playingIndex = position;
        notifyItemChanged(prePlayingIndex);
        notifyItemChanged(playingIndex);

    }

    public void setOnItemClickListener(PlayerListPopupWindow.PlayListItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

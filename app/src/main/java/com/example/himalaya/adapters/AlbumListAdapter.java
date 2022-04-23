package com.example.himalaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.himalaya.R;
import com.example.himalaya.utils.FormatBigNum;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.ArrayList;
import java.util.List;

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.ViewHolder> {

    private static final String TAG = "RecommendListAdapter";
    private List<Album> mData = new ArrayList<>();
    private OnAlbumItemClickListener mItemClickListener = null;
    private OnAlbumItemLongClickListener mItemLongClickListener = null;

    @NonNull
    @Override
    public AlbumListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 这里找到view
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommend, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumListAdapter.ViewHolder holder, int position) {
        // 这里设置数据
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.d(TAG, "itemView clicked --> " + view.getTag());
                if (mItemClickListener != null) {
                    int clickPosition = (int) view.getTag();
                    mItemClickListener.onItemClick(clickPosition, mData.get(clickPosition));
                }
            }
        });
        holder.setData(mData.get(position));

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // true表示消费掉该事件
                if (mItemLongClickListener != null) {
                    int clickPosition = (int) view.getTag();
                    mItemLongClickListener.onItemLongClick(mData.get(clickPosition));
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        // 返回要显示的个数
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    public void setData(List<Album> albumList) {
        if (mData != null) {
            mData.clear();
            mData.addAll(albumList);
        }
        // 通知更新UI
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setData(Album album) {
            // 找到各个控件，并设置数据
            // 专辑封面
            ImageView albumCoverIv = itemView.findViewById(R.id.album_cover_iv);
            // title
            TextView albumTitleTv = itemView.findViewById(R.id.album_title_tv);
            // 描述
            TextView albumDescriptionTv = itemView.findViewById(R.id.album_description_tv);
            // 播放量
            TextView albumPlayCountTv = itemView.findViewById(R.id.album_play_count_tv);
            // 专辑内容数量
            TextView albumContentCountTv = itemView.findViewById(R.id.album_content_count_tv);

            String coverUrlLarge = album.getCoverUrlLarge();
            if (coverUrlLarge != null) {
                Glide.with(itemView.getContext()).load(coverUrlLarge).into(albumCoverIv);
            } else {
                albumCoverIv.setImageResource(R.mipmap.logo);
            }


            albumTitleTv.setText(album.getAlbumTitle());
            albumDescriptionTv.setText(album.getAlbumIntro());
            albumPlayCountTv.setText(FormatBigNum.formatBigNum(album.getPlayCount() + "", false));
            albumContentCountTv.setText(album.getIncludeTrackCount() + "");
        }
    }

    public void setOnAlbumItemClickListener(OnAlbumItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public interface OnAlbumItemClickListener {
        void onItemClick(int position, Album album);
    }


    public void setOnAlbumItemLongClickListener(OnAlbumItemLongClickListener listener) {
        this.mItemLongClickListener = listener;
    }

    public interface OnAlbumItemLongClickListener {
        void onItemLongClick(Album album);
    }
}

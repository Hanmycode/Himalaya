package com.example.himalaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.R;
import com.example.himalaya.base.BaseApplication;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;

import java.util.ArrayList;
import java.util.List;

public class SearchSuggestAdapter extends RecyclerView.Adapter<SearchSuggestAdapter.ViewHolder> {

    private List<QueryResult> mData = new ArrayList<>();
    private itemClickListener mItemClickListener = null;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_suggest, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView suggestText = holder.itemView.findViewById(R.id.search_suggest_item_tv);
        QueryResult queryResult = mData.get(position);
        suggestText.setText(queryResult.getKeyword());
        // 设置点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(queryResult.getKeyword());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * 设置数据
     *
     * @param queryResults
     */
    public void setData(List<QueryResult> queryResults) {
        mData.clear();
        mData.addAll(queryResults);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


    public void setItemClickListener(itemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public interface itemClickListener {
        void onItemClick(String keyword);
    }
}

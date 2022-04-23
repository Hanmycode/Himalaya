package com.example.himalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

public interface ISubscriptionViewCallback {

    /**
     * 添加后通知UI
     *
     * @param isSuccess
     */
    void onAddResult(boolean isSuccess);

    /**
     * 删除后通知UI
     *
     * @param isSuccess
     */
    void onDeleteResult(boolean isSuccess);

    /**
     * 订阅结果加载后通知UI
     * @param albums
     */
    void onSubscriptionLoaded(List<Album> albums);

    /**
     * 订阅数量达到限制
     */
    void onSubReachLimit();
}

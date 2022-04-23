package com.example.himalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

public interface IAlbumDetailViewCallback {

    /**
     * 加载出专辑的详情内容
     *
     * @param tracks
     */
    void onDetailListLoaded(List<Track> tracks);

    /**
     * 把Album传给UI使用
     *
     * @param album
     */
    void onAlbumLoaded(Album album);

    /**
     *网络错误
     */
    void onNetworkError(int errorCode, String errorMsg);

    /**
     * 加载更多的结果
     * @param size >0表示刷新成功，<0表示失败
     */
    void onLoadMoreFinished(int size);

    /**
     * 下拉刷新的结果
     * @param size >0表示刷新成功，<0表示失败
     */
    void onRefreshFinished(int size);

}



package com.example.himalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

public interface IHistoryDaoCallback {

    /**
     * 添加历史的结果通知给presenter
     *
     * @param isSuccess
     */
    void onHistoryAdd(boolean isSuccess);

    /**
     * 删除历史的结果通知给presenter
     *
     * @param isSuccess
     */
    void onHistoryDel(boolean isSuccess);

    /**
     * 清除历史的结果通知给presenter
     */
    void onHistoryClean(boolean isSuccess);

    /**
     * 获取历史的结果通知给presenter
     */
    void onHistoriesLoaded(List<Track> tracks);
}

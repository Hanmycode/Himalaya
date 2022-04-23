package com.example.himalaya.interfaces;

import com.example.himalaya.base.IBasePresenter;
import com.ximalaya.ting.android.opensdk.model.track.Track;

public interface IHistoryPresenter extends IBasePresenter<IHistoryViewCallback> {

    /**
     *获取历史记录
     */
    void listHistories();

    /**
     * 添加历史
     * @param track
     */
    void addHistory(Track track);

    /**
     * 删除历史
     * @param track
     */
    void delHistory(Track track);

    /**
     * 清除历史
     */
    void cleanHistory();



}

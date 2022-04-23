package com.example.himalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

public interface IPlayerViewCallback {

    /**
     * 开始播放
     */
    void onPlayStart();

    /**
     * 播放暂停
     */
    void onPlayPause();

    /**
     * 播放停止
     */
    void onPlayStop();

    /**
     * 播放错误
     */
    void onPlayError();

    /**
     * 播放上一首
     *
     * @param track
     */
    void onPlayPre(Track track);

    /**
     * 播放下一首
     *
     * @param track
     */
    void onPlayNext(Track track);

    /**
     * 播放列表加载完成
     *
     * @param list 播放列表数据
     */
    void onListLoaded(List<Track> list);

    /**
     * 改变播放模式
     *
     * @param playMode
     */
    void onPlayModeChange(XmPlayListControl.PlayMode playMode);

    /**
     * 进度条的改变
     *
     * @param currentProgress
     * @param total
     */
    void onProgressChange(long currentProgress, long total);

    /**
     * 广告正在加载
     */
    void onAdLoading();

    /**
     * 广告加载完成
     */
    void onAdFinished();

    /**
     * 更新当前音频
     * @param track 音频
     */
    void onUpdateTrack(Track track, int playIndex);

    /**
     * 通知UI更新播放列表顺序的文字和图标
     * @param isReverse
     */
    void updateListOrder(boolean isReverse);



}

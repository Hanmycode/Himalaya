package com.example.himalaya.interfaces;

import com.example.himalaya.base.IBasePresenter;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

public interface IPlayerPresenter extends IBasePresenter<IPlayerViewCallback> {

    /**
     * 播放
     */
    void play();

    /**
     * 暂停
     */
    void pause();

    /**
     * 停止
     */
    void stop();

    /**
     * 播放上一首
     */
    void playPre();

    /**
     * 播放下一首
     */
    void playNext();

    /**
     * 切换播放模式
     *
     * @param playMode
     */
    void switchPlayMode(XmPlayListControl.PlayMode playMode);

    /**
     * 获取播放列表
     */
    void getPlayList();

    /**
     * 根据音频在列表中的位置播放
     *
     * @param index 音频在列表中的位置
     */
    void playByIndex(int index);

    /**
     * 切换播放进度
     *
     * @param progress
     */
    void seekTo(int progress);

    /**
     * 判断播放器是否正在播放
     *
     * @return
     */
    boolean isPlaying();

    /**
     * 把播放器列表反转
     */
    void reversePlayList();

    /**
     * 根据Album的id播放第一个音频
     * @param id
     */
    void playByAlbumId(long id);

}

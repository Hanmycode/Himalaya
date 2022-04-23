package com.example.himalaya.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.himalaya.data.XimalayaAPI;
import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.interfaces.IPlayerPresenter;
import com.example.himalaya.interfaces.IPlayerViewCallback;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.advertis.Advertis;
import com.ximalaya.ting.android.opensdk.model.advertis.AdvertisList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.advertis.IXmAdsStatusListener;
import com.ximalaya.ting.android.opensdk.player.constants.PlayerConstants;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerPresenter implements IPlayerPresenter, IXmAdsStatusListener, IXmPlayerStatusListener {

    private List<IPlayerViewCallback> mIPlayerViewCallbacks = new ArrayList<>();

    private static final String TAG = "PlayerPresenter";
    private static volatile PlayerPresenter sInstance = null;
    private final XmPlayerManager mPlayerManager;
    private boolean isPlayListSet = false;
    private Track mCurrentTrack;
    private int mCurrentTrackIndex = 0;
    private boolean mIsReverse = false;
    public static final int DEFAULT_PLAY_INDEX = 0;

    private final SharedPreferences mPlayModeSp;
    // SharedPreferences的name、key
    public static final String PLAY_MODE_SP_NAME = "PlayMode";
    public static final String PLAY_MODE_SP_KEY = "currentPlayMode";
    private XmPlayListControl.PlayMode mCurPlayMode;
    private int mCurrProgressPosition = 0;
    private int mProgressDuration = 0;


    private PlayerPresenter() {
        mPlayerManager = XmPlayerManager.getInstance(BaseApplication.getContext());
        // 记住播放记录，每次播放时从上次记录的地方开始播放，而不是从头播放
        mPlayerManager.setBreakpointResume(true);
        // 注册广告相关接口
        mPlayerManager.addAdsStatusListener(this);
        // 注册播放器状态相关的接口
        mPlayerManager.addPlayerStatusListener(this);
        // 需要记录下当前的播放模式，用户下次播放时使用该播放模式
        mPlayModeSp = BaseApplication.getContext().getSharedPreferences(PLAY_MODE_SP_NAME, Context.MODE_PRIVATE);


    }

    public static PlayerPresenter getInstance() {
        if (sInstance == null) {
            synchronized (PlayerPresenter.class) {
                if (sInstance == null) {
                    sInstance = new PlayerPresenter();
                }
            }
        }
        return sInstance;
    }

    public void setPlayList(List<Track> list, int playIndex) {
        if (mPlayerManager != null) {
            mPlayerManager.setPlayList(list, playIndex);    //设置播放列表和播放声音的index,但是不会自动播放
            isPlayListSet = true;
            mCurrentTrack = list.get(playIndex);
            mCurrentTrackIndex = playIndex;
        } else {
            LogUtil.d(TAG, "mPlayerManager is null");
        }


    }

    /**
     * 判断现在是否存在播放列表
     *
     * @return
     */
    public boolean hasPlayList() {
        return isPlayListSet;
    }

    @Override
    public void play() {
        if (isPlayListSet) {
            mPlayerManager.play();
        }
    }

    @Override
    public void pause() {
        if (mPlayerManager != null) {
            mPlayerManager.pause();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void playPre() {
        // 播放前一个节目
        if (mPlayerManager != null) {
            mPlayerManager.playPre();
        }
    }

    @Override
    public void playNext() {
        // 播放后一个节目
        if (mPlayerManager != null) {
            mPlayerManager.playNext();
        }
    }

    @Override
    public void switchPlayMode(XmPlayListControl.PlayMode playMode) {
        if (mPlayerManager != null) {
            mPlayerManager.setPlayMode(playMode);
            for (IPlayerViewCallback iPlayerViewCallback : mIPlayerViewCallbacks) {
                iPlayerViewCallback.onPlayModeChange(playMode);
            }
        }
        // 把当前的播放模式保存到sp里去
        SharedPreferences.Editor edit = mPlayModeSp.edit();
        edit.putInt(PLAY_MODE_SP_KEY, playMode.ordinal());  // ordinal是Java枚举类的一个属性，对于枚举类中的多个枚举，ordinal保留了枚举们自上而下的顺序
        edit.commit();
    }

    @Override
    public void getPlayList() {
        if (mPlayerManager != null) {
            List<Track> playList = mPlayerManager.getPlayList();
            for (IPlayerViewCallback iPlayerViewCallback : mIPlayerViewCallbacks) {
                iPlayerViewCallback.onListLoaded(playList);
            }
        }
    }

    @Override
    public void playByIndex(int index) {
        // 播放器播放index的位置的音频
        if (mPlayerManager != null) {
            mPlayerManager.play(index);
        }
    }

    @Override
    public void seekTo(int progress) {
        // 更新播放器的进度
        mPlayerManager.seekTo(progress);
    }

    @Override
    public boolean isPlaying() {
        // 返回当前是否正在播放
        return mPlayerManager.isPlaying();
    }

    @Override
    public void reversePlayList() {
        // 把播放器列表反转
        List<Track> playList = mPlayerManager.getPlayList();
        Collections.reverse(playList);

        mIsReverse = !mIsReverse;


        // 参数1是播放列表，参数2是开始播放的下标
        mCurrentTrackIndex = playList.size() - mCurrentTrackIndex - 1;
        mPlayerManager.setPlayList(playList, mCurrentTrackIndex);
        // 更新UI
        mCurrentTrack = (Track) mPlayerManager.getCurrSound();
        for (IPlayerViewCallback iPlayerViewCallback : mIPlayerViewCallbacks) {
            iPlayerViewCallback.onListLoaded(playList);
            iPlayerViewCallback.onUpdateTrack(mCurrentTrack, mCurrentTrackIndex);
            iPlayerViewCallback.updateListOrder(mIsReverse);
        }
    }

    @Override
    public void playByAlbumId(long id) {
        // 1.获取到专辑的内容
        XimalayaAPI ximalayaAPI = XimalayaAPI.getInstance();
        ximalayaAPI.getAlbumDetail(new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
            // 2.把专辑内容设置给播放器
                List<Track> tracks = trackList.getTracks();
                if (tracks != null && tracks.size() > 0) {
                    mPlayerManager.setPlayList(tracks, DEFAULT_PLAY_INDEX);
                    isPlayListSet = true;
                    mCurrentTrack = tracks.get(DEFAULT_PLAY_INDEX);
                    mCurrentTrackIndex = DEFAULT_PLAY_INDEX;
                }

            }

            @Override
            public void onError(int i, String s) {
                Toast.makeText(BaseApplication.getContext(), "请求数据失败...", Toast.LENGTH_SHORT).show();
            }
        }, (int) id, 1);
        // 3.播放

    }

    @Override
    public void registerViewCallback(IPlayerViewCallback iPlayerViewCallback) {
        if (!mIPlayerViewCallbacks.contains(iPlayerViewCallback)) {
            mIPlayerViewCallbacks.add(iPlayerViewCallback);
        }
        // 更新之前，要让UI的pager有数据
        getPlayList();
        // 通知当前的节目
        iPlayerViewCallback.onUpdateTrack(mCurrentTrack, mCurrentTrackIndex);
        // 更新当前播放状态和进度
        handlePlayStatus(iPlayerViewCallback);
        iPlayerViewCallback.onProgressChange(mCurrProgressPosition, mProgressDuration);

        // 从sp里拿到当前的播放模式，默认是PLAY_MODEL_LIST的序号
        int modeIndex = mPlayModeSp.getInt(PLAY_MODE_SP_KEY, XmPlayListControl.PlayMode.PLAY_MODEL_LIST.ordinal());
        // 通过ordinal得到的序号来获取enum的相应播放模式
        mCurPlayMode = XmPlayListControl.PlayMode.getIndex(modeIndex);
        iPlayerViewCallback.onPlayModeChange(mCurPlayMode);

    }

    private void handlePlayStatus(IPlayerViewCallback iPlayerViewCallback) {
        int playerStatus = mPlayerManager.getPlayerStatus();
        // 根据状态调用接口的方法
        if (PlayerConstants.STATE_STARTED == playerStatus) {
            iPlayerViewCallback.onPlayStart();
        } else {
            iPlayerViewCallback.onPlayPause();
        }

    }

    @Override
    public void unRegisterViewCallback(IPlayerViewCallback iPlayerViewCallback) {
        mIPlayerViewCallbacks.remove(iPlayerViewCallback);
    }

    // ====================== 广告相关的回调方法 start ======================
    @Override
    public void onStartGetAdsInfo() {
        LogUtil.d(TAG, "onStartGetAdsInfo...");
    }

    @Override
    public void onGetAdsInfo(AdvertisList advertisList) {
        LogUtil.d(TAG, "onGetAdsInfo...");
    }

    @Override
    public void onAdsStartBuffering() {
        LogUtil.d(TAG, "onAdsStartBuffering...");
    }

    @Override
    public void onAdsStopBuffering() {
        LogUtil.d(TAG, "onAdsStopBuffering...");
    }

    @Override
    public void onStartPlayAds(Advertis advertis, int i) {
        LogUtil.d(TAG, "onStartPlayAds...");
    }

    @Override
    public void onCompletePlayAds() {
        LogUtil.d(TAG, "onCompletePlayAds...");
    }

    @Override
    public void onError(int what, int extra) {
        LogUtil.d(TAG, "onError what --> " + what + "onError extra --> " + extra);
    }

    // ====================== 广告相关的回调方法 end ======================
    //
    //
    // ====================== 播放器状态相关的回调方法 start ======================
    @Override
    public void onPlayStart() {
        LogUtil.d(TAG, "onPlayStart...");
        for (IPlayerViewCallback iPlayerViewCallback : mIPlayerViewCallbacks) {
            iPlayerViewCallback.onPlayStart();
        }
    }

    @Override
    public void onPlayPause() {
        LogUtil.d(TAG, "onPlayPause...");
        for (IPlayerViewCallback iPlayerViewCallback : mIPlayerViewCallbacks) {
            iPlayerViewCallback.onPlayPause();
        }
    }

    @Override
    public void onPlayStop() {
        LogUtil.d(TAG, "onPlayStop...");
        for (IPlayerViewCallback iPlayerViewCallback : mIPlayerViewCallbacks) {
            iPlayerViewCallback.onPlayStop();
        }
    }

    @Override
    public void onSoundPlayComplete() {
        LogUtil.d(TAG, "onSoundPlayComplete...");
    }

    @Override
    public void onSoundPrepared() {
        LogUtil.d(TAG, "onSoundPrepared...");
        // 设置播放模式
        mPlayerManager.setPlayMode(mCurPlayMode);
        if (mPlayerManager.getPlayerStatus() == PlayerConstants.STATE_PREPARED) {
            // 当播放器准备好后，就可以播放了
            mPlayerManager.play();
        }
    }

    @Override
    public void onSoundSwitch(PlayableModel lastModel, PlayableModel curModel) {
        // 当切换音频时
        LogUtil.d(TAG, "onSoundSwitch...");
        if (lastModel != null) {
            LogUtil.d(TAG, "lastModel -->　" + lastModel.getKind());
        }
        if (curModel != null) {
            LogUtil.d(TAG, "curModel -->　" + curModel.getKind());
        }

        mCurrentTrackIndex = mPlayerManager.getCurrentIndex();


        // 设置当前音频的标题
        if (curModel instanceof Track) {
            Track curTrack = (Track) curModel;
            mCurrentTrack = curTrack;
            // 保存播放记录
            HistoryPresenter historyPresenter = HistoryPresenter.getInstance();
            historyPresenter.addHistory(curTrack);

            for (IPlayerViewCallback callback : mIPlayerViewCallbacks) {
                callback.onUpdateTrack(mCurrentTrack, mCurrentTrackIndex);
            }

        }


    }

    @Override
    public void onBufferingStart() {
        LogUtil.d(TAG, "onBufferingStart...");
    }

    @Override
    public void onBufferingStop() {
        LogUtil.d(TAG, "onBufferingStop...");
    }

    @Override
    public void onBufferProgress(int progress) {
        LogUtil.d(TAG, "onBufferProgress..." + progress);
    }

    @Override
    public void onPlayProgress(int currPos, int duration) {
        this.mCurrProgressPosition = currPos;
        this.mProgressDuration = duration;
        // 单位是毫秒
        // LogUtil.d(TAG, "onPlayProgress...");
        for (IPlayerViewCallback iPlayerViewCallback : mIPlayerViewCallbacks) {
            iPlayerViewCallback.onProgressChange(currPos, duration);
        }

    }

    @Override
    public boolean onError(XmPlayerException e) {
        LogUtil.d(TAG, "onError e -->" + e);
        return false;
    }
    // ====================== 播放器状态相关的回调方法 start ======================

}

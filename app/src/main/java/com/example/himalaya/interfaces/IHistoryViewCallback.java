package com.example.himalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

public interface IHistoryViewCallback {

    /**
     * 历史记录加载到UI
     *
     * @param tracks
     */
    void onHistoryLoaded(List<Track> tracks);
}

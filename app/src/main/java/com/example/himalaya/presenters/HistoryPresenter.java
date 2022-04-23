package com.example.himalaya.presenters;

import android.util.Log;

import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.data.HistoryDao;
import com.example.himalaya.data.SubscriptionDao;
import com.example.himalaya.interfaces.IHistoryDaoCallback;
import com.example.himalaya.interfaces.IHistoryPresenter;
import com.example.himalaya.interfaces.IHistoryViewCallback;
import com.example.himalaya.utils.Constants;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HistoryPresenter implements IHistoryPresenter, IHistoryDaoCallback {

    private static final String TAG = "HistoryPresenter";
    private List<IHistoryViewCallback> mViewCallbacks = new ArrayList<>();

    private static volatile HistoryPresenter sInstance = null;
    private final HistoryDao mHistoryDao;
    private List<Track> mCurrentHistories = null;
    private boolean isOutOfSize = false;
    private Track mCurrTrack = null;

    private HistoryPresenter() {
        mHistoryDao = HistoryDao.getInstance();
        mHistoryDao.setCallback(this);
        listHistories();
    }

    public static HistoryPresenter getInstance() {
        if (sInstance == null) {
            synchronized (HistoryPresenter.class) {
                if (sInstance == null) {
                    sInstance = new HistoryPresenter();
                }
            }
        }
        return sInstance;
    }


    @Override
    public void listHistories() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                if (mHistoryDao != null) {
                    mHistoryDao.listHistories();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    /**
     * 设置历史记录最多保存100条，若已经有100条历史，再添加时就先删除最前面的，再添加当前的。
     *
     * @param track
     */
    @Override
    public void addHistory(Track track) {
        this.mCurrTrack = track;
        // 添加时要判断是否已经达到100条记录
        if (mCurrentHistories != null && mCurrentHistories.size() >= Constants.MAX_HISTORY_COUNT) {
            isOutOfSize = true;
            // 若已经有100条历史，再添加时就先删除最前面的，再添加当前的。
            delHistory(mCurrentHistories.get(mCurrentHistories.size() - 1));
        } else {
            doAddHistory(track);
        }
    }

    private void doAddHistory(Track track) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                if (mHistoryDao != null) {
                    mHistoryDao.addHistory(track);
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void delHistory(Track track) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                if (mHistoryDao != null) {
                    mHistoryDao.delHistory(track);
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void cleanHistory() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                if (mHistoryDao != null) {
                    mHistoryDao.cleanHistory();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void registerViewCallback(IHistoryViewCallback iHistoryViewCallback) {
        if (!mViewCallbacks.contains(iHistoryViewCallback)) {
            mViewCallbacks.add(iHistoryViewCallback);
        }
    }

    @Override
    public void unRegisterViewCallback(IHistoryViewCallback iHistoryViewCallback) {
        mViewCallbacks.remove(iHistoryViewCallback);
    }

    // ========== IHistoryDaoCallback impl start ==========
    @Override
    public void onHistoryAdd(boolean isSuccess) {
        listHistories();
    }

    @Override
    public void onHistoryDel(boolean isSuccess) {
        if (isOutOfSize) {
            // 说明此时历史记录达到100条限制
            addHistory(mCurrTrack);
        }
        listHistories();
    }

    @Override
    public void onHistoryClean(boolean isSuccess) {
        listHistories();
    }

    @Override
    public void onHistoriesLoaded(List<Track> tracks) {
        this.mCurrentHistories = tracks;
        LogUtil.d(TAG, "histories size --> " + tracks.size());
        // 从HistoryDao得到历史列表后，通知UI更新
        BaseApplication.getHandler().post(new Runnable() {
            @Override
            public void run() {
                // 在主线程更新UI
                for (IHistoryViewCallback viewCallback : mViewCallbacks) {
                    viewCallback.onHistoryLoaded(tracks);
                }
            }
        });
    }
    // ========== IHistoryDaoCallback impl end ==========
}

package com.example.himalaya.presenters;

import androidx.annotation.Nullable;

import com.example.himalaya.data.XimalayaAPI;
import com.example.himalaya.interfaces.IRecommendPresenter;
import com.example.himalaya.interfaces.IRecommendViewCallback;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;

import java.util.ArrayList;
import java.util.List;

public class RecommendPresenter implements IRecommendPresenter {

    private static final String TAG = "RecommendPresenter";

    private List<IRecommendViewCallback> mCallbacks = new ArrayList<>();

    // 单例模式（懒汉模式实现），DCL双重检查锁机制。
    // volatile关键字是为了防止指令重排序的。
    public static volatile RecommendPresenter sInstance = null;
    private List<Album> mCurrentRecommend = null;
    private List<Album> mRecommendList;

    private RecommendPresenter() {
    }

    /**
     * 获取单例对象
     */
    public static RecommendPresenter getInstance() {
        if (sInstance == null) {
            synchronized (RecommendPresenter.class) {
                if (sInstance == null) {
                    sInstance = new RecommendPresenter();
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取当前的推荐专辑列表
     * @return  使用之前要判空
     */
    public List<Album> getCurrentRecommend() {
        return mCurrentRecommend;
    }

    /**
     * 获取推荐内容(猜你喜欢)
     * 接口是：3.10.6 获取猜你喜欢专辑
     */
    @Override
    public void getRecommendList() {
        // 获取推荐内容
        // 封装参数,表示返回多少条结果数据，取值区间为[1,10]
        updateLoading();
        XimalayaAPI ximalayaAPI = XimalayaAPI.getInstance();
        ximalayaAPI.getRecommendList(new IDataCallBack<GussLikeAlbumList>() {
            @Override
            public void onSuccess(@Nullable GussLikeAlbumList gussLikeAlbumList) {
                // 主线程更新UI
                LogUtil.d(TAG, "thread name --> " + Thread.currentThread().getName());
                // 获取数据成功
                if (gussLikeAlbumList != null) {
                    mRecommendList = gussLikeAlbumList.getAlbumList();
                    // 获取数据成功后，更新UI
                    //UpRecommendUI(recommendList);
                    handleRecommendResult(mRecommendList);
                }
            }

            @Override
            public void onError(int i, String s) {
                // 获取数据失败
                LogUtil.d(TAG, "error int --> " + i + "error msg --> " + s);
                handleError();
            }
        });
    }

    private void handleError() {
        if (mCallbacks != null) {
            for (IRecommendViewCallback callback : mCallbacks) {
                callback.onNetworkError();
            }
        }
    }

    private void handleRecommendResult(List<Album> albumList) {
        // 通知UI更新
        if (albumList != null) {
            if (albumList.size() == 0) {
                for (IRecommendViewCallback callback : mCallbacks) {
                    callback.onEmpty();
                }
            } else {
                for (IRecommendViewCallback callback : mCallbacks) {
                    callback.onRecommendListLoaded(albumList);
                }
                this.mCurrentRecommend = albumList;
            }
        }
    }

    private void updateLoading() {
        for (IRecommendViewCallback callback : mCallbacks) {
            callback.onLoading();
        }
    }


    @Override
    public void registerViewCallback(IRecommendViewCallback callback) {
        if (mCallbacks != null && !mCallbacks.contains(callback)) {
            mCallbacks.add(callback);
        }
    }

    @Override
    public void unRegisterViewCallback(IRecommendViewCallback callback) {
        if (mCallbacks != null) {
            mCallbacks.remove(callback);
        }
    }
}

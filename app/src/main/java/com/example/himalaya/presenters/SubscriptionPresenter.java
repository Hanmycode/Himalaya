package com.example.himalaya.presenters;

import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.data.SubscriptionDao;
import com.example.himalaya.interfaces.ISubDaoCallback;
import com.example.himalaya.interfaces.ISubscriptionPresenter;
import com.example.himalaya.interfaces.ISubscriptionViewCallback;
import com.example.himalaya.utils.Constants;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SubscriptionPresenter implements ISubscriptionPresenter, ISubDaoCallback {

    private static volatile SubscriptionPresenter sInstance = null;
    private final SubscriptionDao mSubscriptionDao;
    private Map<Long, Album> mAlbumData = new HashMap<>();
    private List<ISubscriptionViewCallback> mViewCallbacks = new ArrayList<>();

    private SubscriptionPresenter() {
        mSubscriptionDao = SubscriptionDao.getInstance();
        mSubscriptionDao.setCallback(this);
    }

    private void getSubsList() {
        // rxJava会很方便的自动处理线程，因为这个读取数据库是耗时的动作，所以要使用多线程
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                // 只调用，不处理结果
                if (mSubscriptionDao != null) {
                    mSubscriptionDao.listAlbums();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public static SubscriptionPresenter getInstance() {
        if (sInstance == null) {
            synchronized (SubscriptionPresenter.class) {
                if (sInstance == null) {
                    sInstance = new SubscriptionPresenter();
                }
            }
        }
        return sInstance;
    }


    @Override
    public void addSubscription(final Album album) {
        // 判断订阅数量，不能超过100
        if (mAlbumData.size() >= Constants.MAX_SUB_COUNT) {
            // UI给出提示
            for (ISubscriptionViewCallback viewCallback : mViewCallbacks) {
                viewCallback.onSubReachLimit();
            }
            return;
        }
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                if (mSubscriptionDao != null) {
                    mSubscriptionDao.addAlbum(album);
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void deleteSubscription(final Album album) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                if (mSubscriptionDao != null) {
                    mSubscriptionDao.delAlbum(album);
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void getSubscriptionList() {
        getSubsList();
    }

    @Override
    public boolean isSub(Album album) {
        Album res = mAlbumData.get(album.getId());
        // 不为空时，表示已订阅
        return res != null;
    }

    @Override
    public void registerViewCallback(ISubscriptionViewCallback iSubscriptionViewCallback) {
        if (!mViewCallbacks.contains(iSubscriptionViewCallback)) {
            mViewCallbacks.add(iSubscriptionViewCallback);
        }
    }

    @Override
    public void unRegisterViewCallback(ISubscriptionViewCallback iSubscriptionViewCallback) {
            mViewCallbacks.remove(iSubscriptionViewCallback);
    }
    // ========== ISubDaoCallback impl start =============
    @Override
    public void onAddResult(boolean isSuccess) {
        getSubsList();
        // SubscriptionDao添加后的结果回调
        // 通知UI在主线程更新
        BaseApplication.getHandler().post(new Runnable() {
            @Override
            public void run() {
                for (ISubscriptionViewCallback callback : mViewCallbacks) {
                    callback.onAddResult(isSuccess);
                }
            }
        });

    }

    @Override
    public void onDelResult(boolean isSuccess) {
        getSubsList();
        // SubscriptionDao删除后的结果回调
        // 通知UI在主线程更新
        BaseApplication.getHandler().post(new Runnable() {
            @Override
            public void run() {
                for (ISubscriptionViewCallback callback : mViewCallbacks) {
                    callback.onDeleteResult(isSuccess);
                }
            }
        });
    }

    @Override
    public void onSubListLoaded(final List<Album> result) {
        // SubscriptionDao查询所有数据后的结果回调
        mAlbumData.clear();
        for (Album album : result) {
            mAlbumData.put(album.getId(), album);
        }
        // 通知UI在主线程更新
        BaseApplication.getHandler().post(new Runnable() {
            @Override
            public void run() {
                for (ISubscriptionViewCallback callback : mViewCallbacks) {
                    callback.onSubscriptionLoaded(result);
                }
            }
        });


    }
    // ========== ISubDaoCallback impl end =============
}

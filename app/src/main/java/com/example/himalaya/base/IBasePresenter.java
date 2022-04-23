package com.example.himalaya.base;

import com.example.himalaya.interfaces.IAlbumDetailViewCallback;

public interface IBasePresenter<T> {

    /**
     * 注册UI通知的接口
     *
     * @param
     */
    void registerViewCallback(T t);

    /**
     * 删除UI通知的接口
     *
     * @param
     */
    void unRegisterViewCallback(T t);

}

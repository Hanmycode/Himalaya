package com.example.himalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

public interface ISubDaoCallback {

    /**
     * 添加数据之后，把结果通知给Presenter
     *
     * @param isSuccess
     */
    void onAddResult(boolean isSuccess);


    /**
     * 删除数据之后，把结果通知给Presenter
     *
     * @param isSuccess
     */
    void onDelResult(boolean isSuccess);


    /**
     * 查询所有数据之后，把结果通知给Presenter
     *
     * @param result
     */
    void onSubListLoaded(List<Album> result);
}

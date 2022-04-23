package com.example.himalaya.interfaces;

import com.example.himalaya.base.IBasePresenter;

public interface IRecommendPresenter extends IBasePresenter<IRecommendViewCallback> {
    // 推荐界面的逻辑动作

    /**
     * 获取推荐内容
     */
    void getRecommendList();

    /**
     * 下拉刷新获取更多内容
     */
//    void pull2RefreshMore();

    /**
     * 上滑加载更多
     */
//    void loadMore();


}

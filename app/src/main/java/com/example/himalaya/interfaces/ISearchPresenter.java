package com.example.himalaya.interfaces;

import com.example.himalaya.base.IBasePresenter;

public interface ISearchPresenter extends IBasePresenter<ISearchViewCallback> {

    /**
     * 进行搜索
     *
     * @param keyword
     */
   void doSearch(String keyword);

    /**
     * 重新搜索
     */
   void reSearch();

    /**
     * 加载更多的搜索结果
     */
   void loadMore();

    /**
     * 获取热词
     */
   void getHotWord();

    /**
     * 获取输入的关键字的联想词
     *
     * @param keyword
     */
   void getSuggestKeyword(String keyword);


}

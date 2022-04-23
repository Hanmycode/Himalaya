package com.example.himalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;

import java.util.List;

public interface ISearchViewCallback {

    /**
     * 获取到搜索结果后的回调方法
     * @param result
     */
    void onSearchResultLoaded(List<Album> result);


    /**
     * 获取到推荐热词后
     *
     * @param hotWordList
     */
    void onHotWordLoaded(List<HotWord> hotWordList);

    /**
     * 加载到更多的结果后
     *
     * @param moreResults 结果
     * @param isOK        true表示加载到了更多，false表示没有更多了
     */
    void onLoadMoreResult(List<Album> moreResults, boolean isOK);


    /**
     * 获取到联想词后
     *
     * @param queryResults
     */
    void onGetSuggestWordLoaded(List<QueryResult> queryResults);

    /**
     * 错误通知回调
     * @param errorCode
     * @param errorMsg
     */
    void onError(int errorCode, String errorMsg);
}

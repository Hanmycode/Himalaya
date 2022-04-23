package com.example.himalaya.presenters;

import com.example.himalaya.data.XimalayaAPI;
import com.example.himalaya.interfaces.ISearchPresenter;
import com.example.himalaya.interfaces.ISearchViewCallback;
import com.example.himalaya.utils.Constants;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.SearchAlbumList;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.HotWordList;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;
import com.ximalaya.ting.android.opensdk.model.word.SuggestWords;

import java.util.ArrayList;
import java.util.List;

public class SearchPresenter implements ISearchPresenter {

    private static final String TAG = "SearchPresenter";
    private static volatile SearchPresenter sInstance = null;
    private List<Album> mSearchResult = new ArrayList<>();
    private boolean mIsLoaderMore = false;

    private List<ISearchViewCallback> mCallbacks = new ArrayList<>();
    // 当前输入的的搜索关键字
    private String mCurrentKeyword = null;
    private static final int DEFAULT_PAGE = 1;
    private int mCurrPage = DEFAULT_PAGE;

    private SearchPresenter() {
    }

    public static SearchPresenter getInstance() {
        if (sInstance == null) {
            synchronized (SearchPresenter.class) {
                if (sInstance == null) {
                    sInstance = new SearchPresenter();
                }
            }
        }
        return sInstance;
    }


    @Override
    public void doSearch(String keyword) {
        mCurrPage = DEFAULT_PAGE;
        mSearchResult.clear();
        // 用于搜索
        // 当网络不好的时候，用户会点击重新搜索
        this.mCurrentKeyword = keyword;
        search(keyword);
    }

    private void search(String keyword) {
        XimalayaAPI.getInstance().searchByKeyword(keyword, mCurrPage, new IDataCallBack<SearchAlbumList>() {
            @Override
            public void onSuccess(SearchAlbumList searchAlbumList) {
                List<Album> albums = searchAlbumList.getAlbums();
                mSearchResult.addAll(albums);
                if (albums != null) {
                    LogUtil.d(TAG, "search albums size --> " + albums.size());
                    if (mIsLoaderMore) {
                        for (ISearchViewCallback iSearchCallback : mCallbacks) {
                            iSearchCallback.onLoadMoreResult(mSearchResult, albums.size() != 0);
                        }
                        mIsLoaderMore = false;
                    } else {
                        for (ISearchViewCallback iSearchCallback : mCallbacks) {
                            iSearchCallback.onSearchResultLoaded(mSearchResult);
                        }
                    }
                } else {
                    LogUtil.d(TAG, "search albums is null");
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogUtil.d(TAG, "errorCode -> " + errorCode + "errorMsg --> " + errorMsg);
                for (ISearchViewCallback iSearchCallback : mCallbacks) {
                    if (mIsLoaderMore) {
                        iSearchCallback.onLoadMoreResult(mSearchResult, false);
                        mCurrPage--;
                        mIsLoaderMore = false;
                    } else {
                        iSearchCallback.onError(errorCode, errorMsg);
                    }
                }
            }
        });
    }


    @Override
    public void reSearch() {
        // 当网络不好的时候，用户会点击重新搜索
        search(mCurrentKeyword);
    }

    @Override
    public void loadMore() {
        //判断有没有必要进行加载更多
        if (mSearchResult.size() < Constants.COUNT_LIST_DEFAULT) {
            for (ISearchViewCallback iSearchCallback : mCallbacks) {
                iSearchCallback.onLoadMoreResult(mSearchResult, false);
            }
        } else {
            mIsLoaderMore = true;
            mCurrPage++;
            search(mCurrentKeyword);
        }
    }

    @Override
    public void getHotWord() {
        XimalayaAPI.getInstance().getHotWords(new IDataCallBack<HotWordList>() {
            @Override
            public void onSuccess(HotWordList hotWordList) {
                if (hotWordList != null) {
                    List<HotWord> hotWords = hotWordList.getHotWordList();
                    // LogUtil.d(TAG, "hotWords size --> " + hotWords.size());
                    for (ISearchViewCallback callback : mCallbacks) {
                        callback.onHotWordLoaded(hotWords);
                    }
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogUtil.d(TAG, "errorCode -> " + errorCode + "errorMsg --> " + errorMsg);
            }
        });
    }

    @Override
    public void getSuggestKeyword(String keyword) {
        XimalayaAPI.getInstance().getSuggestWords(keyword, new IDataCallBack<SuggestWords>() {
            @Override
            public void onSuccess(SuggestWords suggestWords) {
                if (suggestWords != null) {
                    List<QueryResult> keyWordList = suggestWords.getKeyWordList();
                    LogUtil.d(TAG, "suggest keyWordList size --> " + keyWordList.size());
                    for (ISearchViewCallback callback : mCallbacks) {
                        callback.onGetSuggestWordLoaded(keyWordList);
                    }
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogUtil.d(TAG, "errorCode -> " + errorCode + "errorMsg --> " + errorMsg);
            }
        });
    }


    @Override
    public void registerViewCallback(ISearchViewCallback iSearchViewCallback) {
        if (!mCallbacks.contains(iSearchViewCallback)) {
            mCallbacks.add(iSearchViewCallback);
        }
    }

    @Override
    public void unRegisterViewCallback(ISearchViewCallback iSearchViewCallback) {
        mCallbacks.remove(iSearchViewCallback);
    }

}

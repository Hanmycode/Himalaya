package com.example.himalaya.data;

import com.example.himalaya.utils.Constants;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;
import com.ximalaya.ting.android.opensdk.model.album.SearchAlbumList;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.model.word.HotWordList;
import com.ximalaya.ting.android.opensdk.model.word.SuggestWords;

import java.util.HashMap;
import java.util.Map;

public class XimalayaAPI {
    // 单例模式
    public static volatile XimalayaAPI sXimalayaAPI = null;

    private XimalayaAPI() {
    }

    public static XimalayaAPI getInstance() {
        if (sXimalayaAPI == null) {
            synchronized (XimalayaAPI.class) {
                if (sXimalayaAPI == null) {
                    sXimalayaAPI = new XimalayaAPI();
                }
            }
        }
        return sXimalayaAPI;
    }


    /**
     * 获取推荐内容
     *
     * @param callBack 请求结果的回调
     */
    public void getRecommendList(IDataCallBack<GussLikeAlbumList> callBack) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.LIKE_COUNT, Constants.COUNT_RECOMMEND_DEFAULT + "");
        CommonRequest.getGuessLikeAlbum(map, callBack);
    }

    /**
     * 根据专辑id获取专辑内容
     *
     * @param callBack  获取专辑详情的回调
     * @param albumId   专辑id
     * @param pageIndex 第几页
     */
    public void getAlbumDetail(IDataCallBack<TrackList> callBack, long albumId, int pageIndex) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.ALBUM_ID, albumId + "");
        map.put(DTransferConstants.SORT, "asc");
        map.put(DTransferConstants.PAGE, pageIndex + "");
        map.put(DTransferConstants.PAGE_SIZE, Integer.toString(Constants.COUNT_LIST_DEFAULT));
        CommonRequest.getTracks(map, callBack);

    }

    /**
     * 根据输入的关键字进行搜索
     *
     * @param keyword
     */
    public void searchByKeyword(String keyword, int page, IDataCallBack<SearchAlbumList> callback) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.SEARCH_KEY, keyword);
        map.put(DTransferConstants.PAGE, Integer.toString(page));
        map.put(DTransferConstants.PAGE_SIZE, Integer.toString(Constants.COUNT_LIST_DEFAULT));
        CommonRequest.getSearchedAlbums(map, callback);
    }

    /**
     * 获取推荐热词
     *
     * @param callback
     */
    public void getHotWords(IDataCallBack<HotWordList> callback) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.TOP, Integer.toString(Constants.COUNT_HOT_WORD_DEFAULT));
        CommonRequest.getHotWords(map, callback);
    }

    /**
     * 根据输入的关键字，获取联想词
     * @param keyword   关键字
     * @param callback  回调
     */
    public void getSuggestWords(String keyword, IDataCallBack<SuggestWords> callback) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.SEARCH_KEY, keyword);
        CommonRequest.getSuggestWord(map, callback);
    }


}

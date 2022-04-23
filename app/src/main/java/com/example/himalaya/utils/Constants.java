package com.example.himalaya.utils;

public class Constants {

    // 获取推荐列表的专辑数量
    public static int COUNT_RECOMMEND_DEFAULT = 10;

    // 默认列表请求数量
    public static int COUNT_LIST_DEFAULT = 50;

    // 默认获取热词的数量
    public static int COUNT_HOT_WORD_DEFAULT = 6;

    // 数据库相关的常量
    // 数据库名
    public static final String DB_NAME = "ximalaya.db";
    // 数据库版本
    public static  final int DB_VERSION = 1;

    // 订阅的表名等等
    public static final String SUB_TB_NAME = "tb_subscription";
    public static final String SUB_TB_ID = "_id";
    public static final String SUB_TB_COVER_URL = "coverUrl";
    public static final String SUB_TB_TITLE = "title";
    public static final String SUB_TB_DESCRIPTION = "description";
    public static final String SUB_TB_PLAY_COUNT = "playCount";
    public static final String SUB_TB_TRACKS_COUNT = "tracksCount";
    public static final String SUB_TB_AUTHOR_NAME = "authorName";
    public static final String SUB_TB_ALBUM_ID = "albumId";
    //订阅最多个数
    public static final int MAX_SUB_COUNT = 100;

    // 历史记录的表名等等
    public static final String HISTORY_TB_NAME = "tb_history";
    public static final String HISTORY_TB_ID = "_id";
    public static final String HISTORY_TB_TRACK_ID = "trackId";
    public static final String HISTORY_TB_TRACK_KIND = "trackKind";
    public static final String HISTORY_TB_TITLE = "title";
    public static final String HISTORY_TB_PLAY_COUNT = "playCount";
    public static final String HISTORY_TB_DURATION = "duration";
    public static final String HISTORY_TB_UPDATE_TIME = "updateTime";
    public static final String HISTORY_TB_COVER_URL = "coverUrl";
    //最大的历史记录数
    public static final int MAX_HISTORY_COUNT = 100;

}

package com.example.himalaya.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.himalaya.utils.Constants;
import com.example.himalaya.utils.LogUtil;

public class XimalayaDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "XimalayaDBHelper";

    public XimalayaDBHelper(@Nullable Context context) {
        // name数据库名字， factory游标工厂， version版本号
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtil.d(TAG, "db onCreate...");
        // 创建表
        // 创建订阅表
        String createSubTbSql = "create table " + Constants.SUB_TB_NAME + "(" +
                Constants.SUB_TB_ID + " integer primary key autoincrement," +
                Constants.SUB_TB_COVER_URL + " varchar," +
                Constants.SUB_TB_TITLE + " varchar," +
                Constants.SUB_TB_DESCRIPTION + " varchar," +
                Constants.SUB_TB_PLAY_COUNT + " integer," +
                Constants.SUB_TB_TRACKS_COUNT + " integer," +
                Constants.SUB_TB_AUTHOR_NAME + " varchar," +
                Constants.SUB_TB_ALBUM_ID + " integer" +
                ")";
        db.execSQL(createSubTbSql);
        // 创建历史记录表
        String createHistoryTbSql = "create table " + Constants.HISTORY_TB_NAME + "(" +
                Constants.HISTORY_TB_ID + " integer primary key autoincrement," +
                Constants.HISTORY_TB_TRACK_ID + " integer," +
                Constants.HISTORY_TB_TRACK_KIND + " varchar," +
                Constants.HISTORY_TB_TITLE + " varchar," +
                Constants.HISTORY_TB_COVER_URL + " varchar," +
                Constants.HISTORY_TB_PLAY_COUNT + " integer," +
                Constants.HISTORY_TB_DURATION + " integer," +
                Constants.HISTORY_TB_UPDATE_TIME + " integer" +
                ")";
        db.execSQL(createHistoryTbSql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

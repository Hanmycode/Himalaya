package com.example.himalaya.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.interfaces.IHistoryDao;
import com.example.himalaya.interfaces.IHistoryDaoCallback;
import com.example.himalaya.utils.Constants;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.Announcer;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

public class HistoryDao implements IHistoryDao {

    private static final String TAG = "HistoryDao";
    private static volatile HistoryDao sInstance = null;
    private final XimalayaDBHelper mDbHelper;
    private IHistoryDaoCallback mHistoryDaoCallback = null;
    private Object mLock = new Object();

    private HistoryDao() {
        mDbHelper = new XimalayaDBHelper(BaseApplication.getContext());
    }

    public static HistoryDao getInstance() {
        if (sInstance == null) {
            synchronized (HistoryDao.class) {
                if (sInstance == null) {
                    sInstance = new HistoryDao();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void setCallback(IHistoryDaoCallback callback) {
        this.mHistoryDaoCallback = callback;
    }

    @Override
    public void addHistory(Track track) {
        synchronized (mLock) {
            SQLiteDatabase db = null;
            boolean isSuccess = false;
            try {
                db = mDbHelper.getWritableDatabase();
                // 添加之前先删除
                db.delete(Constants.HISTORY_TB_NAME, Constants.HISTORY_TB_TRACK_ID + "=?", new String[]{String.valueOf(track.getDataId())});
                db.beginTransaction();
                // 封装数据
                ContentValues values = new ContentValues();
                values.put(Constants.HISTORY_TB_TRACK_ID, track.getDataId());
                values.put(Constants.HISTORY_TB_TRACK_KIND, track.getKind());
                values.put(Constants.HISTORY_TB_TITLE, track.getTrackTitle());
                values.put(Constants.HISTORY_TB_PLAY_COUNT, track.getPlayCount());
                values.put(Constants.HISTORY_TB_DURATION, track.getDuration());
                values.put(Constants.HISTORY_TB_UPDATE_TIME, track.getUpdatedAt());
                values.put(Constants.HISTORY_TB_COVER_URL, track.getCoverUrlLarge());
                // 插入数据
                db.insert(Constants.HISTORY_TB_NAME, null, values);
                db.setTransactionSuccessful();
                isSuccess = true;
            } catch (Exception e) {
                isSuccess = false;
                e.printStackTrace();
            } finally {
                if (db != null && db.inTransaction()) {
                    db.endTransaction();
                    db.close();
                }
                // 添加数据之后，把结果通知给Presenter
                if (mHistoryDaoCallback != null) {
                    mHistoryDaoCallback.onHistoryAdd(isSuccess);
                }
            }
        }
    }

    @Override
    public void delHistory(Track track) {
        synchronized (mLock) {
            SQLiteDatabase db = null;
            boolean isDelSuccess = false;
            try {
                db = mDbHelper.getWritableDatabase();
                db.beginTransaction();
                int delete = db.delete(Constants.HISTORY_TB_NAME, Constants.HISTORY_TB_TRACK_ID + "=?", new String[]{String.valueOf(track.getDataId())});
                LogUtil.d(TAG, "delete count --> " + delete);
                db.setTransactionSuccessful();
                isDelSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
                isDelSuccess = false;
            } finally {
                if (db != null && db.inTransaction()) {
                    db.endTransaction();
                    db.close();
                }
                // 删除数据之后，把结果通知给Presenter
                if (mHistoryDaoCallback != null) {
                    mHistoryDaoCallback.onHistoryDel(isDelSuccess);
                }
            }
        }
    }

    @Override
    public void cleanHistory() {
        synchronized (mLock) {
            SQLiteDatabase db = null;
            boolean isDelSuccess = false;
            try {
                db = mDbHelper.getWritableDatabase();
                db.beginTransaction();
                int delete = db.delete(Constants.HISTORY_TB_NAME, null, null);
                LogUtil.d(TAG, "delete count --> " + delete);
                db.setTransactionSuccessful();
                isDelSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
                isDelSuccess = false;
            } finally {
                if (db != null && db.inTransaction()) {
                    db.endTransaction();
                    db.close();
                }
                // 删除数据之后，把结果通知给Presenter
                if (mHistoryDaoCallback != null) {
                    mHistoryDaoCallback.onHistoryClean(isDelSuccess);
                }
            }
        }
    }

    @Override
    public void listHistories() {
        synchronized (mLock) {
            // 从数据库中查出所有的历史记录
            SQLiteDatabase db = null;
            List<Track> queryResult = new ArrayList<>();
            try {
                db = mDbHelper.getReadableDatabase();
                db.beginTransaction();
                Cursor cursor = db.query(Constants.HISTORY_TB_NAME, null, null, null, null, null, "_id desc");
                // 封装数据
                while (cursor.moveToNext()) {
                    Track track = new Track();
                    int trackId = cursor.getInt(cursor.getColumnIndex(Constants.HISTORY_TB_TRACK_ID));
                    track.setDataId(trackId);
                    String trackKind = cursor.getString(cursor.getColumnIndex(Constants.HISTORY_TB_TRACK_KIND));
                    track.setKind(trackKind);
                    String trackTitle = cursor.getString(cursor.getColumnIndex(Constants.HISTORY_TB_TITLE));
                    track.setTrackTitle(trackTitle);
                    int playCount = cursor.getInt(cursor.getColumnIndex(Constants.HISTORY_TB_PLAY_COUNT));
                    track.setPlayCount(playCount);
                    int duration = cursor.getInt(cursor.getColumnIndex(Constants.HISTORY_TB_DURATION));
                    track.setDuration(duration);
                    long updateTime = cursor.getLong(cursor.getColumnIndex(Constants.HISTORY_TB_UPDATE_TIME));
                    track.setUpdatedAt(updateTime);
                    String coverUrl = cursor.getString(cursor.getColumnIndex(Constants.HISTORY_TB_COVER_URL));
                    track.setCoverUrlLarge(coverUrl);
                    track.setCoverUrlMiddle(coverUrl);
                    track.setCoverUrlSmall(coverUrl);
                    queryResult.add(track);
                }
                LogUtil.d(TAG, "query size -->  " + queryResult.size());
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (db != null) {
                    db.endTransaction();
                    db.close();
                }
                // 查询所有数据之后，把结果通知给Presenter
                if (mHistoryDaoCallback != null) {
                    mHistoryDaoCallback.onHistoriesLoaded(queryResult);
                }
            }
        }
    }
}

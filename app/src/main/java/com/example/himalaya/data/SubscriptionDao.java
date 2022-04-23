package com.example.himalaya.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.interfaces.ISubDao;
import com.example.himalaya.interfaces.ISubDaoCallback;
import com.example.himalaya.utils.Constants;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.Announcer;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionDao implements ISubDao {

    private static final String TAG = "SubscriptionDao";
    private static volatile SubscriptionDao sInstance = null;
    private final XimalayaDBHelper mDbHelper;
    private ISubDaoCallback mCallback = null;

    private SubscriptionDao() {
        mDbHelper = new XimalayaDBHelper(BaseApplication.getContext());
    }

    public static SubscriptionDao getInstance() {
        if (sInstance == null) {
            synchronized (SubscriptionDao.class) {
                if (sInstance == null) {
                    sInstance = new SubscriptionDao();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void setCallback(ISubDaoCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void addAlbum(Album album) {
        SQLiteDatabase db = null;
        boolean isAddSuccess = false;
        try {
            db = mDbHelper.getWritableDatabase();
            db.beginTransaction();
            ContentValues contentValues = new ContentValues();
            // 封装数据
            contentValues.put(Constants.SUB_TB_COVER_URL, album.getCoverUrlLarge());
            contentValues.put(Constants.SUB_TB_TITLE, album.getAlbumTitle());
            contentValues.put(Constants.SUB_TB_DESCRIPTION, album.getAlbumIntro());
            contentValues.put(Constants.SUB_TB_TRACKS_COUNT, album.getIncludeTrackCount());
            contentValues.put(Constants.SUB_TB_PLAY_COUNT, album.getPlayCount());
            contentValues.put(Constants.SUB_TB_AUTHOR_NAME, album.getAnnouncer().getNickname());
            contentValues.put(Constants.SUB_TB_ALBUM_ID, album.getId());
            // 插入数据
            db.insert(Constants.SUB_TB_NAME, null, contentValues);
            db.setTransactionSuccessful();
            isAddSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            isAddSuccess = false;
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
            // 添加数据之后，把结果通知给Presenter
            if (mCallback != null) {
                mCallback.onAddResult(isAddSuccess);
            }
        }
    }

    @Override
    public void delAlbum(Album album) {
        SQLiteDatabase db = null;
        boolean isDelSuccess = false;
        try {
            db = mDbHelper.getWritableDatabase();
            db.beginTransaction();
            int delete = db.delete(Constants.SUB_TB_NAME, Constants.SUB_TB_ALBUM_ID + "=?", new String[]{String.valueOf(album.getId())});
            LogUtil.d(TAG, "delete count --> " + delete);
            db.setTransactionSuccessful();
            isDelSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            isDelSuccess = false;
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
            // 删除数据之后，把结果通知给Presenter
            if (mCallback != null) {
                mCallback.onDelResult(isDelSuccess);
            }
        }
    }

    /**
     * 获取订阅内容
     */
    @Override
    public void listAlbums() {
        SQLiteDatabase db = null;
        List<Album> queryResult = new ArrayList<>();
        try {
            db = mDbHelper.getReadableDatabase();
            db.beginTransaction();
            Cursor cursor = db.query(Constants.SUB_TB_NAME, null, null, null, null, null, "_id desc");
            // 封装数据
            while (cursor.moveToNext()) {
                Album album = new Album();
                String coverUrl = cursor.getString(cursor.getColumnIndex(Constants.SUB_TB_COVER_URL));
                album.setCoverUrlLarge(coverUrl);
                String title = cursor.getString(cursor.getColumnIndex(Constants.SUB_TB_TITLE));
                album.setAlbumTitle(title);
                String description = cursor.getString(cursor.getColumnIndex(Constants.SUB_TB_DESCRIPTION));
                album.setAlbumIntro(description);
                int trackCount = cursor.getInt(cursor.getColumnIndex(Constants.SUB_TB_TRACKS_COUNT));
                album.setIncludeTrackCount(trackCount);
                int playCount = cursor.getInt(cursor.getColumnIndex(Constants.SUB_TB_PLAY_COUNT));
                album.setPlayCount(playCount);
                String authorName = cursor.getString(cursor.getColumnIndex(Constants.SUB_TB_AUTHOR_NAME));
                Announcer announcer = new Announcer();
                announcer.setNickname(authorName);
                album.setAnnouncer(announcer);
                int albumId = cursor.getInt(cursor.getColumnIndex(Constants.SUB_TB_ALBUM_ID));
                album.setId(albumId);
                queryResult.add(album);
            }
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
            if (mCallback != null) {
                mCallback.onSubListLoaded(queryResult);
            }
        }
    }
}

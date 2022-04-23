package com.example.himalaya.base;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.example.himalaya.R;
import com.example.himalaya.utils.LogUtil;
import com.facebook.stetho.Stetho;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.service.NotificationChannelUtils;
import com.ximalaya.ting.android.opensdk.util.BaseUtil;


public class BaseApplication extends Application {


    private static Context mContext;

    private static String oaid;

    public static Handler sHandler = null;

    private XmPlayerManager mPlayerManager;


    @Override
    public void onCreate() {
        super.onCreate();
        // 获取context
        mContext = getApplicationContext();
        // 初始化LogUtil
        LogUtil.init(this.getPackageName(), false);
        // 初始化Handler
        sHandler = new Handler();
        // 初始化stetho调试工具
        Stetho.initializeWithDefaults(this);

        CommonRequest mXimalaya = CommonRequest.getInstanse();
        if (DTransferConstants.isRelease) {
            String mAppSecret = "8646d66d6abe2efd14f2891f9fd1c8af";
            mXimalaya.setAppkey("9f9ef8f10bebeaa83e71e62f935bede8");
            mXimalaya.setPackid("com.app.test.android");
            mXimalaya.init(this, mAppSecret);
        } else {
            String mAppSecret = "0a09d7093bff3d4947a5c4da0125972e";
            mXimalaya.setAppkey("f4d8f65918d9878e1702d49a8cdf0183");
            mXimalaya.setPackid("com.ximalaya.qunfeng");
            mXimalaya.init(this, mAppSecret);
        }
        //初始化播放器
        XmPlayerManager.getInstance(this).init();


    }

    //创建一个静态的方法，以便获取context对象
    public static Context getContext() {
        return mContext;
    }

    //创建一个静态的方法，以获取Handler
    public static Handler getHandler() {
        return sHandler;
    }



}

package com.example.himalaya;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.himalaya.adapters.PlayerTrackPagerAdapter;
import com.example.himalaya.base.BaseActivity;
import com.example.himalaya.interfaces.IPlayerViewCallback;
import com.example.himalaya.presenters.PlayerPresenter;
import com.example.himalaya.utils.LogUtil;
import com.example.himalaya.views.PlayerListPopupWindow;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerActivity extends BaseActivity implements IPlayerViewCallback, ViewPager.OnPageChangeListener {

    private static final String TAG = "PlayerActivity";
    private PlayerPresenter mPlayerPresenter;
    private ImageView mControlBtn;
    private SimpleDateFormat mMinFormat = new SimpleDateFormat("mm:ss");
    private SimpleDateFormat mHourFormat = new SimpleDateFormat("hh:mm:ss");
    private TextView mTotalDurationTv;
    private TextView mCurrentPositionTv;
    private SeekBar mDurationBar;
    private int mProgressBarCurrentPosition = 0;
    private boolean mIsUserTouchProgressBar = false;
    private ImageView mPlayNextBtn;
    private ImageView mPlayPreBtn;
    private TextView mTrackTitleTv;
    private ViewPager mTrackPageView;
    private PlayerTrackPagerAdapter mTrackPagerAdapter;
    private boolean isUserSlidePager = false;
    private ImageView mSwitchPlayModeBtn;
    private XmPlayListControl.PlayMode mCurrentMode = PLAY_MODEL_LIST;
    private boolean isReverse = false;
    private String mTrackTitleText;

    // 通过HashMap可以很方便地通过上一个mode获取到下一个mode
    private static Map<XmPlayListControl.PlayMode, XmPlayListControl.PlayMode> sPlayModeRule = new HashMap<>();

    static {
        // 1.默认的是: PLAY_MODEL_LIST
        // 2.列表循环: PLAY_MODEL_LIST_LOOP
        // 3.随机播放: PLAY_MODEL_RANDOM
        // 4.单曲循环: PLAY_MODEL_SINGLE_LOOP
        sPlayModeRule.put(PLAY_MODEL_LIST, PLAY_MODEL_LIST_LOOP);
        sPlayModeRule.put(PLAY_MODEL_LIST_LOOP, PLAY_MODEL_RANDOM);
        sPlayModeRule.put(PLAY_MODEL_RANDOM, PLAY_MODEL_SINGLE_LOOP);
        sPlayModeRule.put(PLAY_MODEL_SINGLE_LOOP, PLAY_MODEL_LIST);

    }

    private ImageView mPlayerListBtn;
    private PlayerListPopupWindow mPopupWindow;
    private ValueAnimator mEnterBgAnimator;
    public static final int BG_ANIMATION_DURATION = 500;
    private String mCurrCoverUrlLarge;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        initView();
        initEvent();
        initBgAnimation();

        // 测试播放
        mPlayerPresenter = PlayerPresenter.getInstance();
        mPlayerPresenter.registerViewCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放资源
        if (mPlayerPresenter != null) {
            mPlayerPresenter.unRegisterViewCallback(this);
            mPlayerPresenter = null;
        }

    }

    /**
     * 找到各个控件
     */
    private void initView() {
        mControlBtn = this.findViewById(R.id.play_or_pause_btn);
        mTotalDurationTv = this.findViewById(R.id.track_duration_tv);
        mCurrentPositionTv = this.findViewById(R.id.current_position_tv);
        mDurationBar = this.findViewById(R.id.track_seek_bar);
        mPlayNextBtn = this.findViewById(R.id.play_next_btn);
        mPlayPreBtn = this.findViewById(R.id.play_pre_btn);
        mTrackTitleTv = this.findViewById(R.id.track_title_tv);
        mTrackPageView = this.findViewById(R.id.track_pager_view);
        // 创建适配器
        mTrackPagerAdapter = new PlayerTrackPagerAdapter();
        // 设置适配器
        mTrackPageView.setAdapter(mTrackPagerAdapter);
        // 切换播放模式的按钮
        mSwitchPlayModeBtn = this.findViewById(R.id.player_mode_switch_btn);
        // 播放列表按钮
        mPlayerListBtn = this.findViewById(R.id.player_list_btn);
        // 播放列表弹出界面
        mPopupWindow = new PlayerListPopupWindow();

    }


    /**
     * 给控件设置相关的事件
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {
        mControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 如果现在是播放状态，那么点击后就暂停
                if (mPlayerPresenter.isPlaying()) {
                    mPlayerPresenter.pause();
                } else {
                    // 如果现在是非播放状态，那么点击后就播放
                    mPlayerPresenter.play();
                }
            }
        });

        mDurationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
                // 是用户触摸时,把当前的progress给mProgressBarCurrentPosition
                if (isFromUser) {
                    mProgressBarCurrentPosition = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 手指开始拖动进度条时把mIsUserTouchProgressBar改为true
                mIsUserTouchProgressBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mIsUserTouchProgressBar = false;
                // 在手指停止拖动的时候更新进度
                mPlayerPresenter.seekTo(mProgressBarCurrentPosition);
            }
        });

        mPlayPreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 播放前一个节目
                mPlayerPresenter.playPre();
            }
        });

        mPlayNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 播放下一个节目
                mPlayerPresenter.playNext();
            }
        });

        mTrackPageView.addOnPageChangeListener(this);

        mTrackPageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        isUserSlidePager = true;
                        break;
                }
                return false;
            }
        });

        mSwitchPlayModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 修改播放模式
                switchPlayMode();
            }
        });

        mPlayerListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 在底部展示播放列表
                mPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
                // 修改背景的透明度，使之在PopupWindow弹出的过程中有透明度渐变的过程
                mEnterBgAnimator.start();
            }
        });

        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // PopupWindow消失之后，恢复透明度，弹出时的动画翻转即可
                mEnterBgAnimator.reverse();
            }
        });

        mPopupWindow.setPlayListItemClickListener(new PlayerListPopupWindow.PlayListItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // 说明播放列表里的音频被点击了
                mPlayerPresenter.playByIndex(position);
            }
        });

        mPopupWindow.setPLayListActionListener(new PlayerListPopupWindow.PLayListActionListener() {
            @Override
            public void onPlayModeClick() {
                // 修改播放模式
                switchPlayMode();
            }

            @Override
            public void onOrderClick() {
                // 修改顺序逆序
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.reversePlayList();
                }
            }
        });
    }

    private void switchPlayMode() {
        // 处理播放模式的切换
        // 根据当前的mode获取到下一个mode
        // 通过HashMap可以很方便地通过上一个mode获取到下一个mode
        XmPlayListControl.PlayMode playMode = sPlayModeRule.get(mCurrentMode);
        // 修改播放模式
        if (mPlayerPresenter != null) {
            mPlayerPresenter.switchPlayMode(playMode);
        }
    }

    private void initBgAnimation() {
        // 处理弹出PopupWindow的动画
        mEnterBgAnimator = ValueAnimator.ofFloat(1.0f, 0.7f);
        mEnterBgAnimator.setDuration(BG_ANIMATION_DURATION);
        mEnterBgAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                // 处理背景的透明度
                updateBgAlpha(value);
            }
        });

    }


    public void updateBgAlpha(float alpha) {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.alpha = alpha;
        window.setAttributes(attributes);
    }

    /**
     * 根据当前播放模式，更新模式图标
     * PLAY_MODEL_LIST
     * PLAY_MODEL_LIST_LOOP
     * PLAY_MODEL_RANDOM
     * PLAY_MODEL_SINGLE_LOOP
     */
    private void updatePLayModeBtnImg() {
        int resId = R.drawable.selector_player_mode_list_order;
        switch (mCurrentMode) {
            case PLAY_MODEL_LIST:
                resId = R.drawable.selector_player_mode_list_order;
                break;
            case PLAY_MODEL_LIST_LOOP:
                resId = R.drawable.selector_player_mode_list_loop;
                break;
            case PLAY_MODEL_RANDOM:
                resId = R.drawable.selector_player_mode_random;
                break;
            case PLAY_MODEL_SINGLE_LOOP:
                resId = R.drawable.selector_player_mode_single_loop;
                break;
        }
        mSwitchPlayModeBtn.setImageResource(resId);
    }




    // ========== 实现IPlayerViewCallback的方法 ==========
    @Override
    public void onPlayStart() {
        // 开始播放，修改UI层变成暂停的按钮
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_stop);
        }
    }

    @Override
    public void onPlayPause() {
        // 暂停播放，修改UI层变成播放的按钮
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_play);
        }
    }

    @Override
    public void onPlayStop() {
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_play);
        }
    }

    @Override
    public void onPlayError() {

    }

    @Override
    public void onPlayPre(Track track) {

    }

    @Override
    public void onPlayNext(Track track) {

    }

    @Override
    public void onListLoaded(List<Track> list) {
        // 把播放列表数据设置到适配器中
        if (mTrackPagerAdapter != null) {
            mTrackPagerAdapter.setData(list);
        }
        // 数据回来以后，也要给播放器的播放列表
        if (mPopupWindow != null) {
            mPopupWindow.setListData(list);
        }
    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode playMode) {
        // 根据当前播放模式，更新模式图标
        mCurrentMode = playMode;
        // 更新mPopupWindow里的播放模式
        mPopupWindow.updatePlayMode(mCurrentMode);
        updatePLayModeBtnImg();
    }

    @Override
    public void onProgressChange(long current, long total) {
        mDurationBar.setMax((int) total);
        // 更新播放进度，更新进度条
        String totalDuration;
        String currentPosition;
        String ProgressBarCurrentPosition;
        if (total >= 1000 * 60 * 60) {
            totalDuration = mHourFormat.format(total);
            currentPosition = mHourFormat.format(current);
            ProgressBarCurrentPosition = mHourFormat.format(mProgressBarCurrentPosition);
        } else {
            totalDuration = mMinFormat.format(total);
            currentPosition = mMinFormat.format(current);
            ProgressBarCurrentPosition = mMinFormat.format(mProgressBarCurrentPosition);
        }
        // 更新总时长
        if (mTotalDurationTv != null) {
            mTotalDurationTv.setText(totalDuration);
        }

        // 正常播放(没拖动进度条)时,按照播放器回调的播放进度更新进度条位置
        if (!mIsUserTouchProgressBar) {
            // 更新当前时间
            if (mCurrentPositionTv != null) {
                mCurrentPositionTv.setText(currentPosition);
            }
            mDurationBar.setProgress((int) current);
        } else {
            // 当开始拖动进度条时，应该停掉正常播放时“当前时间”的变化，“当前时间”只随着拖动位置变化
            if (mCurrentPositionTv != null) {
                mCurrentPositionTv.setText(ProgressBarCurrentPosition);
            }
        }


    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }


    @Override
    public void onUpdateTrack(Track track, int playIndex) {
        if (track == null) {
            LogUtil.d(TAG, "onUpdateTrack --> track null");
            return;
        }
        mTrackTitleText = track.getTrackTitle();
        // 设置当前音频的标题
        if (mTrackTitleTv != null) {
            mTrackTitleTv.setText(mTrackTitleText);
        }
        // 当前音频改变时，就获取到当前播放器中的播放位置
        // 当点击按钮切换音频时，修改播放页面的图片
        if (mTrackPageView != null) {
            mTrackPageView.setCurrentItem(playIndex, true);
        }

        // 设置突出显示播放列表中当前播放的音频
        if (mPopupWindow != null) {
            mPopupWindow.setCurrentPlayPosition(playIndex);
        }



        // todo: 点进播放控制栏后没有图片
//        String coverUrlLarge = track.getCoverUrlLarge();
//        Observable.just(coverUrlLarge)
//                .map(coverUrl -> Glide.with(this).asFile().load(coverUrl).submit().get())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<File>() {
//                    @Override
//                    public void onSubscribe(@NonNull Disposable d) {
//                        LogUtil.d(TAG, "=====> onSubscribe");
//                    }
//
//                    @Override
//                    public void onNext(@NonNull File file) {
//                        Music music = new Music();
//                        music.setType(Music.Type.ONLINE);
//                        music.setCoverPath(file.getPath());
//                        mAlbumCoverView.setCoverBitmap(CoverLoader.get().loadRound(music));
//                        mPlayingBgIv.setImageBitmap(CoverLoader.get().loadBlur(music));
//                        LogUtil.d(TAG, "=====> onNext");
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//                        e.printStackTrace();
//                        LogUtil.d(TAG, "=====> onError");
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        LogUtil.d(TAG, "=====> onComplete");
//                    }
//                });

    }

    @Override
    public void updateListOrder(boolean isReverse) {
        mPopupWindow.updateOrderIcon(isReverse);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        // 当页面被选中的时候，就切换播放内容
        if (mPlayerPresenter != null && isUserSlidePager) {
            mPlayerPresenter.playByIndex(position);
        }
        isUserSlidePager = false;

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
package com.example.himalaya;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.himalaya.adapters.IndicatorAdapter;
import com.example.himalaya.adapters.MainContentAdapter;
import com.example.himalaya.base.BaseActivity;
import com.example.himalaya.data.XimalayaDBHelper;
import com.example.himalaya.interfaces.IPlayerViewCallback;
import com.example.himalaya.presenters.PlayerPresenter;
import com.example.himalaya.presenters.RecommendPresenter;
import com.example.himalaya.utils.FragmentCreator;
import com.example.himalaya.utils.LogUtil;
import com.example.himalaya.views.RoundRectImageView;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;

import java.util.List;


public class MainActivity extends BaseActivity implements IPlayerViewCallback {

    private static final String TAG = "MainActivity";
    private MagicIndicator mMagicIndicator;
    private ViewPager mContentPager;
    private IndicatorAdapter mIndicatorAdapter;
    private RoundRectImageView mTrackCoverIv;
    private TextView mTrackTitleTv;
    private TextView mTrackAuthorTv;
    private ImageView mPlayControlBtn;
    private PlayerPresenter mPlayerPresenter;
    private View mPlayControlItem;
    private View mSearchBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        initPresenter();
    }

    private void initPresenter() {
        mPlayerPresenter = PlayerPresenter.getInstance();
        mPlayerPresenter.registerViewCallback(this);
    }

    private void initView() {
        mMagicIndicator = this.findViewById(R.id.main_indicator);
        mMagicIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.main_color));
        // mMagicIndicator.setBackgroundColor(this.getResources().getColor(R.color.main_color));
        // 创建indicator的适配器
        mIndicatorAdapter = new IndicatorAdapter(this);
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setAdapter(mIndicatorAdapter);
        commonNavigator.setAdjustMode(true);
        mMagicIndicator.setNavigator(commonNavigator);

        // 创建ViewPager内容适配器
        mContentPager = this.findViewById(R.id.content_page);
        // viewpager会自己管理fragments,默认缓存（预加载）DEFAULT_OFFSCREEN_PAGES =1时,其他页面fragment会自动finish掉
        // setOffscreenPageLimit设置缓存个数
        mContentPager.setOffscreenPageLimit(FragmentCreator.PAGE_COUNT - 1);
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        MainContentAdapter mainContentAdapter = new MainContentAdapter(supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContentPager.setAdapter(mainContentAdapter);

        // 把indicator和ViewPager绑定在一起
        ViewPagerHelper.bind(mMagicIndicator, mContentPager);

        // 找到主界面播放控制栏相关的控件
        mPlayControlItem = this.findViewById(R.id.main_play_item);
        mTrackCoverIv = this.findViewById(R.id.main_track_cover);
        mTrackTitleTv = this.findViewById(R.id.main_track_title);
        mTrackTitleTv.setSelected(true);
        mTrackAuthorTv = this.findViewById(R.id.main_track_author);
        mPlayControlBtn = this.findViewById(R.id.main_play_control_btn);
        // 找到搜索控件
        mSearchBtn = this.findViewById(R.id.search_btn);
    }

    private void initEvent() {
        mIndicatorAdapter.setOnIndicatorTabClickListener(new IndicatorAdapter.OnIndicatorTabClickListener() {
            @Override
            public void onTabClick(int index) {
                LogUtil.d(TAG, "click indicatorTab index is --> " + index);
                if (mContentPager != null) {
                    mContentPager.setCurrentItem(index);
                }
            }
        });

        mPlayControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlayerPresenter != null) {
                    boolean hasPlayList = mPlayerPresenter.hasPlayList();
                    if (!hasPlayList) {
                        // 如果没有设置播放列表，就默认播放推荐列表第一个专辑的第一首
                        // 第一个推荐专辑是不固定的
                        playFirstRecommendAlbum();
                    } else {
                        if (mPlayerPresenter.isPlaying()) {
                            mPlayerPresenter.pause();
                        } else {
                            mPlayerPresenter.play();
                        }
                    }

                }
            }
        });

        mPlayControlItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlayerPresenter != null) {
                    boolean hasPlayList = mPlayerPresenter.hasPlayList();
                    if (!hasPlayList) {
                        // 如果没有设置播放列表，就默认播放推荐列表第一个专辑的第一首
                        // 第一个推荐专辑是不固定的
                        playFirstRecommendAlbum();
                        mPlayerPresenter.play();
                    }
                    // 跳转到播放器界面
                    startActivity(new Intent(MainActivity.this, PlayerActivity.class));
                }
            }
        });

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });
    }

    /**
     * 默认播放推荐列表第一个专辑的第一首
     */
    private void playFirstRecommendAlbum() {
        List<Album> currentRecommend = RecommendPresenter.getInstance().getCurrentRecommend();
        if (currentRecommend != null) {
            Album firstAlbum = currentRecommend.get(0);
            long albumId = firstAlbum.getId();
            mPlayerPresenter.playByAlbumId(albumId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayerPresenter != null) {
            mPlayerPresenter.unRegisterViewCallback(this);
        }
    }

    private void updatePlayControl(boolean isPlaying) {
        if (mPlayControlBtn != null) {
            mPlayControlBtn.setImageResource(isPlaying ? R.drawable.selector_player_stop : R.drawable.selector_player_play);
        }

    }

    @Override
    public void onPlayStart() {
        updatePlayControl(true);
    }

    @Override
    public void onPlayPause() {
        updatePlayControl(false);
    }

    @Override
    public void onPlayStop() {
        updatePlayControl(false);
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

    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode playMode) {

    }

    @Override
    public void onProgressChange(long currentProgress, long total) {

    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }

    @Override
    public void onUpdateTrack(Track track, int playIndex) {
        if (track != null) {
            String trackTitle = track.getTrackTitle();
            String nickname = track.getAnnouncer().getNickname();
            String coverUrl = track.getCoverUrlMiddle();
            if (mTrackTitleTv != null) {
                mTrackTitleTv.setText(trackTitle);
            }
            if (mTrackAuthorTv != null) {
                mTrackAuthorTv.setText(nickname);
            }
            if (mTrackCoverIv != null) {
                Glide.with(this).load(coverUrl).into(mTrackCoverIv);
            }

        }

    }

    @Override
    public void updateListOrder(boolean isReverse) {

    }
}
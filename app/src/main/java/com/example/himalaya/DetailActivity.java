package com.example.himalaya;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.himalaya.adapters.TrackListAdapter;
import com.example.himalaya.base.BaseActivity;
import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.interfaces.IAlbumDetailViewCallback;
import com.example.himalaya.interfaces.IPlayerViewCallback;
import com.example.himalaya.interfaces.ISubscriptionViewCallback;
import com.example.himalaya.presenters.AlbumDetailPresenter;
import com.example.himalaya.presenters.PlayerPresenter;
import com.example.himalaya.presenters.SubscriptionPresenter;
import com.example.himalaya.utils.LogUtil;
import com.example.himalaya.views.RoundRectImageView;
import com.example.himalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class DetailActivity extends BaseActivity implements IAlbumDetailViewCallback, UILoader.OnRetryClickListener, TrackListAdapter.ItemClickListener, IPlayerViewCallback, ISubscriptionViewCallback {

    private static final String TAG = "DetailActivity";
    private ImageView mLargeCover;
    private RoundRectImageView mSmallCover;
    private TextView mAlbumTitle;
    private TextView mAlbumAuthor;
    private AlbumDetailPresenter mAlbumDetailPresenter;
    private int mCurrentPage = 1;
    private RecyclerView mDetailList;
    private TrackListAdapter mDetailListAdapter;
    private FrameLayout mDetailListContainer;
    private UILoader mUiLoader;
    private long mCurrentId = -1;
    private PlayerPresenter mPlayerPresenter;
    private ImageView mPlayControlIv;
    private TextView mPlayControlTv;
    private View mPlayControlBtn;
    private List<Track> mCurrentTrackList = null;
    public static final int DEFAULT_PLAY_INDEX = 0;
    private TwinklingRefreshLayout mRefreshLayout;
    private boolean mIsLoadMore = false;
    private String mCurrTrackTitle;
    private TextView mSubBtn;
    private SubscriptionPresenter mSubscriptionPresenter;
    private Album mCurrentAlbum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        // 设置沉浸式状态栏
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        initView();
        initPresenter();
        // 设置订阅按钮的状态
        updateSubStatus();
        updatePlayStatus(mPlayerPresenter.isPlaying());
        initEvent();

    }

    private void initView() {
        mLargeCover = this.findViewById(R.id.large_cover_iv);
        mSmallCover = this.findViewById(R.id.small_cover_iv);
        mAlbumTitle = this.findViewById(R.id.album_title_tv);
        mAlbumAuthor = this.findViewById(R.id.album_author_tv);

        // 详情界面控制播放的按钮
        mPlayControlIv = this.findViewById(R.id.detail_play_control_iv);
        mPlayControlTv = this.findViewById(R.id.detail_play_control_text_tv);
        mPlayControlBtn = this.findViewById(R.id.play_control_btn);
        mPlayControlTv.setSelected(true);

        mDetailListContainer = this.findViewById(R.id.detail_list_container_fl);
        // 使用UILoader实现详情界面的多种状态(加载中、成功、内容为空、无网络)
        if (mUiLoader == null) {
            mUiLoader = new UILoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    // 返回成功时的view，也就是album详情列表
                    return createSuccessView(container);
                }
            };
        }
        mDetailListContainer.removeAllViews();
        mDetailListContainer.addView(mUiLoader);
        mUiLoader.setOnRetryClickListener(this);

        mSubBtn = this.findViewById(R.id.detail_subs_btn);
    }

    private void initPresenter() {
        // AlbumDetailPresenter
        mAlbumDetailPresenter = AlbumDetailPresenter.getInstance();
        mAlbumDetailPresenter.registerViewCallback(this);
        // PlayerPresenter
        mPlayerPresenter = PlayerPresenter.getInstance();
        mPlayerPresenter.registerViewCallback(this);
        // mSubscriptionPresenter
        mSubscriptionPresenter = SubscriptionPresenter.getInstance();
        mSubscriptionPresenter.getSubscriptionList();
        mSubscriptionPresenter.registerViewCallback(this);
    }

    private void initEvent() {
        mPlayControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlayerPresenter != null) {
                    // 判断播放器是否有播放列表
                    if (mPlayerPresenter.hasPlayList()) {
                        // 主动控制播放器的状态
                        handlePlayControl();
                    } else {
                        // 处理没有播放列表的情况
                        handleNoPlayList();
                    }
                }
            }
        });

        mSubBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSubscriptionPresenter != null) {
                    boolean isSub = mSubscriptionPresenter.isSub(mCurrentAlbum);
                    // 如果已订阅，则点击后取消订阅；如果没订阅，则点击后订阅。
                    if (isSub) {
                        mSubscriptionPresenter.deleteSubscription(mCurrentAlbum);
                    } else {
                        mSubscriptionPresenter.addSubscription(mCurrentAlbum);
                    }
                    updateSubStatus();
                }
            }
        });

    }

    private void updateSubStatus() {
        if (mSubscriptionPresenter != null) {
            boolean isSub = mSubscriptionPresenter.isSub(mCurrentAlbum);
            mSubBtn.setText(isSub ? R.string.cancel_subscribe_btn_text : R.string.subscribe_btn_text);
        }
    }

    private void handlePlayControl() {
        // 主动控制播放器的状态
        if (mPlayerPresenter.isPlaying()) {
            // 正在播放时点击，就暂停
            mPlayerPresenter.pause();
        } else {
            // 正在暂停时点击，就播放
            mPlayerPresenter.play();
        }
    }

    /**
     * 当现在不存在播放列表时，
     */
    private void handleNoPlayList() {
        mPlayerPresenter.setPlayList(mCurrentTrackList, DEFAULT_PLAY_INDEX);

    }

    private View createSuccessView(ViewGroup container) {
        View detailListView = LayoutInflater.from(this).inflate(R.layout.item_detail_list, container, false);
        mDetailList = detailListView.findViewById(R.id.album_detail_list_rv);
        // 找到刷新控件
        mRefreshLayout = detailListView.findViewById(R.id.refresh_layout);
        // 设置布局管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mDetailList.setLayoutManager(linearLayoutManager);
        // 设置适配器
        mDetailListAdapter = new TrackListAdapter();
        mDetailList.setAdapter(mDetailListAdapter);
        mDetailListAdapter.setItemClickListener(this);

//        BezierLayout headView = new BezierLayout(this);
//        mRefreshLayout.setHeaderView(headView);
        mRefreshLayout.setMaxHeadHeight(140);
        mRefreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                super.onRefresh(refreshLayout);
                BaseApplication.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DetailActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
                        mRefreshLayout.finishRefreshing();
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                super.onLoadMore(refreshLayout);
                // 加载更多内容
                if (mAlbumDetailPresenter != null) {
                    mAlbumDetailPresenter.loadMore();
                    mIsLoadMore = true;
                }
            }
        });

        return detailListView;
    }


    @Override
    public void onDetailListLoaded(List<Track> tracks) {
        // 下滑加载结束
        if (mIsLoadMore && mRefreshLayout != null) {
            mRefreshLayout.finishLoadmore();
            mIsLoadMore = false;
        }

        this.mCurrentTrackList = tracks;
        // 判断数据结果，根据结果控制UI显示
        if (tracks == null || tracks.size() == 0) {
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
            }
        }
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }

        // 更新/设置详情列表的UI数据
        mDetailListAdapter.setData(tracks);
    }

    @Override
    public void onAlbumLoaded(Album album) {
        this.mCurrentAlbum = album;

        if (mAlbumTitle != null) {
            mAlbumTitle.setText(album.getAlbumTitle());
        }
        if (mAlbumAuthor != null) {
            mAlbumAuthor.setText(album.getAnnouncer().getNickname());
        }
        if (mLargeCover != null) {
            // 设置毛玻璃效果
            Glide.with(this).load(album.getCoverUrlLarge())
                    .apply(bitmapTransform(new BlurTransformation(50)))
                    .into(mLargeCover);
        }
        if (mSmallCover != null) {
            Glide.with(this).load(album.getCoverUrlLarge()).into(mSmallCover);
        }

        // 获取专辑的详情内容
        long id = album.getId();
        mCurrentId = id;
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.getAlbumDetail((int) id, mCurrentPage);
        }
        // 拿数据，显示Loading状态
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        }
    }

    @Override
    public void onNetworkError(int errorCode, String errorMsg) {
        // 请求错误，显示网络异常状态
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
        }
    }

    @Override
    public void onLoadMoreFinished(int size) {
        if (size > 0) {
            // 表示加载出了数据
            Toast.makeText(this, "成功加载" + size + "条音频", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "没有更多音频了", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRefreshFinished(int size) {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消接口的注册
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.unRegisterViewCallback(this);
        }
        if (mPlayerPresenter != null) {
            mPlayerPresenter.unRegisterViewCallback(this);
        }
        if (mSubscriptionPresenter != null) {
            mSubscriptionPresenter.unRegisterViewCallback(this);
        }

    }

    @Override
    public void onRetryClick() {
        // 表示网络不佳的时候，用户点击了重试
        // 重新加载数据即可
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.getAlbumDetail((int) mCurrentId, mCurrentPage);
        }
    }

    @Override
    public void itemClick(List<Track> detailData, int position) {
        // 设置播放器播放列表的数据
        mPlayerPresenter.setPlayList(detailData, position);
        // 点击item跳转到播放器界面
        Intent intent = new Intent(this, PlayerActivity.class);
        startActivity(intent);
    }

    /**
     * 根据播放状态修改图标及文字
     *
     * @param playing
     */
    private void updatePlayStatus(boolean playing) {
        if (mPlayControlBtn != null) {
            mPlayControlIv.setImageResource(playing ? R.drawable.selector_play_control_pause : R.drawable.selector_play_control_play);
            if (!playing) {
                mPlayControlTv.setText(R.string.click_play_tips_text);
            } else {
                if (!TextUtils.isEmpty(mCurrTrackTitle)) {
                    mPlayControlTv.setText(mCurrTrackTitle);
                }
            }
        }
    }


    // ============= 实现IPlayerViewCallback的方法 =============
    @Override
    public void onPlayStart() {
        // 播放器播放时图标修改为暂停，文字修改为正在播放
        updatePlayStatus(true);
    }

    @Override
    public void onPlayPause() {
        // 播放器暂停时图标修改为播放，文字修改为已暂停
        updatePlayStatus(false);
    }

    @Override
    public void onPlayStop() {
        // 图标修改为播放，文字修改为已暂停
        updatePlayStatus(false);
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
            mCurrTrackTitle = track.getTrackTitle();
            if (!TextUtils.isEmpty(mCurrTrackTitle) && mPlayControlTv != null) {
                mPlayControlTv.setText(mCurrTrackTitle);
            }
        }
    }

    @Override
    public void updateListOrder(boolean isReverse) {

    }

    // =========== ISubscriptionViewCallback impl start =============
    @Override
    public void onAddResult(boolean isSuccess) {
        if (isSuccess) {
            mSubBtn.setText(R.string.cancel_subscribe_btn_text);
        }
        String tipsText = getString(isSuccess ? R.string.subscribe_success_tips_text : R.string.subscribe_fail_tips_text);
        Toast.makeText(this, tipsText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteResult(boolean isSuccess) {
        if (isSuccess) {
            mSubBtn.setText(R.string.subscribe_btn_text);
        }
        String tipsText = getString(isSuccess ? R.string.cancel_subscribe_success_tips_text : R.string.cancel_subscribe_fail_tips_text);
        Toast.makeText(this, tipsText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSubscriptionLoaded(List<Album> albums) {
        for (Album album : albums) {
            LogUtil.d(TAG, "subscription --> " + album.getAlbumTitle());
        }
    }

    @Override
    public void onSubReachLimit() {
        Toast.makeText(this, getString(R.string.subscription_over), Toast.LENGTH_SHORT).show();
    }
    // =========== ISubscriptionViewCallback impl end =============
}

package com.example.himalaya.fragments;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.DetailActivity;
import com.example.himalaya.R;
import com.example.himalaya.adapters.AlbumListAdapter;
import com.example.himalaya.base.BaseFragment;
import com.example.himalaya.interfaces.IRecommendViewCallback;
import com.example.himalaya.presenters.AlbumDetailPresenter;
import com.example.himalaya.presenters.RecommendPresenter;
import com.example.himalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

public class RecommendFragment extends BaseFragment implements IRecommendViewCallback, UILoader.OnRetryClickListener, AlbumListAdapter.OnAlbumItemClickListener {

    private static final String TAG = "RecommendFragment";
    private View mRootView;
    private RecyclerView mRecommendRecyclerView;
    private AlbumListAdapter mAlbumListAdapter;
    private RecommendPresenter mRecommendPresenter;
    private UILoader mUiLoader;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {

        mUiLoader = new UILoader(getContext()) {
            @Override
            protected View getSuccessView(ViewGroup container) {
                return createSuccessView(layoutInflater, container);
            }
        };

        // 逻辑和UI分离
        // 获取到逻辑层的对象
        mRecommendPresenter = RecommendPresenter.getInstance();
        // 注册通知接口
        mRecommendPresenter.registerViewCallback(this);
        // 获取推荐列表
        mRecommendPresenter.getRecommendList();

        // mUiLoader跟父View解绑，否则重复绑定会崩溃
        if (mUiLoader.getParent() instanceof ViewGroup) {
            ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
        }

        mUiLoader.setOnRetryClickListener(this);

        // 返回View，给界面显示
        return mUiLoader;
    }

    private View createSuccessView(LayoutInflater layoutInflater, ViewGroup container) {
        // View加载完成
        mRootView = layoutInflater.inflate(R.layout.fragment_recommend, container, false);
        // RecyclerView的使用
        // 1.找到控件
        mRecommendRecyclerView = mRootView.findViewById(R.id.recommend_recyclerview);
        TwinklingRefreshLayout twinklingRefreshLayout = mRootView.findViewById(R.id.recommend_over_scroll_view);
        twinklingRefreshLayout.setPureScrollModeOn();
        // 2.设置布管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecommendRecyclerView.setLayoutManager(linearLayoutManager);
        // 3.设置适配器
        mAlbumListAdapter = new AlbumListAdapter();
        mAlbumListAdapter.setOnAlbumItemClickListener(this);
        mRecommendRecyclerView.setAdapter(mAlbumListAdapter);

        return mRootView;
    }

    @Override
    public void onRecommendListLoaded(List<Album> result) {
        // 当获取到推荐内容之后，这个方法就会被调用（获取成功时）
        // 把数据设置给适配器，并更新UI
        mAlbumListAdapter.setData(result);
        mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
    }

    @Override
    public void onNetworkError() {
        mUiLoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
    }

    @Override
    public void onEmpty() {
        mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
    }

    @Override
    public void onLoading() {
        mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 取消接口的注册
        if (mRecommendPresenter != null) {
            mRecommendPresenter.unRegisterViewCallback(this);
        }
    }

    @Override
    public void onRetryClick() {
        // 表示网络不佳的时候，用户点击了重试
        // 重新加载数据即可
        if (mRecommendPresenter != null) {
            mRecommendPresenter.getRecommendList();
        }
    }

    @Override
    public void onItemClick(int position, Album album) {
        AlbumDetailPresenter.getInstance().setTargetAlbum(album);
        // item被点击了
        // 根据位置拿到数据
        Intent intent = new Intent(getContext(), DetailActivity.class);
        startActivity(intent);
    }
}

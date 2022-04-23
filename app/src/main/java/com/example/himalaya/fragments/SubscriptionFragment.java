package com.example.himalaya.fragments;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.DetailActivity;
import com.example.himalaya.R;
import com.example.himalaya.adapters.AlbumListAdapter;
import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.base.BaseFragment;
import com.example.himalaya.interfaces.ISubscriptionViewCallback;
import com.example.himalaya.presenters.AlbumDetailPresenter;
import com.example.himalaya.presenters.SubscriptionPresenter;
import com.example.himalaya.views.ConfirmDialog;
import com.example.himalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

public class SubscriptionFragment extends BaseFragment implements ISubscriptionViewCallback, AlbumListAdapter.OnAlbumItemClickListener, AlbumListAdapter.OnAlbumItemLongClickListener, ConfirmDialog.OnDialogActionListener {

    private SubscriptionPresenter mSubscriptionPresenter;
    private RecyclerView mSubListRv;
    private AlbumListAdapter mAdapter;
    private TwinklingRefreshLayout mSubRefreshLayout;
    private Album mCurrentClickAlbum;
    private UILoader mUiLoader;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        FrameLayout rootView = (FrameLayout) layoutInflater.inflate(R.layout.fragment_subscription, container, false);

        if (mUiLoader == null) {
            mUiLoader = new UILoader(container.getContext()) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView();
                }
                @Override
                protected View getEmptyView() {
                    // 重写方法
                    View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view, this, false);
                    TextView emptyTips = emptyView.findViewById(R.id.empty_view_tips_tv);
                    emptyTips.setText(R.string.empty_view_sub_tips);
                    return emptyView;
                }
            };
        } else {
            if (mUiLoader.getParent() instanceof ViewGroup) {
                ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
            }
        }
        rootView.addView(mUiLoader);
        return rootView;
    }

    private View createSuccessView() {
        View itemView = LayoutInflater.from(BaseApplication.getContext()).inflate(R.layout.item_subscription, null);

        mSubRefreshLayout = itemView.findViewById(R.id.subscription_over_scroll_view);
        mSubRefreshLayout.setEnableLoadmore(false);
        mSubRefreshLayout.setEnableRefresh(false);
        mSubRefreshLayout.setPureScrollModeOn();
        mSubListRv = itemView.findViewById(R.id.subscription_recyclerview);
        mSubListRv.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        mAdapter = new AlbumListAdapter();
        mAdapter.setOnAlbumItemClickListener(this);
        mAdapter.setOnAlbumItemLongClickListener(this);
        mSubListRv.setAdapter(mAdapter);

        mSubscriptionPresenter = SubscriptionPresenter.getInstance();
        mSubscriptionPresenter.registerViewCallback(this);
        mSubscriptionPresenter.getSubscriptionList();
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        }
        return itemView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 取消接口的注册
        if (mSubscriptionPresenter != null) {
            mSubscriptionPresenter.unRegisterViewCallback(this);
        }
        mAdapter.setOnAlbumItemClickListener(null);
    }

    @Override
    public void onAddResult(boolean isSuccess) {

    }

    @Override
    public void onDeleteResult(boolean isSuccess) {
        // 给出取消订阅的提示
        Toast.makeText(getActivity(), isSuccess ? R.string.cancel_sub_success_tips : R.string.cancel_sub_fail_tips, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSubscriptionLoaded(List<Album> albums) {
        if (albums.size() == 0) {
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
            }
        } else {
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
            }
        }
        // 更新UI
        if (mAdapter != null) {
            mAdapter.setData(albums);
        }
    }

    @Override
    public void onSubReachLimit() {
//        Toast.makeText(getActivity(), getString(R.string.subscription_over), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(int position, Album album) {
        AlbumDetailPresenter.getInstance().setTargetAlbum(album);
        // item被点击了
        // 根据位置拿到数据
        Intent intent = new Intent(getContext(), DetailActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(Album album) {
        this.mCurrentClickAlbum = album;
        // 长按订阅item时
        ConfirmDialog confirmDialog = new ConfirmDialog(getActivity());
        confirmDialog.setOnDialogActionListener(this);
        confirmDialog.show();
    }

    @Override
    public void onCancelSubClick() {
        // 取消订阅
        if (mCurrentClickAlbum != null && mSubscriptionPresenter != null) {
            mSubscriptionPresenter.deleteSubscription(mCurrentClickAlbum);
        }
    }
}

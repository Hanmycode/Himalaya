package com.example.himalaya.fragments;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.PlayerActivity;
import com.example.himalaya.R;
import com.example.himalaya.adapters.TrackListAdapter;
import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.base.BaseFragment;
import com.example.himalaya.interfaces.IHistoryViewCallback;
import com.example.himalaya.presenters.HistoryPresenter;
import com.example.himalaya.presenters.PlayerPresenter;
import com.example.himalaya.views.ConfirmCheckBoxDialog;
import com.example.himalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

public class HistoryFragment extends BaseFragment implements IHistoryViewCallback, TrackListAdapter.ItemClickListener, TrackListAdapter.OnTrackItemLongClickListener, ConfirmCheckBoxDialog.OnDialogDelActionListener {

    private UILoader mUiLoader;
    private RecyclerView mHistoryListRv;
    private TwinklingRefreshLayout mSubRefreshLayout;
    private TrackListAdapter mAdapter;
    private HistoryPresenter mHistoryPresenter;
    private PlayerPresenter mPlayerPresenter;
    private Track mCurrTrack = null;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        FrameLayout rootView = (FrameLayout) layoutInflater.inflate(R.layout.fragment_history, container, false);
        if (mUiLoader == null) {
            mUiLoader = new UILoader(BaseApplication.getContext()) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView();
                }
                @Override
                protected View getEmptyView() {
                    // 重写方法
                    View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view, this, false);
                    TextView emptyTips = emptyView.findViewById(R.id.empty_view_tips_tv);
                    emptyTips.setText(R.string.empty_view_no_history_tips);
                    return emptyView;
                }
            };
        } else {
            if (mUiLoader.getParent() instanceof ViewGroup) {
                ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
            }
        }
        mHistoryPresenter = HistoryPresenter.getInstance();
        mHistoryPresenter.registerViewCallback(this);
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        }
        mHistoryPresenter.listHistories();
        mPlayerPresenter = PlayerPresenter.getInstance();

        rootView.addView(mUiLoader);
        return rootView;
    }

    private View createSuccessView() {
        View successView = LayoutInflater.from(BaseApplication.getContext()).inflate(R.layout.item_history, null);

        mSubRefreshLayout = successView.findViewById(R.id.history_over_scroll_view);
        mSubRefreshLayout.setEnableLoadmore(false);
        mSubRefreshLayout.setEnableRefresh(false);
        mSubRefreshLayout.setPureScrollModeOn();
        mHistoryListRv = successView.findViewById(R.id.history_recyclerview);
        mHistoryListRv.setLayoutManager(new LinearLayoutManager(successView.getContext()));
        mAdapter = new TrackListAdapter();
        mAdapter.setItemClickListener(this);
        mAdapter.setOnTrackItemLongClickListener(this);
        mHistoryListRv.setAdapter(mAdapter);

        return successView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 取消接口的注册
        if (mHistoryPresenter != null) {
            mHistoryPresenter.unRegisterViewCallback(this);
        }
    }

    @Override
    public void onHistoryLoaded(List<Track> tracks) {
        if (tracks == null || tracks.size() == 0) {
            mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
        } else {
            mAdapter.setData(tracks);
            mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }
    }

    @Override
    public void itemClick(List<Track> detailData, int position) {
        // 当点击历史记录的数据时
        // 设置播放器播放列表的数据
        if (mPlayerPresenter != null) {
            mPlayerPresenter.setPlayList(detailData, position);
        }
        // 点击item跳转到播放器界面
        startActivity(new Intent(getActivity(), PlayerActivity.class));
    }

    @Override
    public void onItemLongClick(Track track) {
        this.mCurrTrack = track;
        // 长按删除历史记录
        ConfirmCheckBoxDialog confirmDialog = new ConfirmCheckBoxDialog(getActivity());
        confirmDialog.setOnDialogActionListener(this);
        confirmDialog.show();
    }


    @Override
    public void onDelHistoryClick(boolean checked) {
        if (mHistoryPresenter != null && mCurrTrack != null) {
            if (!checked) {
                mHistoryPresenter.delHistory(mCurrTrack);
            } else {
                mHistoryPresenter.cleanHistory();
            }
        }
    }
}

package com.example.himalaya.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.R;
import com.example.himalaya.adapters.PlayListAdapter;
import com.example.himalaya.base.BaseApplication;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

public class PlayerListPopupWindow extends PopupWindow {

    private final View mPopView;
    private TextView mCloseBtn;
    private RecyclerView mPlayTrackList;
    private PlayListAdapter mPlayListAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private TextView mPlayModeTv;
    private ImageView mPlayModeIv;
    private View mPlayModeContainer;
    private PLayListActionListener mPLayListActionListener = null;
    private View mOrderContainer;
    private ImageView mOrderIcon;
    private TextView mOrderText;

    public PlayerListPopupWindow() {
        // 设置PopupWindow的宽高
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setOutsideTouchable(true);

        // 设置PopupWindow的内容
        mPopView = LayoutInflater.from(BaseApplication.getContext()).inflate(R.layout.pop_play_list, null);
        setContentView(mPopView);

        // 设置窗口进入和退出的动画
        setAnimationStyle(R.style.pop_animation);

        initView();
        initEvent();
    }

    private void initView() {
        mCloseBtn = mPopView.findViewById(R.id.play_list_close_btn);
        // 找到控件
        mPlayTrackList = mPopView.findViewById(R.id.play_list_rv);
        // 设置布局管理器
        mLinearLayoutManager = new LinearLayoutManager(BaseApplication.getContext());
        mPlayTrackList.setLayoutManager(mLinearLayoutManager);
        // 设置适配器
        mPlayListAdapter = new PlayListAdapter();
        mPlayTrackList.setAdapter(mPlayListAdapter);
        // 播放模式文字和图片控件
        mPlayModeTv = mPopView.findViewById(R.id.play_list_play_mode_tv);
        mPlayModeIv = mPopView.findViewById(R.id.play_list_play_mode_iv);
        mPlayModeContainer = mPopView.findViewById(R.id.play_list_play_mode_container);
        // 播放顺序
        mOrderContainer = mPopView.findViewById(R.id.play_list_order_container);
        mOrderIcon = mPopView.findViewById(R.id.play_list_order_iv);
        mOrderText = mPopView.findViewById(R.id.play_list_order_tv);

    }

    private void initEvent() {
        // 点击“关闭”后，窗口消失
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayerListPopupWindow.this.dismiss();
            }
        });
        // 点击切换播放模式
        mPlayModeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPLayListActionListener != null) {
                    mPLayListActionListener.onPlayModeClick();
                }

            }
        });

        mOrderContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 切换播放列表为顺序或逆序
                mPLayListActionListener.onOrderClick();
            }
        });

    }

    /**
     * 更新切换顺序逆序的图标和文字
     * @param isReverse
     */
    public void updateOrderIcon(boolean isReverse) {
        mOrderIcon.setImageResource(isReverse ? R.drawable.selector_player_mode_list_reverse : R.drawable.selector_player_mode_list_order);
        mOrderText.setText(BaseApplication.getContext().getString(isReverse ? R.string.reverse_text : R.string.order_text));
        Toast.makeText(BaseApplication.getContext(), mOrderText.getText(), Toast.LENGTH_SHORT).show();

    }

    public void setListData(List<Track> data) {
        if (mPlayListAdapter != null) {
            mPlayListAdapter.setData(data);
        }
    }

    public void setCurrentPlayPosition(int position) {
        if (mPlayListAdapter != null) {
            mPlayListAdapter.setCurrentPlayPosition(position);
            // 让RecyclerView滑动到当前播放的position
            mPlayTrackList.scrollToPosition(position);
            mLinearLayoutManager.scrollToPositionWithOffset(position, 0);
        }
    }

    public void setPlayListItemClickListener(PlayListItemClickListener itemClickListener) {
        mPlayListAdapter.setOnItemClickListener(itemClickListener);
    }

    /**
     * 更新播放列表的播放模式UI
     *
     * @param currentMode
     */
    public void updatePlayMode(XmPlayListControl.PlayMode currentMode) {
        updatePLayModeBtnImg(currentMode);
    }

    /**
     * 根据当前播放模式，更新模式图标
     * PLAY_MODEL_LIST
     * PLAY_MODEL_LIST_LOOP
     * PLAY_MODEL_RANDOM
     * PLAY_MODEL_SINGLE_LOOP
     */
    private void updatePLayModeBtnImg(XmPlayListControl.PlayMode playMode) {
        int resId = R.drawable.selector_player_mode_list_order;
        int textId = R.string.play_mode_list_order_text;
        switch (playMode) {
            case PLAY_MODEL_LIST:
                resId = R.drawable.selector_player_mode_list_order;
                textId = R.string.play_mode_list_order_text;
                break;
            case PLAY_MODEL_LIST_LOOP:
                resId = R.drawable.selector_player_mode_list_loop;
                textId = R.string.play_mode_list_loop_text;
                break;
            case PLAY_MODEL_RANDOM:
                resId = R.drawable.selector_player_mode_random;
                textId = R.string.play_mode_random_text;
                break;
            case PLAY_MODEL_SINGLE_LOOP:
                resId = R.drawable.selector_player_mode_single_loop;
                textId = R.string.play_mode_single_loop_text;
                break;
        }
        mPlayModeIv.setImageResource(resId);
        mPlayModeTv.setText(textId);
    }


    public interface PlayListItemClickListener {
        void onItemClick(int position);
    }


    public void setPLayListActionListener(PLayListActionListener pLayListActionListener) {
        mPLayListActionListener = pLayListActionListener;

    }

    public interface PLayListActionListener {
        // 点击切换播放模式
        void onPlayModeClick();

        // 点击切换顺序逆序
        void onOrderClick();
    }


}

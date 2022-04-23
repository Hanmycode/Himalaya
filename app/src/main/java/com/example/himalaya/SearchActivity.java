package com.example.himalaya;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.himalaya.adapters.AlbumListAdapter;
import com.example.himalaya.adapters.SearchSuggestAdapter;
import com.example.himalaya.interfaces.ISearchViewCallback;
import com.example.himalaya.presenters.AlbumDetailPresenter;
import com.example.himalaya.presenters.SearchPresenter;
import com.example.himalaya.views.FlowTextLayout;
import com.example.himalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements ISearchViewCallback, AlbumListAdapter.OnAlbumItemClickListener {

    private static final String TAG = "SearchActivity";
    private View mBackBtn;
    private EditText mInputBox;
    private View mSearchBtn;
    private FrameLayout mResultContainer;
    private SearchPresenter mSearchPresenter;
    private FlowTextLayout mHotWordsFlowLayout;
    private UILoader mContentUILoader;
    private View mResultView;
    private RecyclerView mSearchResultRv;
    private AlbumListAdapter mAlbumListAdapter;
    private View mHotWordsView;
    private InputMethodManager mInputManager;
    private static final int SHOW_KEYBOARD_TIME = 100;
    private RecyclerView mSuggestWordsRv;
    private SearchSuggestAdapter mSuggestAdapter;
    private View mDelInputBtn;
    private TwinklingRefreshLayout mSearchRefreshLayout;
    private boolean isNeedSuggestWords = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initView();
        initEvent();
        initPresenter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSearchPresenter != null) {
            mSearchPresenter.unRegisterViewCallback(this);
            mSearchPresenter = null;
        }
    }

    private void initView() {
        mBackBtn = this.findViewById(R.id.search_back_iv);
        mInputBox = this.findViewById(R.id.search_edit_text);
        mDelInputBtn = this.findViewById(R.id.search_input_delete_btn);
        mDelInputBtn.setVisibility(View.GONE);
        mSearchBtn = this.findViewById(R.id.search_btn);
        mResultContainer = this.findViewById(R.id.search_container);

        // 修改EditText里左侧搜索图标的大小
        Drawable drawable = getResources().getDrawable(R.mipmap.edit_text_search_icon);
        drawable.setBounds(0, 0, 38, 38);//第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        mInputBox.setCompoundDrawables(drawable, null, null, null);
        mInputBox.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 获取焦点
                mInputBox.requestFocus();
                // 默认打开后自动弹出键盘
                mInputManager.showSoftInput(mInputBox, InputMethodManager.SHOW_IMPLICIT);
            }
        }, SHOW_KEYBOARD_TIME);
        //在该Editview获得焦点的时候将“回车”键改为“搜索”
        mInputBox.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mInputBox.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        //不然回车【搜索】会换行
        mInputBox.setSingleLine(true);

        if (mContentUILoader == null) {
            mContentUILoader = new UILoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView();
                }
                @Override
                protected View getEmptyView() {
                    // 重写方法
                    View emptyView =  LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view, this, false);
                    TextView emptyTips = emptyView.findViewById(R.id.empty_view_tips_tv);
                    emptyTips.setText(R.string.empty_view_search_tips);
                    return emptyView;
                }
            };
            if (mContentUILoader.getParent() instanceof ViewGroup) {
                // 不能重复添加
                ((ViewGroup) mContentUILoader.getParent()).removeView(mContentUILoader);
            }
            mResultContainer.addView(mContentUILoader);
        }

    }

    /**
     * 创建搜索请求成功后的View
     *
     * @return
     */
    private View createSuccessView() {
        mResultView = LayoutInflater.from(this).inflate(R.layout.search_result_layout, null);
        // 显示热词
        mHotWordsView = mResultView.findViewById(R.id.search_hot_word_view);
        mHotWordsFlowLayout = mResultView.findViewById(R.id.hot_words_flow_layout);

        // 搜索结果
        mSearchResultRv = mResultView.findViewById(R.id.search_result_rv);
        mSearchRefreshLayout = mResultView.findViewById(R.id.search_result_refresh);
        mSearchRefreshLayout.setEnableRefresh(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mSearchResultRv.setLayoutManager(layoutManager);
        mAlbumListAdapter = new AlbumListAdapter();
        mSearchResultRv.setAdapter(mAlbumListAdapter);

        //搜索联想
        mSuggestWordsRv = mResultView.findViewById(R.id.search_suggest_rv);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        mSuggestWordsRv.setLayoutManager(layoutManager1);
        mSuggestAdapter = new SearchSuggestAdapter();
        mSuggestWordsRv.setAdapter(mSuggestAdapter);

        return mResultView;
    }

    private void initEvent() {
        // 点击返回
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestSearch();
            }
        });

        mInputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(charSequence)) {
                    // 当EditText为空时，隐藏结果，显示推荐热词
                    mDelInputBtn.setVisibility(View.GONE);
                    hideSuccessView();
                    mHotWordsView.setVisibility(View.VISIBLE);
                } else {
                    if (isNeedSuggestWords) {
                        // 根据输入内容实时触发联想词
                        if (mSearchPresenter != null) {
                            mDelInputBtn.setVisibility(View.VISIBLE);
                            mSearchPresenter.getSuggestKeyword(charSequence.toString());
                        }
                    } else {
                        isNeedSuggestWords = true;
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mInputBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_SEARCH) && keyEvent != null) {
                    //点击搜索要做的操作
                    requestSearch();
                    return true;
                }
                return false;
            }
        });

        mHotWordsFlowLayout.setClickListener(new FlowTextLayout.ItemClickListener() {
            @Override
            public void onItemClick(String text) {
                // 推荐热词的点击，不需要相关的联想词显示
                mDelInputBtn.setVisibility(View.VISIBLE);
                isNeedSuggestWords = false;
                switch2Search(text);
            }
        });

        mContentUILoader.setOnRetryClickListener(new UILoader.OnRetryClickListener() {
            @Override
            public void onRetryClick() {
                if (mSearchPresenter != null) {
                    mSearchPresenter.reSearch();
                    mContentUILoader.updateStatus(UILoader.UIStatus.LOADING);
                }
            }
        });

        if (mSuggestAdapter != null) {
            mSuggestAdapter.setItemClickListener(new SearchSuggestAdapter.itemClickListener() {
                @Override
                public void onItemClick(String keyword) {
                    // 点击联想词，执行搜索
                    // 推荐热词的点击，不需要相关的联想词显示
                    isNeedSuggestWords = false;
                    switch2Search(keyword);
                }
            });
        }

        mDelInputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 点击删除按钮时，将EditText置空
                mInputBox.setText("");
            }
        });

        mSearchRefreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                super.onLoadMore(refreshLayout);
                // 加载更多内容
                //加载更多的内容
                if (mSearchPresenter != null) {
                    mSearchPresenter.loadMore();
                }
            }
        });

        mAlbumListAdapter.setOnAlbumItemClickListener(this);

    }

    private void initPresenter() {
        mSearchPresenter = SearchPresenter.getInstance();
        // 注册UI更新的接口
        mSearchPresenter.registerViewCallback(this);
        // 去拿热词
        mSearchPresenter.getHotWord();

        mInputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public void onSearchResultLoaded(List<Album> result) {
        handleSearchResult(result);

        // 隐藏键盘
        mInputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void handleSearchResult(List<Album> result) {
        // 当有数据回来时，隐藏推荐热词，显示结果
        hideSuccessView();
        mSearchRefreshLayout.setVisibility(View.VISIBLE);

        if (result != null) {
            if (result.size() == 0) {
                mContentUILoader.updateStatus(UILoader.UIStatus.EMPTY);
            } else {
                // 如果数据不为空，就设置数据
                mAlbumListAdapter.setData(result);
                mContentUILoader.updateStatus(UILoader.UIStatus.SUCCESS);
            }
        }
    }

    @Override
    public void onHotWordLoaded(List<HotWord> hotWordList) {
        //todo 热词缓存
        // 当有推荐热词回来时，隐藏结果容器，显示推荐热词
        hideSuccessView();
        mHotWordsView.setVisibility(View.VISIBLE);
        if (mContentUILoader != null) {
            mContentUILoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }

        Log.d(TAG, "onHotWordLoaded: hotWordList --> " + hotWordList.size());
        List<String> hotWords = new ArrayList<>();
        hotWords.clear();
        for (HotWord hotWord : hotWordList) {
            hotWords.add(hotWord.getSearchword());
        }
        Collections.sort(hotWords);
        // 更新UI
        mHotWordsFlowLayout.setTextContents(hotWords);

    }

    @Override
    public void onLoadMoreResult(List<Album> moreResults, boolean isOK) {
        //处理加载更多的结果
        if (mSearchRefreshLayout != null) {
            mSearchRefreshLayout.finishLoadmore();
        }
        if (isOK) {
            handleSearchResult(moreResults);
        } else {
            Toast.makeText(this, "没有更多内容了", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGetSuggestWordLoaded(List<QueryResult> queryResults) {
        // 得到实时的联想词
        // 把数据设置给适配器
        mSuggestAdapter.setData(queryResults);
        // 控制UI的显示状态
        if (mContentUILoader != null) {
            mContentUILoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }
        hideSuccessView();
        mSuggestWordsRv.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        if (mContentUILoader != null) {
            mContentUILoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
        }
    }

    private void requestSearch() {
        // 执行搜索
        String inputKeyword = mInputBox.getText().toString().trim();
        if (TextUtils.isEmpty(inputKeyword)) {
            Toast.makeText(SearchActivity.this, "输入框为空，请输入内容后搜索", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (mSearchPresenter != null) {
                mSearchPresenter.doSearch(inputKeyword);
                mContentUILoader.updateStatus(UILoader.UIStatus.LOADING);
            }
        }
    }

    private void switch2Search(String text) {
        // 1.把热词放到输入框
        mInputBox.setText(text);
        mInputBox.setSelection(text.length());
        // 2.搜索
        if (mSearchPresenter != null) {
            mSearchPresenter.doSearch(text);
            if (mContentUILoader != null) {
                mContentUILoader.updateStatus(UILoader.UIStatus.LOADING);
            }
        }
    }

    private void hideSuccessView() {
        mSuggestWordsRv.setVisibility(View.GONE);
        mHotWordsView.setVisibility(View.GONE);
        mSearchRefreshLayout.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(int position, Album album) {
        AlbumDetailPresenter.getInstance().setTargetAlbum(album);
        // item被点击了
        // 根据位置拿到数据
        Intent intent = new Intent(this, DetailActivity.class);
        startActivity(intent);
    }
}
package com.yininghuang.zhihudailynews.comment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yininghuang.zhihudailynews.BaseActivity;
import com.yininghuang.zhihudailynews.BaseFragment;
import com.yininghuang.zhihudailynews.R;
import com.yininghuang.zhihudailynews.adapter.ZhihuCommentAdapter;
import com.yininghuang.zhihudailynews.model.ZhihuComments;
import com.yininghuang.zhihudailynews.utils.ItemDecoration;
import com.yininghuang.zhihudailynews.widget.AutoLoadRecyclerView;

import java.util.List;

/**
 * Created by Yining Huang on 2016/10/20.
 */

public class ZhihuCommentFragment extends BaseFragment implements ZhihuCommentContract.View {

    private AutoLoadRecyclerView mContentRec;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mRootView;
    private ZhihuCommentContract.Presenter mPresenter;
    private ZhihuCommentAdapter mAdapter;

    public static ZhihuCommentFragment newInstance(int id) {
        Bundle args = new Bundle();
        args.putInt("id", id);
        ZhihuCommentFragment fragment = new ZhihuCommentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_zhihu_comment, container, false);
        initViews(mRootView);
        mPresenter.init(getArguments().getInt("id"));
        return mRootView;
    }

    private void initViews(View rootView) {
        mContentRec = rootView.findViewById(R.id.contentRec);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeLayout);
        mContentRec.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new ZhihuCommentAdapter(getActivity());
        mContentRec.setAdapter(mAdapter);
        RecyclerView.ItemDecoration itemDecoration;
        if (((BaseActivity) getActivity()).getThemeId() == BaseActivity.DARK_THEME)
            itemDecoration = new ItemDecoration(getActivity(), R.color.colorDividerDark);
        else itemDecoration = new ItemDecoration(getActivity(), R.color.colorDivider);
        mContentRec.addItemDecoration(itemDecoration);

        mContentRec.setOnLoadingListener(() -> {
            if (mContentRec.isRefreshing())
                return;
            List<ZhihuComments.ZhihuComment> comments = mAdapter.getComments();
            if (!comments.isEmpty())
                mPresenter.queryHistoryComments(comments.get(comments.size() - 1).getId());
        });

        mSwipeRefreshLayout.setOnRefreshListener(() -> mPresenter.reload());
    }

    @Override
    public void setLoadingStatus(boolean status) {
        mSwipeRefreshLayout.setRefreshing(status);
    }

    @Override
    public void setHistoryLoadingStatus(boolean status) {
        mContentRec.setRefresh(status);
    }

    @Override
    public void showComments(List<ZhihuComments.ZhihuComment> comments) {
        mAdapter.getComments().clear();
        mAdapter.addComments(comments);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showCommentsCount(int count) {
        ((ZhihuCommentActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.commentsWithCount, count));
    }

    @Override
    public void addHistoryComment(List<ZhihuComments.ZhihuComment> comments) {
        mAdapter.addComments(comments);
        mAdapter.notifyItemRangeInserted(mAdapter.getItemCount() - comments.size(), comments.size());
    }

    @Override
    public void showLoadError() {
        Snackbar.make(mRootView, R.string.load_error, Snackbar.LENGTH_LONG)
                .setAction(R.string.refresh, view -> mPresenter.reload())
                .show();
    }

    @Override
    public void showLoadComplete() {
        mAdapter.setLoadComplete();
        mContentRec.setLoadComplete();
    }

    @Override
    public void setPresenter(ZhihuCommentContract.Presenter presenter) {
        mPresenter = presenter;
    }
}

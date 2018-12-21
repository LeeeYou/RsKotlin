package com.leeeyou.zhihudaily.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.leeeyou.BaseFragment;
import com.leeeyou.IndexActivity;
import com.leeeyou.zhihudaily.model.ZhiHuDailyRepositoryKt;
import com.leeeyou.zhihudaily.model.bean.ZhiHuDaily;
import com.leeeyou.zhihudaily.model.bean.ZhiHuDailyItem;
import com.xyz.leeeyou.zhihuribao.R;

import org.joda.time.DateTime;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * ClassName: StoryFragment
 * Description: 【知乎日报】fragment , Java style
 * <p>
 * Author:      leeeyou
 * Date:        2017/4/24 13:46
 */
public class ZhiHuDailyFragment extends BaseFragment {

    private RecyclerView mRecyclerView;
    private ZhiHuDailyAdapter mAdapter;

    //when loading more, up to the date of the data can be loaded
    private String[] dateList = new String[7];
    private int mDatePosition = 0;
    private int mMostDate = 7;

    Observable<ZhiHuDaily> storyObservable;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.activity_story, container, false);
        mRecyclerView = (RecyclerView) inflate.findViewById(R.id.recyclerViewRiBao);
        return inflate;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initDateList();
        initAdapter();
        updateData();
    }

    private void initAdapter() {
        mAdapter = new ZhiHuDailyAdapter(R.layout.item_lv_story, null);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                if (mDatePosition < mMostDate - 1) {
                    storyObservable = ZhiHuDailyRepositoryKt.fetchStoryListByDate(dateList[++mDatePosition]);
                    fetchStoryData();
                } else {
                    mAdapter.loadMoreEnd();
                }
            }
        }, mRecyclerView);
    }

    private void fetchStoryData() {
        storyObservable
                .subscribeOn(Schedulers.newThread())
                .filter(new Func1<ZhiHuDaily, Boolean>() {
                    @Override
                    public Boolean call(ZhiHuDaily riBao) {
                        StringBuilder sb = new StringBuilder();
                        char[] chars = riBao.getDate().toCharArray();
                        for (int i = 0; i < chars.length; i++) {
                            if (i == 4 || i == 6) {
                                sb.append("-");
                            }
                            sb.append(chars[i]);
                        }

                        List<ZhiHuDailyItem> stories = riBao.getStories();
                        for (ZhiHuDailyItem story : stories) {
                            story.setDate(sb.toString());
                        }

                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ZhiHuDaily>() {
                    @Override
                    public void onCompleted() {
                        mAdapter.loadMoreComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        ((IndexActivity) getActivity()).refreshComplete();
                        e.printStackTrace();
                        com.leeeyou.util.T.showShort(getActivity(), "出错了:" + e.getMessage());
                    }

                    @Override
                    public void onNext(ZhiHuDaily ribao) {
                        ((IndexActivity) getActivity()).refreshComplete();
                        setDataToAdapter(ribao.getStories());
                    }
                });
    }

    private void setDataToAdapter(@NonNull List<ZhiHuDailyItem> stories) {
        if (mDatePosition == 0) {
            mAdapter.setNewData(stories);
        } else {
            mAdapter.addData(stories);
        }
    }

    private void initDateList() {
        DateTime mDateTime = DateTime.now();
        for (int i = 0; i < mMostDate; i++) {
            DateTime tempDateTime = mDateTime.minusDays(i);
            dateList[i] = String.valueOf(tempDateTime.getYear()) +
                    (tempDateTime.getMonthOfYear() < 10 ? "0" + tempDateTime.getMonthOfYear() : tempDateTime.getMonthOfYear()) +
                    (tempDateTime.getDayOfMonth() < 10 ? "0" + tempDateTime.getDayOfMonth() : tempDateTime.getDayOfMonth());
        }
    }

    @Override
    public boolean checkCanDoRefresh() {
        return !mRecyclerView.canScrollVertically(-1);
    }

    @Override
    public void updateData() {
        mAdapter.removeAllFooterView();
        mDatePosition = 0;
        storyObservable = ZhiHuDailyRepositoryKt.fetchStoryListByDate(dateList[mDatePosition]);
        fetchStoryData();
    }

}
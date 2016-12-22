package com.junyenhuang.birdhouse.carol;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.junyenhuang.birdhouse.items.House;

import java.util.ArrayList;
import java.util.List;

public class CarolOverviewAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {
    private static final String TAG = CarolOverviewAdapter.class.getSimpleName();
    public final static float BIG_SCALE = 1.0f;
    private OverviewMainActivity context;
    private FragmentManager fragmentManager;
    private List<Fragment> mFragments;
    private ArrayList<House> mHouses;

    public CarolOverviewAdapter(OverviewMainActivity context, FragmentManager fm,
                                List<Fragment> frags, ArrayList<House> houses)
    {
        super(fm);
        this.fragmentManager = fm;
        this.context = context;
        mFragments = frags;
        mHouses = houses;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
        //return HouseFragment.newInstance(context, mHouses.get(position), position, scale);
    }

    @Override
    public int getCount() {
        if(mFragments == null) {
            return 0;
        } else {
            return mFragments.size();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        try {
            if (positionOffset >= 0f && positionOffset <= 1f) {
                if(OverviewMainActivity.mPageCounter != null
                        && OverviewMainActivity.mPagerShadow != null && mFragments != null) {
                    OverviewMainActivity.mPageCounter.setText(String.valueOf(position + 1) + "/" + getCount());
                    OverviewMainActivity.mPagerShadow.setText(String.valueOf(position + 1) + "/" + getCount());
                    OverviewMainActivity.mSelectedPage = position;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public void updateData(ArrayList<House> houses, List<Fragment> frags) {
        if(houses != null && mFragments != null) {
            mFragments.clear();
            mFragments.addAll(frags);
            mHouses.clear();
            mHouses.addAll(houses);
            notifyDataSetChanged();
        }
    }
}
package com.cs180.ucrtinder.ucrtinder.tindercard;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cs180.ucrtinder.ucrtinder.R;

/**
 * Created by bananapanda on 10/22/15.
 */
public class SwipePhotoAdapter extends PagerAdapter {

    public int getCount() {
        return 3;
    }

    public Object instantiateItem(ViewGroup collection, int position) {
        LayoutInflater inflater = (LayoutInflater) collection.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int resId = 0;
        switch (position) {
            case 0:
                resId = R.layout.profile_photo1;
                break;
            case 1:
                resId = R.layout.profile_photo2;
                break;
            case 2:
                resId = R.layout.profile_photo3;
                break;
        }
        View view = inflater.inflate(resId, null);
        ((ViewPager) collection).addView(view, 0);
        return view;
    }
    @Override
    public void destroyItem(ViewGroup arg0, int arg1, Object arg2) {
        ((ViewPager) arg0).removeView((View) arg2);
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == ((View) arg1);
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
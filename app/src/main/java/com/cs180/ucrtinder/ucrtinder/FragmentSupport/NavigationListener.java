package com.cs180.ucrtinder.ucrtinder.FragmentSupport;

import android.support.v4.view.GravityCompat;
import android.view.Gravity;
import android.view.View;

/**
 * Created by daniel on 10/30/15.
 */
public class NavigationListener implements View.OnClickListener{
    private AndroidDrawer mAndroidDrawer;

    public NavigationListener(AndroidDrawer androidDrawer){
        mAndroidDrawer = androidDrawer;
    }

    @Override
    public void onClick(View v) {
        if (mAndroidDrawer != null) {
            if(!mAndroidDrawer.getDrawerLayout().isDrawerOpen(GravityCompat.START)){
                mAndroidDrawer.getDrawerLayout().openDrawer(GravityCompat.START);
            }
            else{
                mAndroidDrawer.getDrawerLayout().closeDrawers();
            }
        }
    }
}

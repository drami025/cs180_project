package com.cs180.ucrtinder.ucrtinder.FragmentSupport;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cs180.ucrtinder.ucrtinder.ui.ConversationActivity;
import com.cs180.ucrtinder.ucrtinder.ui.MainActivity;
import com.cs180.ucrtinder.ucrtinder.ui.MatchedMessageActivity;
import com.cs180.ucrtinder.ucrtinder.ui.ProfileActivity;
import com.cs180.ucrtinder.ucrtinder.R;
import com.cs180.ucrtinder.ucrtinder.ui.SettingsActivity;

/**
 * Created by daniel on 10/23/15.
 */
public class AndroidDrawer {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mPosition;
    private AppCompatActivity mActivity;

    public AndroidDrawer(AppCompatActivity activity, int drawer_layout, int left_drawer){

        mActivity = activity;

        mPosition = getPosition(activity);
        mDrawerLayout = (DrawerLayout) mActivity.findViewById(drawer_layout);
        mDrawerLayout.setBackgroundColor(Color.WHITE);

        mDrawerList = (ListView) mActivity.findViewById(left_drawer);
        String[] naviItems = mActivity.getResources().getStringArray(R.array.menu_items);
        String[] descriptionItems = mActivity.getResources().getStringArray(R.array.menu_descriptions);

        mDrawerList.setAdapter(new AndroidDrawerAdapter(mActivity, naviItems, descriptionItems, mPosition));

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, R.string.open, R.string.close){
            public void onDrawerClosed(View view){
                super.onDrawerClosed(view);
                mActivity.invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView){
                super.onDrawerOpened(drawerView);
                mActivity.invalidateOptionsMenu();
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerLayout.post(new Runnable(){
            @Override
            public void run(){
                mDrawerToggle.syncState();
            }
        });
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id){
            selectItem(position, mPosition);
        }
    }

    private void selectItem(int position, int match){

        mDrawerLayout.closeDrawers();

        if(position == match){
            return;
        }

        Intent intent = null;

        switch(position){
            case 0:
                intent = new Intent(mActivity, ProfileActivity.class);
                break;
            case 1:
                intent = new Intent(mActivity, MainActivity.class);
                break;
            case 2:
                //intent = new Intent(mActivity, MatchedMessageActivity.class);
                intent = new Intent(mActivity, ConversationActivity.class);
                break;
            case 4:
                intent = new Intent(mActivity, SettingsActivity.class);
                break;
            default:
                break;
        }

        if(intent != null)
            mActivity.startActivity(intent);
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    public int getPosition(AppCompatActivity activity){

        if(activity instanceof ProfileActivity){
            return 0;
        }
        else if(activity instanceof MainActivity){
            return 1;
        }
        else if(activity instanceof ConversationActivity){
            return 2;
        }

        return -1;
    }

}

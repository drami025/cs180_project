package com.cs180.ucrtinder.ucrtinder.FragmentSupport;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.cs180.ucrtinder.ucrtinder.ui.ConversationActivity;
import com.cs180.ucrtinder.ucrtinder.ui.LoadingScreenActivity;
import com.cs180.ucrtinder.ucrtinder.ui.LoginActivity;
import com.cs180.ucrtinder.ucrtinder.ui.MainActivity;
import com.cs180.ucrtinder.ucrtinder.ui.PreferencesActivity;
import com.cs180.ucrtinder.ucrtinder.ui.ProfileActivity;
import com.cs180.ucrtinder.ucrtinder.R;
import com.cs180.ucrtinder.ucrtinder.ui.SettingsActivity;
import com.parse.ParseUser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by daniel on 10/23/15.
 */
public class AndroidDrawer {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mPosition;
    private AppCompatActivity mActivity;
    private ImageView mProfileImage;

    public AndroidDrawer(AppCompatActivity activity, int drawer_layout, int left_drawer, int profile_pic){
        this(activity, drawer_layout, left_drawer, profile_pic, null);
    }

    public AndroidDrawer(AppCompatActivity activity, int drawer_layout, int left_drawer, int profile_pic, final Context context){

        mActivity = activity;

        mPosition = getPosition(activity);
        mDrawerLayout = (DrawerLayout) mActivity.findViewById(drawer_layout);
        mDrawerLayout.setBackgroundColor(Color.WHITE);

        mProfileImage = (ImageView) mActivity.findViewById(profile_pic);
        mProfileImage.setOnClickListener(new PhotoClickListener());
        ExecutorService es = Executors.newFixedThreadPool(1);
        es.execute(new ProfileImageRunnable());

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

                if(context != null) {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(drawerView.getWindowToken(), 0);
                }
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

    private class PhotoClickListener implements ImageView.OnClickListener{

        @Override
        public void onClick(View view) {
            if(mPosition != 0){
                Intent intent = new Intent(mActivity, ProfileActivity.class);
                mActivity.startActivity(intent);
            }
        }
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
                intent = new Intent(mActivity, LoadingScreenActivity.class);
                break;
            case 1:
                intent = new Intent(mActivity, MainActivity.class);
                break;
            case 2:
                intent = new Intent(mActivity, ConversationActivity.class);
                break;
            case 3:
                intent = new Intent(mActivity, PreferencesActivity.class);
                break;
            case 4:
                intent = new Intent(mActivity, SettingsActivity.class);
                break;
            case 7:
                ParseUser.logOut();
                intent = new Intent(mActivity, LoginActivity.class);
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

        if(activity instanceof LoadingScreenActivity){
            return 0;
        }
        else if(activity instanceof MainActivity){
            return 1;
        }
        else if(activity instanceof ConversationActivity){
            return 2;
        }
        else if(activity instanceof PreferencesActivity){
            return 3;
        }
        else if(activity instanceof SettingsActivity){
            return 4;
        }

        return -1;
    }

    class ProfileImageRunnable implements Runnable {

        @Override
        public void run() {
            final Bitmap bmp;

            try {
                ParseUser user = ParseUser.getCurrentUser();
                String urlString = user.getString("photo0");
                URL url = new URL(urlString);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProfileImage.setImageBitmap(bmp);
                    }
                });
            } catch(MalformedURLException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            } catch (NullPointerException n) {
                n.printStackTrace();
            }
        }
    }

}

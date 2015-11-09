package com.cs180.ucrtinder.ucrtinder.tindercard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.cs180.ucrtinder.ucrtinder.Parse.ParseConstants;
import com.cs180.ucrtinder.ucrtinder.R;
import com.cs180.ucrtinder.ucrtinder.ui.CardProfileActivity;
import com.cs180.ucrtinder.ucrtinder.ui.ProfileActivity;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by bananapanda on 10/22/15.
 */
public class SwipePhotoAdapter extends PagerAdapter {

    public static Bitmap bmap;
    public static String ActivityType = "";
    public static String User = "";
    private List<ParseUser> profileUser;

    public SwipePhotoAdapter() {

    }

    public SwipePhotoAdapter(String type, String user) {
        ActivityType = type;
        User = user;
    }

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


        ParseUser parseUser = null;
        // Select the image to place on the view
        if (ActivityType.equals(ProfileActivity.KEY_USERPROFILE) || ActivityType.equals("")) { //current user
            parseUser = ParseUser.getCurrentUser();
        } else if (ActivityType.equals(CardProfileActivity.KEY_CARDPROFILE)) { //card user
            if (!User.equals("")) {
               ParseQuery<ParseUser> mainQuery = ParseUser.getQuery();
                mainQuery.whereEqualTo(ParseConstants.KEY_OBJECTID, User);
                try {
                    profileUser = mainQuery.find();
                } catch (Exception e) {}
                if(profileUser != null) {
                    parseUser = profileUser.get(0);
                }
            } else {
                parseUser = null;
            }
        }
        ImageView imageView = null;
        Bitmap bitmap = null;

        if (parseUser != null) {
            if (resId == R.layout.profile_photo1) {
                // Set picture from parse onto picture slider
                imageView = (ImageView) view.findViewById(R.id.imageView1);
                String url = null;
                try {
                    url = parseUser.getString(ParseConstants.KEY_PHOTO0);
                } catch (NullPointerException n) {
                    n.printStackTrace();
                }
                if (url == null || (url.equals(""))) {
                    url = parseUser.getString(ParseConstants.KEY_PROFILEPIC);
                }
                bitmap = getBitmapFromURL(url);

            } else if (resId == R.layout.profile_photo2) {
                // Set picture from parse onto picture slider
                imageView = (ImageView) view.findViewById(R.id.imageView2);
                String url = null;
                try {
                    url = parseUser.getString(ParseConstants.KEY_PHOTO1);
                } catch (NullPointerException n) {
                    n.printStackTrace();
                }
                if (url == null || (url.equals(""))) {
                    url = parseUser.getString(ParseConstants.KEY_PROFILEPIC);
                }
                bitmap = getBitmapFromURL(url);

            } else if (resId == R.layout.profile_photo3) {
                // Set picture from parse onto picture slider
                imageView = (ImageView) view.findViewById(R.id.imageView3);
                String url = null;
                try {
                    url = parseUser.getString(ParseConstants.KEY_PHOTO2);
                } catch (NullPointerException n) {
                    n.printStackTrace();
                }

                if (url == null || (url.equals(""))) {
                    url = parseUser.getString(ParseConstants.KEY_PROFILEPIC);
                }
                bitmap = getBitmapFromURL(url);
            }

            // Set the picture on the view
            if (bitmap != null && imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }

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

    Drawable drawable_from_url(String url, String src_name) throws
            java.net.MalformedURLException, java.io.IOException
    {

        return Drawable.createFromStream(((java.io.InputStream)
                new java.net.URL(url).getContent()), src_name);
    }

    public static Bitmap getBitmapFromURL(final String src) {
        if (src != null) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(src);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        bmap = BitmapFactory.decodeStream(input);
                    } catch (IOException e) {
                        // Log exception
                        bmap = null;
                    }
                }
            });

            t.start();

            try {
                t.join();
            } catch (InterruptedException i) {
                i.printStackTrace();
            }
        }
        else {
            bmap = null;
        }

        return bmap;
    }
}
package com.cs180.ucrtinder.ucrtinder.tindercard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.cs180.ucrtinder.ucrtinder.Parse.ParseConstants;
import com.cs180.ucrtinder.ucrtinder.R;
import com.cs180.ucrtinder.ucrtinder.ui.CardProfileActivity;
import com.cs180.ucrtinder.ucrtinder.ui.ProfileActivity;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by bananapanda on 10/22/15.
 */
public class SwipePhotoAdapter extends PagerAdapter {

    public static Bitmap bmap;
    public static String ActivityType = "";
    public static String User = "";
    private List<ParseUser> profileUser;
    private boolean currentUserhasPhotos;
    private boolean candidateUserhasPhotos;
    private boolean allNull;
    private Bitmap photo0;
    private Bitmap photo1;
    private Bitmap photo2;

    private ParseUser activeUser;


    public SwipePhotoAdapter() {
        currentUserhasPhotos = false;
        candidateUserhasPhotos = false;
        allNull = true;

        photo0 = null;
        photo1 = null;
        photo2 = null;

        activeUser = null;
    }

    public SwipePhotoAdapter(String type, String user) {
        ActivityType = type;
        User = user;
        currentUserhasPhotos = false;
        candidateUserhasPhotos = false;
        allNull = true;

        photo0 = null;
        photo1 = null;
        photo2 = null;

        activeUser = null;
    }

    @Override
    public int getCount() {
        return 3;
    }


    public void pullPhotos() {
        if (ActivityType.equals(ProfileActivity.KEY_USERPROFILE) || ActivityType.equals("")) { //current user
            pullPhotosFromURLs();
        } else if (ActivityType.equals(CardProfileActivity.KEY_CARDPROFILE)) { //card user
            pullCandidatePhotosFromURLs();
        }
    }

    private void pullPhotosFromURLs() {

        allNull = true;
        currentUserhasPhotos = false;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                activeUser = ParseUser.getCurrentUser();
                if (activeUser != null) {
                    String url = null;
                    url = activeUser.getString(ParseConstants.KEY_PHOTO0);
                    if (url == null || (url.equals(""))) {
                        url = activeUser.getString(ParseConstants.KEY_PROFILEPIC);
                    }
                    photo0 = getBitmapFromURL(url);

                    url = null;
                    url = activeUser.getString(ParseConstants.KEY_PHOTO1);
                    if (url == null || (url.equals(""))) {
                        url = activeUser.getString(ParseConstants.KEY_PROFILEPIC);
                    }
                    photo1 = getBitmapFromURL(url);

                    url = null;
                    url = activeUser.getString(ParseConstants.KEY_PHOTO2);
                    if (url == null || (url.equals(""))) {
                        url = activeUser.getString(ParseConstants.KEY_PROFILEPIC);
                    }
                    photo2 = getBitmapFromURL(url);
                }

                if (photo0 != null) {
                    currentUserhasPhotos = true;
                }

                if (photo0 == null && photo1 == null && photo2 == null) {
                    allNull = false;
                }
            }
        });

        t.start();
    }

    private void pullCandidatePhotosFromURLs() {

        allNull = true;
        candidateUserhasPhotos = false;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ParseQuery<ParseUser> mainQuery = ParseUser.getQuery();
                mainQuery.whereEqualTo(ParseConstants.KEY_OBJECTID, User);
                try {
                    mainQuery.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> list, ParseException e) {
                            if (e != null) {
                                Log.e(getClass().getSimpleName(), "Failed to find candidate");
                                e.printStackTrace();
                            } else {
                                activeUser = list.get(0);
                                //View view = updatePhotos(parseUser, inflater, resId, collection);

                                String url = null;
                                url = activeUser.getString(ParseConstants.KEY_PHOTO0);
                                if (url == null || (url.equals(""))) {
                                    url = activeUser.getString(ParseConstants.KEY_PROFILEPIC);
                                }
                                photo0 = getBitmapFromURL(url);

                                url = null;
                                url = activeUser.getString(ParseConstants.KEY_PHOTO1);
                                if (url == null || (url.equals(""))) {
                                    url = activeUser.getString(ParseConstants.KEY_PROFILEPIC);
                                }
                                photo1 = getBitmapFromURL(url);

                                url = null;
                                url = activeUser.getString(ParseConstants.KEY_PHOTO2);
                                if (url == null || (url.equals(""))) {
                                    url = activeUser.getString(ParseConstants.KEY_PROFILEPIC);
                                }
                                photo2 = getBitmapFromURL(url);

                                if (photo0 != null) {
                                    candidateUserhasPhotos = true;
                                    allNull = false;
                                }
                                if (photo0 != null || photo1 != null || photo2 != null) {
                                    allNull = false;
                                }
                            }
                        }

                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();
    }

    public boolean getCurrentUserPhotoBool() {
        return currentUserhasPhotos;
    }

    public boolean getCandidateUserPhotoBool() {
        return candidateUserhasPhotos;
    }

    public boolean getAllNullBool() {
        return allNull;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        final LayoutInflater inflater = (LayoutInflater) collection.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int resId_sub = 0;
        switch (position) {
            case 0:
                resId_sub = R.layout.profile_photo1;
                break;
            case 1:
                resId_sub = R.layout.profile_photo2;
                break;
            case 2:
                resId_sub = R.layout.profile_photo3;
                break;
        }
        final int resId = resId_sub;

        // update photos on the ui thread
        return updatePhotos(inflater, resId, collection);
    }

    public View updatePhotos(LayoutInflater inflater, int resId, ViewGroup collection) {
        View view = inflater.inflate(resId, null);
        ImageView imageView = null;
        Bitmap bitmap = null;

        if (resId == R.layout.profile_photo1) {
            // Set picture from parse onto picture slider
            imageView = (ImageView) view.findViewById(R.id.imageView1);
            bitmap = photo0;

        } else if (resId == R.layout.profile_photo2) {
            // Set picture from parse onto picture slider
            imageView = (ImageView) view.findViewById(R.id.imageView2);
            bitmap = photo1;

        } else if (resId == R.layout.profile_photo3) {
            // Set picture from parse onto picture slider
            imageView = (ImageView) view.findViewById(R.id.imageView3);
            bitmap = photo2;
        }

        // Set the picture on the view
        if (bitmap != null && imageView != null) {
            imageView.setImageBitmap(bitmap);
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

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
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
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
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
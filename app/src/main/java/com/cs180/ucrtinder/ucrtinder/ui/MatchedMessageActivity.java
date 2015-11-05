package com.cs180.ucrtinder.ucrtinder.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.cs180.ucrtinder.ucrtinder.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.ucrtinder.R;

import java.util.ArrayList;
import java.util.List;

public class MatchedMessageActivity extends AppCompatActivity {

    AndroidDrawer mDrawer;

    ListView MessageList;
    /** Declaring an ArrayAdapter to set items to ListView */
    ArrayAdapter<String> adapter;
    /** Items entered by the user is stored in this ArrayList variable */
    ArrayList<String> list = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matched_message);

        //Hiding actionBar - AP
        try {
            getSupportActionBar().hide();
        } catch(NullPointerException e){
            e.printStackTrace();
        }

        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());
        */


        // Creating an android drawer to slide in from the left side
        mDrawer = new AndroidDrawer(this, R.id.drawer_layout_matched_message, R.id.left_drawer_matched_message);


        MessageList = (ListView) findViewById(R.id.MessageList);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.abc_list_divider_mtrl_alpha);
        Drawable divider = new BitmapDrawable(getResources(), bm);
        MessageList.setDivider(divider);



        list.add("one");
        list.add("two");
        list.add("three");
        list.add("four");
        /** Defining the ArrayAdapter to set items to ListView */
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);



        /** Setting the adapter to the ListView */
        MessageList.setAdapter(adapter);
    }



}

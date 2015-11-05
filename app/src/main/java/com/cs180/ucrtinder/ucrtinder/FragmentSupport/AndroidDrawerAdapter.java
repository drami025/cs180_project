package com.cs180.ucrtinder.ucrtinder.FragmentSupport;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cs180.ucrtinder.ucrtinder.R;

/**
 * Created by daniel on 10/23/15.
 */
public class AndroidDrawerAdapter extends BaseAdapter{
    String[] mNaviItems;
    String[] mDescriptionItems;
    int mCurrentActivityPosition;
    Context mContext;

    public AndroidDrawerAdapter(Context context, String[] items, String[] descriptions, int position){
        mNaviItems = items;
        mDescriptionItems = descriptions;
        mCurrentActivityPosition = position;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mNaviItems.length;
    }

    @Override
    public Object getItem(int position) {
        return mNaviItems[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v == null){
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            v = inflater.inflate(R.layout.drawer_list_item, parent, false);
        }

        TextView item = (TextView) v.findViewById(R.id.fragment_drawer_item);
        item.setText(mNaviItems[position]);

        TextView description = (TextView) v.findViewById(R.id.fragment_drawer_description);
        description.setText(mDescriptionItems[position]);

        if(mCurrentActivityPosition == position){
            Resources resource = mContext.getResources();
            v.setBackgroundColor(Color.parseColor(resource.getString(R.string.selected_item_color)));
            item.setTextColor(Color.parseColor(resource.getString(R.string.selected_text_color)));
        }

        return v;
    }
}

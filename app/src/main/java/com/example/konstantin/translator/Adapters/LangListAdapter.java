package com.example.konstantin.translator.Adapters;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


public class LangListAdapter extends ArrayAdapter {
    private final ArrayList<Map.Entry<String, String>> mData;
    private int resourceTitle;

    public LangListAdapter(@NonNull Context context, @LayoutRes int resource, Map<String, String> map) {
        super(context, resource);
        mData = new ArrayList<>();

        mData.addAll(map.entrySet());

        Collections.sort(mData, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> t1, Map.Entry<String, String> t2) {
                return t1.getValue().compareTo(t2.getValue());

            }
        });
        this.resourceTitle = resource;
    }


    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<String, String> getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public String getKey(int position)
    {
        return mData.get(position).getKey();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(resourceTitle, parent, false);
        } else {
            result = convertView;
        }

        Map.Entry<String, String> item = getItem(position);

        if (item != null) {
            ((TextView) result.findViewById(android.R.id.text1)).setText(item.getValue());
        }

        return result;
    }

}
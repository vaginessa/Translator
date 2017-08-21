package com.example.konstantin.translator.Adapters;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.konstantin.translator.Database;
import com.example.konstantin.translator.R;


public class BookmarkListAdapter extends CursorAdapter implements Filterable {
    private LayoutInflater inflater;
    private int layout;
    private Database db;
    private Cursor cursor;
    private String type;
    private SparseBooleanArray cbArray;

    public BookmarkListAdapter(Context context, Database db, Cursor c, int layout, String type) {
        super(context, c,  0);
        this.db = db;
        this.layout = layout;
        this.cursor = c;
        this.type = type;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        cbArray = new SparseBooleanArray();
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = inflater.inflate(layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(convertView);
        //int id = cursor.getInt(cursor.getColumnIndex("_id"));
        boolean checked = cursor.getInt(cursor.getColumnIndex("in_favorite")) == 1;
        viewHolder.cb_favorite.setTag(cursor.getPosition());
        viewHolder.cb_favorite.setChecked(checked);
        cbArray.put(cursor.getPosition(), checked);
        viewHolder.cb_favorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                db.setState(getIdFromPosition((int)compoundButton.getTag()), b, "favorite");
                cbArray.put((int)compoundButton.getTag(), b);
            }
        });

        convertView.setTag(viewHolder);

        return convertView;
    }

    public int getIdFromPosition(int position)
    {
        cursor.moveToPosition(position);
        return cursor.getInt(cursor.getColumnIndex("_id"));

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.text_source.setText(cursor.getString(cursor.getColumnIndex("source")));
        viewHolder.text_lang.setText(cursor.getString(cursor.getColumnIndex("source_lang")) + "-" + cursor.getString(cursor.getColumnIndex("target_lang")));
        viewHolder.text_translation.setText(cursor.getString(cursor.getColumnIndex("translation")));
        viewHolder.cb_favorite.setTag(cursor.getPosition());
        viewHolder.cb_favorite.setChecked(cbArray.get(cursor.getPosition()));
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        this.cursor = cursor;

        if (cursor.moveToFirst())
        {
            cbArray.clear();

            do {
                cbArray.put(cursor.getPosition(), cursor.getInt(cursor.getColumnIndex("in_favorite")) == 1);
            }while (cursor.moveToNext());
        }
    }



    private static class ViewHolder {
        TextView text_source;
        TextView text_translation;
        TextView text_lang;
        CheckBox cb_favorite;



        public ViewHolder(View view) {
            text_source = ((TextView) view.findViewById(R.id.text_source));
            text_lang = ((TextView) view.findViewById(R.id.text_lang));
            text_translation = ((TextView) view.findViewById(R.id.text_translation));
            cb_favorite = (CheckBox) view.findViewById(R.id.cb_favorite);
        }
    }
}



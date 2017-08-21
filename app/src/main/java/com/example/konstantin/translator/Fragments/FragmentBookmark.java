package com.example.konstantin.translator.Fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.konstantin.translator.Database;
import com.example.konstantin.translator.MainActivity;
import com.example.konstantin.translator.Adapters.BookmarkListAdapter;
import com.example.konstantin.translator.R;


public class FragmentBookmark extends Fragment {

    private Context context;
    private Database db;
    private ListView listView;
    private BookmarkListAdapter adapter;
    private EditText editFind;
    private ImageButton btnFindHistory, btnClearEditHistory;
    private View view;
    private MenuItem item;
    private String type;
    private TextView textMsg;
    private LinearLayoutCompat searchBar, layoutMsg;
    private AppCompatImageView imgMsg;


    public FragmentBookmark() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString("type");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_bookmark, container, false);
        db = ((MainActivity)context).getDB();

        listView = (ListView) view.findViewById(R.id.list_history);
        editFind = (EditText) view.findViewById(R.id.edit_find_history_favorite);
        btnFindHistory = (ImageButton) view.findViewById(R.id.btn_find_history);
        btnClearEditHistory = (ImageButton) view.findViewById(R.id.btn_clear_edit_history);
        searchBar = (LinearLayoutCompat)  view.findViewById(R.id.search_bar);
        layoutMsg = (LinearLayoutCompat)  view.findViewById(R.id.layout_fragment_history_favorite_msg);
        textMsg = ((TextView) view.findViewById(R.id.text_msg_history_favorite));
        imgMsg = ((AppCompatImageView) view.findViewById(R.id.img_msg_history_favorite));


        setHasOptionsMenu(true);

        Cursor cursor = db.getAll(type);
        if (cursor.getCount() == 0) {
            searchBar.setVisibility(View.INVISIBLE);
        }
        adapter = new BookmarkListAdapter(context, db, cursor, R.layout.item_bookmark, type);
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                return db.Filter(constraint.toString(), type);
            }

        });


        listView.setAdapter(adapter);
        listView.setEmptyView(layoutMsg);
        listView.setOnItemLongClickListener (new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView parent, View view, final int position, long id) {
                final CharSequence[] items = { getResources().getString(R.string.msgDelete )};

                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        db.setState(adapter.getIdFromPosition(position), false, type);
                        notifyAdapterChange();
                    }

                }).create().show();
                return false;
            }
        });


        if (type.equals("history"))
        {
            editFind.setHint(getResources().getString(R.string.findInHistory));
            imgMsg.setImageResource(R.drawable.ic_timelapse_black_100dp);
        } else {
            editFind.setHint(getResources().getString(R.string.findInFavorites));
            imgMsg.setImageResource(R.drawable.ic_timelapse_black_100dp);
        }

        editFind.clearFocus();
        editFind.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0 && btnClearEditHistory.getVisibility() == View.INVISIBLE) {
                    adapter.getFilter().filter(charSequence);
                    btnClearEditHistory.setVisibility(View.VISIBLE);
                } else if (charSequence.length() == 0)
                {
                    btnClearEditHistory.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnFindHistory.setOnClickListener(OnClick);
        btnClearEditHistory.setOnClickListener(OnClick);
        return view;
    }

    private View.OnClickListener OnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.btn_clear_edit_history:
                    editFind.setText("");
                    btnClearEditHistory.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };


    public void notifyAdapterChange() {

        if (adapter != null) {
            editFind.setText("");
            editFind.clearFocus();
            adapter.changeCursor(db.getAll(type));

            if (adapter.getCount() == 0) {
                searchBar.setVisibility(View.INVISIBLE);

            } else {
                searchBar.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_bookmarks, menu);
        this.item = menu.getItem(0);
        if (adapter.getCount() == 0)
            this.item.setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.item1)
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if (type.equals("history")) {
                builder.setMessage(getResources().getString(R.string.clearHistory));
            } else {
                builder.setMessage(getResources().getString(R.string.clearFavorite));
            }
            builder .setCancelable(true)
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            db.clear(type);
                            notifyAdapterChange();
                        }
                    })
                    .setNegativeButton("Отмена", null);

            AlertDialog alertdialog = builder.create();
            alertdialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

}

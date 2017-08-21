package com.example.konstantin.translator;

import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.konstantin.translator.Adapters.LangListAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class LangListActivity extends AppCompatActivity {

    Toolbar toolbar;
    String type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lang_list);

        toolbar = (Toolbar) findViewById(R.id.toolbar_lang_list);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();

        String title = intent.getStringExtra("title");
        type = intent.getStringExtra("type");

        getSupportActionBar().setTitle(title);

        Map<String, String> json = null;

        AssetManager mAssetManager = getApplicationContext().getAssets();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    mAssetManager.open("lang_ru.txt")));
            String str = br.readLine();
            if (str != null) {
                json = JsonYandex.parseJSONLangList(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        final LangListAdapter adapter = new LangListAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, json);
        ListView listView = (ListView) findViewById(R.id.list_lang);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.putExtra("type", type);
                intent.putExtra("lang_short", adapter.getKey(i));
                intent.putExtra("lang", adapter.getItem(i).getValue());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

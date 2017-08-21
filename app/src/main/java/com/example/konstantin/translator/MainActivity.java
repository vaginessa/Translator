package com.example.konstantin.translator;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.konstantin.translator.Fragments.FragmentBookmarks;
import com.example.konstantin.translator.Fragments.FragmentSettings;
import com.example.konstantin.translator.Fragments.FragmentTranslate;

public class MainActivity extends AppCompatActivity{

    Fragment fragmentTranslate;
    Fragment fragmentBookmarks;
    Fragment fragmentSettings;
    BottomNavigationView navigation;


    private String currentFragmentTag = "fragmentTranslate";
    private Database db;
    public Database getDB() {
        return db;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        db = new Database(this);

        fragmentTranslate = new FragmentTranslate();
        fragmentBookmarks = new FragmentBookmarks();
        fragmentSettings = new FragmentSettings();
        getSupportFragmentManager().beginTransaction().add(R.id.content,fragmentTranslate, "fragmentTranslate").commit();



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (!currentFragmentTag.equals("fragmentTranslate"))
        {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content,fragmentTranslate, "fragmentTranslate")
                    .commit();
            currentFragmentTag = "fragmentTranslate";
            navigation.setSelectedItemId(R.id.navigation_home);
        } else {
            super.onBackPressed();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right)
                            .replace(R.id.content,fragmentTranslate, "fragmentTranslate")
                            .commit();
                    currentFragmentTag = "fragmentTranslate";
                    return true;
                case R.id.navigation_dashboard:
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    if (currentFragmentTag.equals("fragmentTranslate"))
                    {
                        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                    }else{
                        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
                    }
                    ft.replace(R.id.content,fragmentBookmarks, "fragmentBookmarks").commit();
                    currentFragmentTag = "fragmentBookmarks";
                    return true;
                case R.id.navigation_notifications:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
                            .replace(R.id.content,fragmentSettings, "fragmentSettings")
                            .commit();
                    currentFragmentTag = "fragmentSettings";
                    return true;
            }
            return false;


        }

    };

}

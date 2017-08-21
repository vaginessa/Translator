package com.example.konstantin.translator.Fragments;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.konstantin.translator.LangListActivity;
import com.example.konstantin.translator.Database;
import com.example.konstantin.translator.MainActivity;
import com.example.konstantin.translator.Preferences;
import com.example.konstantin.translator.R;
import com.example.konstantin.translator.YandexTranslate;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class FragmentTranslate extends Fragment {


    private OnFragmentInteractionListener mListener;
    private TextView translatedText, coop;
    private EditText sourceText;
    private ImageButton imgChangeLang;
    private YandexTranslate translatorAPI;
    private Timer timer;
    private Context context;
    private Database db;
    private IntentFilter intentFilter;
    private Button langFrom, langTo;
    private AppCompatImageButton btnClearSource;
    private ProgressBar progressBar;
    private String currentLangFromShort, currentLangToShort;
    private String currentLangFrom, currentLangTo;
    public static final String CONNECTIVITY_ACTION_LOLLIPOP = "com.example.CONNECTIVITY_ACTION_LOLLIPOP";
    private Preferences preferences;
    final Handler handler = new Handler();
    private TextTranslation currentTranslation;
    private boolean isKeyboardShow = false;

    public FragmentTranslate() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
        intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(CONNECTIVITY_ACTION_LOLLIPOP);
        preferences = new Preferences(getActivity());
        this.db = ((MainActivity)context).getDB();
        currentTranslation = new TextTranslation();
    }

    @Override
    public void onPause() {
        super.onPause();
        preferences.saveLang(currentLangFrom, currentLangFromShort, currentLangTo, currentLangToShort);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_translate, container, false);
        initView(view);
        loadPreferences();
        return view;
    }


    public void initView(final View view) {

        translatedText = (TextView) view.findViewById(R.id.translatedText);
        sourceText = (EditText) view.findViewById(R.id.sourceText);
        langFrom = (Button) view.findViewById(R.id.langFrom);
        langTo = (Button) view.findViewById(R.id.langTo);
        imgChangeLang = (ImageButton) view.findViewById(R.id.swapLang);
        coop = (TextView) view.findViewById(R.id.coop);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
        btnClearSource = (AppCompatImageButton) view.findViewById(R.id.btnClearSource);

        langFrom.setOnClickListener(onClick);
        langTo.setOnClickListener(onClick);
        imgChangeLang.setOnClickListener(onClick);
        coop.setOnClickListener(onClick);


        sourceText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(startTranslate);
                    }
                }, 500);

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing to do here
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // user is typing: reset already started timer (if existing)
                if (timer != null) {
                    timer.cancel();
                }
                if (charSequence.length() > 0 && btnClearSource.getVisibility() == View.INVISIBLE) {
                    btnClearSource.setVisibility(View.VISIBLE);
                } else if (charSequence.length() == 0)
                {
                    btnClearSource.setVisibility(View.INVISIBLE);
                    translatedText.setText("");
                }
            }
        });

        translatorAPI = new YandexTranslate(new YandexTranslate.IYandexTranslate() {
            @Override
            public void onTranslate(String text) {
                translatedText.setText(text);
                currentTranslation.translatedText = text;
                progressBar.setVisibility(View.INVISIBLE);
                if (!isKeyboardShow) {
                    db.addToHistory(currentTranslation.sourceText,
                            currentTranslation.translatedText,
                            currentTranslation.currentLangFromShort,
                            currentTranslation.currentLangToShort);
                }
            }

            @Override
            public void onError(int code, String message) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectionError(String message) {
                context.registerReceiver(mBroadcastReceiver, intentFilter);
                registerConnectivityActionLollipop();
            }
        });

        KeyboardVisibilityEvent.setEventListener(
                getActivity(),
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                       if (!isOpen && currentTranslation.isComplete()) {
                           db.addToHistory(currentTranslation.sourceText,
                                   currentTranslation.translatedText,
                                   currentTranslation.currentLangFromShort,
                                   currentTranslation.currentLangToShort);
                       }
                        isKeyboardShow = isOpen;
                    }
                });
        btnClearSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sourceText.setText("");

            }
        });
    }


    View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {
                case R.id.langFrom:
                    intent = new Intent(getActivity(), LangListActivity.class);
                    intent.putExtra("title", "Язык текста");
                    intent.putExtra("type", "langFrom");
                    startActivityForResult(intent, 1);
                    break;
                case R.id.langTo:
                    intent = new Intent(getActivity(), LangListActivity.class);
                    intent.putExtra("title", "Язык перевода");
                    intent.putExtra("type", "langTo");
                    startActivityForResult(intent, 1);
                    break;

                case R.id.swapLang:
                    String str1 = currentLangToShort;
                    String str2 = currentLangTo;

                    currentLangToShort = currentLangFromShort;
                    currentLangTo = currentLangFrom;
                    langTo.setText(currentLangFrom);

                    currentLangFromShort = str1;
                    currentLangFrom = str2;
                    langFrom.setText(str2);

                    if (!translatedText.getText().equals(""))
                    {
                        sourceText.setText(translatedText.getText());
                    }
                    handler.post(startTranslate);

                    break;
                case R.id.coop:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://translate.yandex.ru/")));

            }


        }
    };

    final Runnable startTranslate = new Runnable() {
        public void run() {
            String source = sourceText.getText().toString().trim();
            if (source.length() > 0) {
                currentTranslation = new TextTranslation();
                currentTranslation.currentLangFromShort = currentLangFromShort;
                currentTranslation.currentLangToShort = currentLangToShort;
                currentTranslation.sourceText = source;

                String text = db.getTranslateFromHistory(source, currentLangFromShort, currentLangToShort);
                if (!text.equals(""))
                {
                    currentTranslation.translatedText = text;
                    translatedText.setText(text);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    translatedText.setText("");
                    translatorAPI.translate(source, currentLangFromShort + "-" + currentLangToShort);
                }
            }
        }
    };


    private void loadPreferences()
    {

        Map<String, String> lang = preferences.loadLang();

        currentLangToShort = lang.get("currentLangToShort");
        currentLangTo = lang.get("currentLangTo");
        langTo.setText(currentLangTo);

        currentLangFromShort = lang.get("currentLangFromShort");
        currentLangFrom = lang.get("currentLangFrom");
        langFrom.setText(currentLangFrom);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        preferences.saveLang(currentLangFrom, currentLangFromShort, currentLangTo, currentLangToShort);

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
        sourceText.setText(currentTranslation.sourceText);
        translatedText.setText(currentTranslation.translatedText);


    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        View view = inflater.inflate(R.layout.fragment_translate, viewGroup);
        initView(view);
        loadPreferences();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null)
        {
            return;
        }
        String type = data.getStringExtra("type");
        String lang = data.getStringExtra("lang");
        String langShort = data.getStringExtra("lang_short");

        if (type.equals("langTo"))
        {
            currentLangToShort = langShort;
            currentLangTo = lang;
            langTo.setText(lang);

        } else {
            currentLangFromShort = langShort;
            currentLangFrom = lang;
            langFrom.setText(lang);
        }

        preferences.saveLang(currentLangFrom, currentLangFromShort, currentLangTo, currentLangToShort);
        handler.post(startTranslate);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void registerConnectivityActionLollipop() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        connectivityManager.registerNetworkCallback(builder.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                Intent intent = new Intent(CONNECTIVITY_ACTION_LOLLIPOP);
                intent.putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

                context.sendBroadcast(intent);
            }

            @Override
            public void onLost(Network network) {
                Intent intent = new Intent(CONNECTIVITY_ACTION_LOLLIPOP);
                intent.putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, true);

                context.sendBroadcast(intent);
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && TextUtils.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION) ||
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && TextUtils.equals(intent.getAction(), CONNECTIVITY_ACTION_LOLLIPOP)) {

                if (intent.getExtras() != null) {
                    final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {

                        context.unregisterReceiver(mBroadcastReceiver);
                        handler.post(startTranslate);
                    }
                }

            }
        }
    };

    private class TextTranslation
    {
        private String currentLangFromShort;
        private String currentLangToShort;
        private String sourceText;
        private String translatedText;

        private TextTranslation()
        {
            currentLangFromShort = "";
            currentLangToShort = "";
            sourceText = "";
            translatedText = "";
        }

        private boolean isComplete()
        {
            return currentLangFromShort.length() > 0
                    && currentLangTo.length() > 0
                    && sourceText.length() > 0
                    && translatedText.length() > 0;
        }
    }
}

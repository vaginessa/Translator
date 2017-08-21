package com.example.konstantin.translator;


import com.example.konstantin.translator.Json.ErrorJSON;
import com.example.konstantin.translator.Json.TranslateJSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class YandexTranslate {

    private String keyTranslate = "trnsl.1.1.20170411T161012Z.ab73cddef9e1bd37.a6951a5fad4add5e1426c87e20aa8b1f1bc676ef";


    private static Retrofit rfTranslate = null;
    private final String urlTranslate = "https://translate.yandex.net/";
    private static YandexApiTranslate yandexApiTranslate;
    private IYandexTranslate iYandexTranslate;
    private Call<TranslateJSON> call;

    public YandexTranslate(IYandexTranslate iYandexTranslate)
    {
        this.iYandexTranslate = iYandexTranslate;
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        rfTranslate = new Retrofit.Builder()
                .baseUrl(urlTranslate)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        yandexApiTranslate = rfTranslate.create(YandexApiTranslate.class);

    }

    public void translate(String text, String lang)
    {

        if (call!= null )
        {
            call.cancel();
        }
        call = yandexApiTranslate.getTranslate(keyTranslate, text, lang);
        call.enqueue(new Callback<TranslateJSON>() {
            @Override
            public void onResponse(Call<TranslateJSON> call, Response<TranslateJSON> response) {
                if (response.isSuccessful() && response.body() != null)
                {
                    iYandexTranslate.onTranslate(response.body().getText().get(0));
                }
                else if (response.errorBody() != null)
                {
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    ErrorJSON errorJSON;
                    try {
                        errorJSON = gson.fromJson(response.errorBody().string(), ErrorJSON.class);
                        iYandexTranslate.onError(errorJSON.getCode(), errorJSON.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                        iYandexTranslate.onError(-1, "error");
                    }
                } else {
                    iYandexTranslate.onError(-1, "error");
                }
            }
            @Override
            public void onFailure(Call<TranslateJSON> call, Throwable t) {
                if (!call.isCanceled())
                    iYandexTranslate.onConnectionError(t.getMessage());
            }
        });

    }

    private interface YandexApiTranslate {
        @POST("/api/v1.5/tr.json/translate")
        Call<TranslateJSON> getTranslate(@Query("key") String key, @Query("text") String text, @Query("lang") String lang);

    }



    public interface IYandexTranslate {

        void onTranslate(String translatedText);
        void onError(int code, String message);
        void onConnectionError(String message);
    }

}



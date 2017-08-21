package com.example.konstantin.translator;


import com.example.konstantin.translator.Json.LangListJSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Map;


public final class JsonYandex {

    public static Map<String,String> parseJSONLangList(String txtLangs)
    {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        LangListJSON langList = gson.fromJson(txtLangs, LangListJSON.class);
        return langList.getLangs();
    }
}

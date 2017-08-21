package com.example.konstantin.translator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Switch;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Database {

    private final String DB_NAME = "translate.db";
    private final int DB_VERSION = 1;
    private final String DB_TABLE_NAME = "history";
    private mSQLiteOpenHelper dbHelper;
    private SQLiteDatabase db;

    public static final String ID = "_id";
    public static final String SOURCE = "source";
    public static final String TRANSLATION = "translation";
    public static final String SOURCE_LANG = "source_lang";
    public static final String TARGET_LANG = "target_lang";
    public static final String IN_HISTORY = "in_history";
    public static final String IN_FAVORITE = "in_favorite";
    public static final String DATE_HISTORY = "date_history";
    public static final String DATE_FAVORITE = "date_favorite";




    public Database(Context context)
    {
        String path = context.getDatabasePath(DB_NAME).getPath();

        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }

        dbHelper = new mSQLiteOpenHelper(context, DB_NAME, null, DB_VERSION);
        db = dbHelper.getWritableDatabase();
        clearTable();
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private String getColumnFromType(String type)
    {
        switch (type)
        {
            case "history":
                return IN_HISTORY;
            case "favorite":
                return IN_FAVORITE;
        }
        return null;
    }

    private long isExistInHistory(String sourceText, String sourceLang, String targetLang)
    {
        long result = -1;
        Cursor c = db.rawQuery("SELECT * FROM " + DB_TABLE_NAME + " WHERE source = ? AND source_lang = ? AND target_lang = ? LIMIT 1", new String[] {sourceText, sourceLang, targetLang});
        if (c.getCount() > 0)
        {
            c.moveToFirst();
            result = c.getInt(c.getColumnIndex("_id"));
        }

        c.close();
        return result;
    }

    public void addToFavorit(String sourceText, String sourceLang, String targetLang)
    {
        ContentValues cv = new ContentValues();
        long id = isExistInHistory(sourceText, sourceLang, targetLang);

        if (id > -1 )
        {
            cv.put("date_favorite", getDateTime());
            cv.put("in_favorite", true);
            db.update(DB_TABLE_NAME, cv, "_id = ?", new String[] { String.valueOf(id) });
        }
    }

    public void addToHistory(String sourceText, String translationText, String sourceLang, String targetLang)
    {
        ContentValues cv = new ContentValues();
        long id = isExistInHistory(sourceText, sourceLang, targetLang);

        if (id > -1 )
        {
            cv.put("date_history", getDateTime());
            cv.put("in_history", true);
            db.update(DB_TABLE_NAME, cv, "_id = ?", new String[] { String.valueOf(id) });
            return;
        }

        cv.put("source", sourceText);
        cv.put("translation", translationText);
        cv.put("source_lang", sourceLang);
        cv.put("target_lang", targetLang);
        cv.put("in_history", true);
        cv.put("in_favorite", false);
        cv.put("date_history", getDateTime());
        db.insert(DB_TABLE_NAME, null, cv);
    }

    private void clearTable()
    {
        db.delete(DB_TABLE_NAME, "in_history = 0 AND in_favorite = 0", null);
    }

    public void clear(String type) {
        ContentValues cv = new ContentValues();
        String columnName = getColumnFromType(type);
        cv.put(columnName, false);
        db.update(DB_TABLE_NAME, cv, columnName + " = 1", null);
        clearTable();
    }

    public Cursor getAll(String type)
    {
        return db.query(DB_TABLE_NAME,
                    new String[]{"_id", "in_favorite", "in_history", "source", "translation", "source_lang", "target_lang", "date_history"},
                    getColumnFromType(type) + " = 1",
                    null,
                    null,
                    null,
                    "date_history DESC");
    }


    public boolean setState(int id, boolean value, String type)
    {
        ContentValues cv = new ContentValues();
        cv.put(getColumnFromType(type), value);
        boolean result = db.update(DB_TABLE_NAME, cv, "_id = ?", new String[]{String.valueOf(id)}) > 0;
        clearTable();
        return result;

    }

    public Cursor Filter(String text, String type)
    {
        return db.rawQuery("SELECT _id, in_favorite, in_history, source, translation, source_lang, target_lang FROM history WHERE " + getColumnFromType (type) + " = 1 AND (source LIKE '%" + text + "%' OR translation LIKE '%" + text + "%');", new String[]{});
    }

    public String getTranslateFromHistory(String string, String currentLangFromShort, String currentLangToShort) {

        String result = "";
        Cursor cursor = db.query(DB_TABLE_NAME,
                                new String[]{"_id", "source", "translation"},
                                "source = ? AND source_lang = ? AND target_lang = ?",
                                new String[]{string, currentLangFromShort, currentLangToShort},
                                null,
                                null,
                                null,
                                "1");
        if (cursor.moveToFirst())
        {
            result = cursor.getString(cursor.getColumnIndex("translation"));
        }
        cursor.close();
        return result;
    }


    private class mSQLiteOpenHelper extends SQLiteOpenHelper {
         private Context context;

        mSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
            this.context = context;
        }


        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("create table "+ DB_TABLE_NAME +" ("
                    + "_id integer primary key autoincrement,"
                    + "source text,"
                    + "translation text,"
                    + "source_lang text,"
                    + "target_lang text,"
                    + "in_history bool,"
                    + "in_favorite bool,"
                    + "date_history datetime,"
                    + "date_favorite datetime"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }
}
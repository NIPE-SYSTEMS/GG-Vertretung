package de.gebatzens.ggvertretungsplan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NewsFragmentDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "schulinfoapp";
    private static final int DATABASE_VERSION = 2;
    private static final String KEY_ID = "id";
    private static final String KEY_NEWS_TITLE = "news_title";
    private static final String NEWS_TABLE_NAME = "read_news";
    private static final String NEWS_TABLE_CREATE =
            "CREATE TABLE " + NEWS_TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY," +
                    KEY_NEWS_TITLE + " TEXT);";

    NewsFragmentDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NEWS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public String addReadNews(String news_title) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        values.put(KEY_NEWS_TITLE, news_title);
        db.insert(NEWS_TABLE_NAME, null, values);
        return ts;
    }

    public boolean checkNewsRead(String news_title) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {KEY_NEWS_TITLE};
        Cursor cursor =
                db.query(NEWS_TABLE_NAME, // a. table
                        columns, // b. column names
                        KEY_NEWS_TITLE + " = ?", // c. selections
                        new String[] { news_title }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit
        if (cursor != null) {
            cursor.moveToFirst();
        }
        if(cursor.getCount()==0) {
            return false;
        } else {
            return true;
        }
    }
}
package me.texot.rword;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by texot on 15/11/8.
 */
public class WordDbAdapter {
    private static final String TAG = "WordDbAdapter";

    private static final String DATABASE_NAME = "word.db";
    private static final String DATABASE_TABLE_WORD = "words";
    private static final String DATABASE_TABLE_LOG = "logs";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_WORD_KEY_ID = "id";
    private static final String TABLE_WORD_KEY_LIST_ID = "list_id";
    private static final String TABLE_WORD_KEY_WORD = "word";
    private static final String TABLE_WORD_KEY_CONTENT = "content";

    private static final String TABLE_LOG_KEY_ID = "id";
    private static final String TABLE_LOG_KEY_WORD_ID = "word_id";
    private static final String TABLE_LOG_KEY_TIME = "time";
    private static final String TABLE_LOG_KEY_RESULT = "result";

    private final Context mCtx;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static class DatabaseHelper extends SQLiteAssetHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, context.getExternalFilesDir(null).getAbsolutePath(), null, DATABASE_VERSION);
        }
    }

    public class WordData {
        public int id;
        public String word;
        public ArrayList<Pair<String, String>> pronAndParaphrList;
    }


    public WordDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public WordDbAdapter open() {
        if(mDb != null || mDbHelper != null) {
            if (mDb != null) Log.e(TAG, "open: mDb not null");
            if (mDbHelper != null) Log.e(TAG, "open: mDbHelper not null");
            close();
        }
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if(mDb != null) mDb.close();
        if(mDbHelper != null) mDbHelper.close();
        mDb = null;
        mDbHelper = null;
    }

    public String[] getWordList(int wordListId) {
        Cursor cs = mDb.query(DATABASE_TABLE_WORD, new String[]{TABLE_WORD_KEY_WORD},
                TABLE_WORD_KEY_LIST_ID + '=' + String.valueOf(wordListId), null, null, null, null);
        if(cs == null) return null;

        int count = cs.getCount();
        if(count == 0) return new String[0];

        String[] ret = new String[cs.getCount()];
        int counter = 0;
        cs.moveToFirst();
        do {
            ret[counter++] = cs.getString(0);
        } while(cs.moveToNext());
        cs.close();
        return ret;
    }

    private static ArrayList<Pair<String, String>> unpackContent(byte[] data) {
        ArrayList<Pair<String, String>> ret = new ArrayList<>();

        try {
            JSONObject jObject = new JSONObject(new String(data, "UTF-8"));
            JSONArray jSubwordArray = jObject.getJSONArray("subwords");
            for (int i = 0; i < jSubwordArray.length(); i++) {
                JSONObject jSubwordObject = jSubwordArray.getJSONObject(i);
                String pron = jSubwordObject.getString("pronounce");
                JSONArray jDetailArray = jSubwordObject.getJSONArray("details");
                String detail = "";
                for (int j = 0; j < jDetailArray.length(); j++) {
                    JSONObject jDetailObject = jDetailArray.getJSONObject(j);
                    JSONArray jPOSArray = jDetailObject.getJSONArray("POS");
                    String POS = "";
                    for (int k = 0; k < jPOSArray.length(); k++) {
                        POS += "/" + jPOSArray.getString(k);
                    }
                    if(POS.length() > 0)
                        POS = POS.substring(1);

                    JSONArray jParaphrArray = jDetailObject.getJSONArray("paraphrase");
                    String paraphr = "";
                    for (int k = 0; k < jParaphrArray.length(); k++) {
                        JSONObject jParaphrObject = jParaphrArray.getJSONObject(k);
                        paraphr += "；" + jParaphrObject.getString("cn");
                    }
                    if(paraphr.length() > 0)
                        paraphr = paraphr.substring(1);
                    detail += POS + " " + paraphr + "\n";
                }
                Pair<String, String> pronAndParaphr = new Pair<>(pron, detail);
                ret.add(pronAndParaphr);
            }

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        return ret;
    }

    public WordData[] getWordListWithContent(int wordListId) {
        Cursor cs = mDb.query(DATABASE_TABLE_WORD, new String[]{TABLE_WORD_KEY_ID, TABLE_WORD_KEY_WORD, TABLE_WORD_KEY_CONTENT},
                TABLE_WORD_KEY_LIST_ID + '=' + String.valueOf(wordListId), null, null, null, null);
        if(cs == null) return null;

        int count = cs.getCount();
        if(count == 0) return new WordData[0];

        WordData[] ret = new WordData[cs.getCount()];

        int counter = 0;
        cs.moveToFirst();
        do {
            ret[counter] = new WordData();
            ret[counter].id = cs.getInt(cs.getColumnIndex(TABLE_WORD_KEY_ID));
            ret[counter].word = cs.getString(cs.getColumnIndex(TABLE_WORD_KEY_WORD));
            ret[counter++].pronAndParaphrList = unpackContent(cs.getBlob(cs.getColumnIndex(TABLE_WORD_KEY_CONTENT)));
        } while(cs.moveToNext());
        cs.close();
        return ret;
    }

    public void logWordRemember(int wordId, int time, boolean result)
    {
        ContentValues cv = new ContentValues();
        cv.put(TABLE_LOG_KEY_WORD_ID, wordId);
        cv.put(TABLE_LOG_KEY_TIME, time);
        cv.put(TABLE_LOG_KEY_RESULT, result);
        mDb.insert(DATABASE_TABLE_LOG, null, cv);
    }


}

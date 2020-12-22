package com.example.mycoolweather;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    /*DatabaseHelper:
     * context:Activity   name:数据库名
     * SQLiteDatabase.CursorFactory factory：返回自定义的Cursor ，一般传入null
     * version 当前数据库版本号*/
    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    /*创建数据库表*/
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql="create table city(cityname text,weatherId text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

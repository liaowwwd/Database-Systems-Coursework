package com.example.creditenough;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper{
    public static final String CREATE_USER = "create table Users ("
            +"user text primary key NOT NULL,"
            +"password text)";
    public static final String CREATE_TYPE = "create table Types ("
            +"name text primary key NOT NULL,"
            +"credit integer)";
    public static final String CREATE_COURSE1 = "create table Courses1 ("
            +"name text primary key NOT NULL,"
            +"credit integer,"
            +"type text,"
            +"FOREIGN KEY (type) REFERENCES Types(name))";
    public static final String CREATE_COURSE2 = "create table Courses2 ("
            +"name text,"
            +"account text,"
            +"FOREIGN KEY (account) REFERENCES Users(user),"
            +"primary key (name,account))";


    private Context mContext;
    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER);
        db.execSQL(CREATE_TYPE);
        db.execSQL(CREATE_COURSE1);
        db.execSQL(CREATE_COURSE2);


        Toast.makeText(mContext, "创建数据库成功", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
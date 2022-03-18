package com.example.creditenough;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private MyDatabaseHelper dbHelper;
    private EditText coursenameEdit, creditEdit, typeEdit;
    private Button addcourse, lookfor, delete;
    private String account = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coursenameEdit = (EditText) findViewById(R.id.name);
        creditEdit = (EditText) findViewById(R.id.credit);
        typeEdit = (EditText) findViewById(R.id.type);
        dbHelper = new MyDatabaseHelper(this, "creditenough.db", null ,1);
        Intent intent = getIntent();
        //从Intent当中根据key取得Authority
        if (intent != null) {
            account = intent.getStringExtra("key");
            Log.e("key", account);
        }
        if (account.equals("admin")) {
            //管理员权限之 添加 & 修改 至Types表
            Button addtype= (Button) findViewById(R.id.addtype);
            addtype.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String creditStr = creditEdit.getText().toString();
                    String type = typeEdit.getText().toString();//大类名称
                    if (creditStr == null || type == null) {
                        Toast.makeText(MainActivity.this, "条件不齐全", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int credit= Integer.parseInt(creditStr);//大类需要的总学分
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    //先查询 看该大类是否已经存在
                    Cursor cursor = db.query("Types", null,"name = ?", new String[]{type},null,
                            null, null);
                    if (cursor.moveToNext()) {//该大类存在，修改值
                        values.put("credit", credit);
                        db.update("Types", values, "name = ?", new String[]{type});
                    }else{//该大类不存在
                        values.put("credit", credit);
                        values.put("name", type);
                        db.insert("Types", null, values);
                    }
                    Toast.makeText(MainActivity.this, "类型插入/修改操作完成", Toast.LENGTH_SHORT).show();
                }
            });

        }
        else{
            //查询还需要多少学分
            lookfor = (Button) findViewById(R.id.lookfor);
            lookfor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String type = typeEdit.getText().toString();
                    if (type == null) {
                        Toast.makeText(MainActivity.this, "条件不齐全", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    int CreditNeeded = 0,  CreditHave = 0;
                    //先查询type表，获得该类型应有总学分

                    Cursor cursor = db.query("Types", null,"name = ?",
                            new String[]{type},null, null, null);
                    if(cursor.moveToFirst()) {
                        CreditNeeded = cursor.getInt(cursor.getColumnIndex("credit"));//需要的总学分
                        Log.e(null, String.valueOf(CreditNeeded));
                    }else{
                        Toast.makeText(MainActivity.this, "查询类型不存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //再查询该用户已选课程
                    cursor = db.query("Courses2", null,"account=?", new String[]{account},null,
                            null, null);


                    if(cursor.moveToFirst()) {
                        do{
                            String coursename = cursor.getString(cursor.getColumnIndex("name"));
                            //查询课程表1中该课程
                            Cursor cursor2 = db.query("Courses1", null,"name=?", new String[]{coursename},null,
                                    null, null);
                            cursor2.moveToFirst();
                            CreditHave += cursor2.getInt(cursor2.getColumnIndex("credit"));//得到的总学分
                        }while (cursor.moveToNext());
                    }
                    if (CreditHave >= CreditNeeded) {
                        Toast.makeText(MainActivity.this, type+"类型学分已修满", Toast.LENGTH_SHORT).show();
                    }else{
                        int left = CreditNeeded - CreditHave;
                        Toast.makeText(MainActivity.this, type+"类型学分还需要"+String.valueOf(left), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            //添加课程进入表Courses
            addcourse = (Button) findViewById(R.id.addcourse);
            addcourse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String coursename = coursenameEdit.getText().toString();
                    String CreditStr = creditEdit.getText().toString();;
                    String type = typeEdit.getText().toString();
                    if(coursename == null || CreditStr == null || type == null) {
                        Toast.makeText(MainActivity.this, "条件不全无法查询", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int credit= Integer.parseInt(CreditStr);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    //如果该Course1 中课程已经存在，选择更改
                    Cursor cursor = db.query("Courses1", null,"name=?", new String[]{coursename},null,
                            null, null);
                    if(cursor.moveToFirst()) {
                        do{
                            ContentValues values = new ContentValues();
                            values.put("credit", credit);
                            values.put("type", type);
                            db.update("Courses1", values, "name = ?", new String[]{coursename});
                        }while (cursor.moveToNext());
                    }
                    else {//否则
                        ContentValues values1 = new ContentValues();
                        values1.put("name", coursename);
                        values1.put("credit", credit);
                        values1.put("type", type);
                        db.insert("Courses1", null, values1);
                    }
                    //对Courses2
                    cursor = db.query("Courses2", null,"name=? and account=?", new String[]{coursename, account},null,
                            null, null);
                    if(!cursor.moveToFirst()) {//不存在该关系
                        ContentValues values2 = new ContentValues();
                        values2.put("name", coursename);
                        values2.put("account", account);
                        db.insert("Courses2", null, values2);
                    }
                    Toast.makeText(MainActivity.this, "添加操作完成", Toast.LENGTH_SHORT).show();
                }
            });
            //删除Courses表中某学生的课程信息
            delete= (Button) findViewById(R.id.deletecourse);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String coursename = coursenameEdit.getText().toString();
                    if(coursename == null ) {
                        Toast.makeText(MainActivity.this, "条件不全无法删除", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    //删除过程只删除该用户与该课程的关系，即表2
                    db.delete("Courses2", "name = ? and account = ?", new String[]{coursename, account});
                    Toast.makeText(MainActivity.this, "删除操作完成", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}


package com.example.creditenough;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;


public class LoginActivity extends Activity {
    private EditText accountEdit;
    private EditText passwordEdit;
    private Button login, register;
    private MyDatabaseHelper dbHelper;
    Sha1Hex Hash= new Sha1Hex();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        accountEdit = (EditText) findViewById(R.id.account);
        passwordEdit = (EditText) findViewById(R.id.password);
        dbHelper = new MyDatabaseHelper(this, "creditenough.db", null ,1);

        register = (Button) findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String account = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                Log.e(null, password);
                try {
                    password = Hash.makeSHA1Hash(password);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                dbHelper.getWritableDatabase();
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("user", account);
                values.put("password", password);
                db.insert("Users", null, values);
                Toast.makeText(LoginActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
            }
        });

        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                Cursor cursor = db.query("Users", null,"user = ?", new String[]{account}
                        ,null,null, null);
                if (cursor.moveToFirst()) {
                    boolean exi = false;
                    try {
                        password = Hash.makeSHA1Hash(password);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    do{
                        String Hashpassword = cursor.getString(cursor.getColumnIndex("password"));//需要的总学分
                        if (Hashpassword.equals(password)) {
                            exi = true;
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            if (account.equals("admin")){
                                intent.putExtra("key","admin");
                            }
                            else{
                                intent.putExtra("key",account);
                            }
                            startActivity(intent);
                            finish();
                        }
                    }while(cursor.moveToNext());
                    if(!exi){
                        Toast.makeText(LoginActivity.this, "密码不正确", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else{//该大类不存在
                    Toast.makeText(LoginActivity.this, "请先注册", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

    }
}
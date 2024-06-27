package com.example.apptelefare2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptelefare2.DatabaseHelper;

import java.util.Random;

public class LoginActivity extends AppCompatActivity {
    private EditText etPhoneNumber, etVerificationCode;
    private Button btnSendCode, btnLogin;
    private CheckBox cbRememberMe;
    private String generatedCode;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etVerificationCode = findViewById(R.id.etVerificationCode);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnLogin = findViewById(R.id.btnLogin);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedPhoneNumber = sharedPreferences.getString("phoneNumber", "");
        if (!savedPhoneNumber.isEmpty()) {
            etPhoneNumber.setText(savedPhoneNumber);
        }

        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyAndLogin();
            }
        });
    }

    private void sendVerificationCode() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "请输入手机号码", Toast.LENGTH_SHORT).show();
            return;
        }

        generatedCode = String.format("%04d", new Random().nextInt(10000));
        Toast.makeText(this, "验证码已发送: " + generatedCode, Toast.LENGTH_SHORT).show();
    }

    private void verifyAndLogin() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String enteredCode = etVerificationCode.getText().toString().trim();

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "请输入手机号码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredCode.isEmpty()) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!enteredCode.equals(generatedCode)) {
            Toast.makeText(this, "验证码错误", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isPhoneNumberExists(phoneNumber)) {
            if (cbRememberMe.isChecked()) {
                sharedPreferences.edit().putString("phoneNumber", phoneNumber).apply();
            }

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            addPhoneNumberToDatabase(phoneNumber);
            Toast.makeText(this, "账号已创建，请重新登录", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isPhoneNumberExists(String phoneNumber) {
        Cursor cursor = db.query(DatabaseHelper.TABLE_ACCOUNT_INFO,
                new String[]{DatabaseHelper.COLUMN_NUMBER},
                DatabaseHelper.COLUMN_NUMBER + "=?",
                new String[]{phoneNumber},
                null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }

        return exists;
    }

    private void addPhoneNumberToDatabase(String phoneNumber) {
        db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_ACCOUNT_INFO + " (" +
                        DatabaseHelper.COLUMN_NUMBER + ", " +
                        DatabaseHelper.COLUMN_CALLS_MONEY + ", " +
                        DatabaseHelper.COLUMN_DATA_TYPE + ", " +
                        DatabaseHelper.COLUMN_DATA_LEFT + ", " +
                        DatabaseHelper.COLUMN_DISCOUNT + ") VALUES (?, 0, '', 0, 0)",
                new Object[]{phoneNumber});
    }
}

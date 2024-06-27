package com.example.apptelefare2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class RechargeActivity extends AppCompatActivity {

    private EditText etRechargeAmount;
    private Button btnRecharge;
    private Button btnGetCoupon;
    private TextView tvBalance;
    private Spinner spinnerCoupons;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        etRechargeAmount = findViewById(R.id.etRechargeAmount);
        btnRecharge = findViewById(R.id.btnRecharge);
        btnGetCoupon = findViewById(R.id.btnGetCoupon);
        tvBalance = findViewById(R.id.tvBalance);
        spinnerCoupons = findViewById(R.id.spinnerCoupons);

        // Load saved phone number from MainActivity
        phoneNumber = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
                .getString("phoneNumber", "");

        btnRecharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rechargePhone();
            }
        });

        btnGetCoupon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnHome();
            }
        });

        updateBalanceDisplay();
        loadCoupons();
    }

    private void returnHome() {
        Intent intent = new Intent(RechargeActivity.this, MainActivity.class);
        intent.putExtra("fragment", "home");
        startActivity(intent);
    }

    private void updateBalanceDisplay() {
        Cursor cursor = db.query(DatabaseHelper.TABLE_ACCOUNT_INFO,
                new String[]{DatabaseHelper.COLUMN_CALLS_MONEY},
                DatabaseHelper.COLUMN_NUMBER + "=?",
                new String[]{phoneNumber},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int callsMoneyIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CALLS_MONEY);
            if (callsMoneyIndex != -1) {
                double balance = cursor.getDouble(callsMoneyIndex);
                tvBalance.setText("话费余额: ￥" + String.format("%.2f", balance));
            } else {
                Toast.makeText(this, "无法获取话费余额信息", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        }
    }

    private void rechargePhone() {
        String rechargeAmountStr = etRechargeAmount.getText().toString();
        if (rechargeAmountStr.isEmpty()) {
            Toast.makeText(this, "请输入充值金额", Toast.LENGTH_SHORT).show();
            return;
        }

        double rechargeAmount = Double.parseDouble(rechargeAmountStr);
        double newBalance = rechargeAmount;
        double discount = applyDiscount(rechargeAmount);

        db.execSQL("UPDATE " + DatabaseHelper.TABLE_ACCOUNT_INFO +
                        " SET " + DatabaseHelper.COLUMN_CALLS_MONEY + " = " + DatabaseHelper.COLUMN_CALLS_MONEY + " + ?" +
                        " WHERE " + DatabaseHelper.COLUMN_NUMBER + " = ?",
                new Object[]{newBalance, phoneNumber});

        updateBalanceDisplay();
        Toast.makeText(this, "已充值 " + String.format("%.2f", rechargeAmount) + " 元，优惠 " +
                String.format("%.2f", discount) + " 元", Toast.LENGTH_SHORT).show();
        returnHome();
    }

    @SuppressLint("Range")
    private double applyDiscount(double amount) {
        double discount = 0.0;

        // 查询满足条件的优惠券
        Cursor cursor = db.query(DatabaseHelper.TABLE_DISCOUNT_INFO,
                new String[]{DatabaseHelper.COLUMN_DIS_MONEY},
                DatabaseHelper.COLUMN_DIS_CONDITION + " <= ?",
                new String[]{String.valueOf(amount)},
                null, null, DatabaseHelper.COLUMN_DIS_CONDITION + " DESC", "1");

        if (cursor != null && cursor.moveToFirst()) {
            discount = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_DIS_MONEY));
            cursor.close();
        }

        return discount;
    }

    private void loadCoupons() {
        // 查询用户已领取的优惠券
        Cursor cursor = db.query(DatabaseHelper.TABLE_DISCOUNT_INFO,
                new String[]{DatabaseHelper.COLUMN_DIS_MONEY},
                null,
                null,
                null, null, null);

        List<String> couponList = new ArrayList<>();
        while (cursor != null && cursor.moveToNext()) {
            @SuppressLint("Range") double discount = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_DIS_MONEY));
            couponList.add("优惠券: ￥" + String.format("%.2f", discount));
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, couponList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCoupons.setAdapter(adapter);
    }

}

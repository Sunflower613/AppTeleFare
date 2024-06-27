package com.example.apptelefare2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class FlowRechargeActivity extends AppCompatActivity {

    private Spinner spinnerFlowPackages;
    private TextView tvFlowPackageInfo, tvRemainingFlow;
    private Button btnRechargeFlow;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private String[] flowPackages = {"普通包 - 5GB", "夜间包 - 10GB", "月末包 - 8GB", "流量日包 - 1GB"};
    private String selectedFlowPackage;
    private String phoneNumber;
    private double remainingFlow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_recharge);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        spinnerFlowPackages = findViewById(R.id.spinnerFlowPackages);
        tvFlowPackageInfo = findViewById(R.id.tvFlowPackageInfo);
        tvRemainingFlow = findViewById(R.id.tvRemainingFlow);
        btnRechargeFlow = findViewById(R.id.btnRechargeFlow);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, flowPackages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFlowPackages.setAdapter(adapter);

        // Load saved phone number from MainActivity
        phoneNumber = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
                .getString("phoneNumber", "");

        btnRechargeFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedFlowPackage = spinnerFlowPackages.getSelectedItem().toString();
                handleFlowPackageSelection();
            }
        });

        loadRemainingFlow();
        updateRemainingFlowDisplay();
    }

    private void returnHome() {
        Intent intent = new Intent(FlowRechargeActivity.this, MainActivity.class);
        intent.putExtra("fragment", "home");
        startActivity(intent);
    }

    private void handleFlowPackageSelection() {
        if (selectedFlowPackage.contains("夜间包") || selectedFlowPackage.contains("月末包")) {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("你选择的流量包仅在特殊时段可用!")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            rechargeFlowPackage();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            rechargeFlowPackage();
        }
    }

    private void rechargeFlowPackage() {
        double flowToAdd = 0.0;

        if (selectedFlowPackage.contains("1GB")) {
            flowToAdd = 1.0;
        } else if (selectedFlowPackage.contains("5GB")) {
            flowToAdd = 5.0;
        } else if (selectedFlowPackage.contains("10GB")) {
            flowToAdd = 10.0;
        } else if (selectedFlowPackage.contains("8GB")) {
            flowToAdd = 8.0;
        }

        remainingFlow += flowToAdd;
        updateDatabaseWithNewFlow();
        updateRemainingFlowDisplay();
        Toast.makeText(this, "已成功充值 " + flowToAdd + "GB 流量包", Toast.LENGTH_SHORT).show();
        returnHome();
    }

    @SuppressLint("Range")
    private void loadRemainingFlow() {
        Cursor cursor = db.query(DatabaseHelper.TABLE_ACCOUNT_INFO,
                new String[]{DatabaseHelper.COLUMN_DATA_LEFT},
                DatabaseHelper.COLUMN_NUMBER + "=?",
                new String[]{phoneNumber},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            remainingFlow = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATA_LEFT));
            cursor.close();
        } else {
            remainingFlow = 0.0;
        }
    }

    private void updateDatabaseWithNewFlow() {
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_ACCOUNT_INFO +
                        " SET " + DatabaseHelper.COLUMN_DATA_LEFT + " = ?" +
                        " WHERE " + DatabaseHelper.COLUMN_NUMBER + " = ?",
                new Object[]{remainingFlow, phoneNumber});
    }

    private void updateRemainingFlowDisplay() {
        tvRemainingFlow.setText("剩余流量: " + String.format("%.2f", remainingFlow) + "GB");
    }
}

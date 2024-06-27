package com.example.apptelefare2.ui.home;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.apptelefare2.R;
import com.example.apptelefare2.RechargeActivity;
import com.example.apptelefare2.FlowRechargeActivity;
import com.example.apptelefare2.DatabaseHelper;
import com.example.apptelefare2.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dbHelper = new DatabaseHelper(getContext());

        final TextView textView = binding.tvWelcome;

        Button btnRechargeBalance = binding.btnRechargeBalance;
        Button btnRechargeFlow = binding.btnRechargeFlow;

        Button btnCoupon50 = binding.imageButton;
        Button btnCoupon100 = binding.imageButton2;
        Button btnCoupon200 = binding.imageButton3;

        Button btnNightPackage = binding.imageButton4;
        Button btnMonthEndPackage = binding.imageButton5;
        Button btnDayPackage = binding.imageButton6;

        // Load saved phone number from MainActivity
        String phoneNumber = getActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
                .getString("phoneNumber", "");

        // Load user profile data from database and display
        loadUserProfile(phoneNumber);

        btnRechargeBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RechargeActivity.class);
                startActivity(intent);
            }
        });

        btnRechargeFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FlowRechargeActivity.class);
                startActivity(intent);
            }
        });

        btnCoupon50.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCouponToDatabase(50, 2);
            }
        });

        btnCoupon100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCouponToDatabase(100, 5);
            }
        });

        btnCoupon200.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCouponToDatabase(200, 12);
            }
        });

        btnNightPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FlowRechargeActivity.class);
                startActivity(intent);
            }
        });

        btnMonthEndPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FlowRechargeActivity.class);
                startActivity(intent);
            }
        });

        btnDayPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FlowRechargeActivity.class);
                startActivity(intent);
            }
        });

        return root;
    }

    private void loadUserProfile(String phoneNumber) {
        // Query the database for user profile information based on phoneNumber
        Cursor cursor = dbHelper.getUserProfile(phoneNumber);

        if (cursor != null && cursor.moveToFirst()) {
            // Read data from cursor
            @SuppressLint("Range") double balance = cursor.getDouble(cursor.getColumnIndex("calls_money"));
            @SuppressLint("Range") double remainingFlow = cursor.getDouble(cursor.getColumnIndex("data_left"));

            // Update UI with user profile data
            binding.btnRechargeBalance.setText("话费  ￥" + String.format("%.2f", balance));
            binding.btnRechargeFlow.setText("流量 " + String.format("%.2f", remainingFlow) + "GB");

            // Close cursor after use
            cursor.close();
        } else {
            Toast.makeText(getActivity(), "未能获取用户信息", Toast.LENGTH_SHORT).show();
        }
    }

    private void addCouponToDatabase(int condition, int discount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DIS_CONDITION, condition);
        values.put(DatabaseHelper.COLUMN_DIS_MONEY, discount);
        db.insert(DatabaseHelper.TABLE_DISCOUNT_INFO, null, values);
        Toast.makeText(getContext(), "已领取优惠券: 满" + condition + "减" + discount, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

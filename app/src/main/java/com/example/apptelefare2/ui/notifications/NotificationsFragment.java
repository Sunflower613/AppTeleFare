package com.example.apptelefare2.ui.notifications;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.apptelefare2.DatabaseHelper;
import com.example.apptelefare2.LoginActivity;
import com.example.apptelefare2.MyPrizesActivity;
import com.example.apptelefare2.R;

public class NotificationsFragment extends Fragment {

    private TextView tvUsername, tvBalance, tvRemainingFlow;
    private ImageView imageView;
    private Button btnMyCoupons, btnMyFlowPackages, btnMyPrizes, btnRechargeHistory, btnLogout;
    private Spinner spinnerMyCoupons, spinnerMyFlowPackages, spinnerRechargeHistory;

    // Database Helper instance
    private DatabaseHelper dbHelper;

    // User information columns in database
    private static final String COLUMN_USERNAME = "number"; // Assuming "number" is the column name for username (phone number)
    private static final String COLUMN_BALANCE = "calls_money";
    private static final String COLUMN_REMAINING_FLOW = "data_left";

    private String phoneNumber; // Saved phone number from MainActivity

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        dbHelper = new DatabaseHelper(getActivity());

        // Initialize views
        tvUsername = view.findViewById(R.id.tvUsername);
        tvBalance = view.findViewById(R.id.tvBalance);
        tvRemainingFlow = view.findViewById(R.id.tvRemainingFlow);
        imageView = view.findViewById(R.id.imageView);

        btnMyCoupons = view.findViewById(R.id.btnMyCoupons);
        btnMyFlowPackages = view.findViewById(R.id.btnMyFlowPackages);
        btnMyPrizes = view.findViewById(R.id.btnMyPrizes);
        btnRechargeHistory = view.findViewById(R.id.btnRechargeHistory);
        btnLogout = view.findViewById(R.id.btnLogout);

        spinnerMyCoupons = view.findViewById(R.id.spinnerMyCoupons);
        spinnerMyFlowPackages = view.findViewById(R.id.spinnerMyFlowPackages);
        spinnerRechargeHistory = view.findViewById(R.id.spinnerRechargeHistory);

        // Load saved phone number from MainActivity
        phoneNumber = getActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
                .getString("phoneNumber", "");

        // Load user profile data from database and display
        loadUserProfile();

        // Button click listeners
        btnMyPrizes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMyPrizes();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        return view;
    }

    private void loadUserProfile() {
        // Query the database for user profile information based on phoneNumber
        Cursor cursor = dbHelper.getUserProfile(phoneNumber);

        if (cursor != null && cursor.moveToFirst()) {
            // Read data from cursor
            String username = phoneNumber;
            @SuppressLint("Range") double balance = cursor.getDouble(cursor.getColumnIndex(COLUMN_BALANCE));
            @SuppressLint("Range") double remainingFlow = cursor.getDouble(cursor.getColumnIndex(COLUMN_REMAINING_FLOW));

            // Update UI with user profile data
            tvUsername.setText("用户名: " + username);
            tvBalance.setText("话费余额: ￥" + String.format("%.2f", balance));
            tvRemainingFlow.setText("剩余流量: " + String.format("%.2f", remainingFlow) + "GB");

            // Close cursor after use
            cursor.close();
        } else {
            Toast.makeText(getActivity(), "未能获取用户信息", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMyPrizes() {
        // Navigate to MyPrizesActivity to show user's prizes
        Intent intent = new Intent(getActivity(), MyPrizesActivity.class);
        startActivity(intent);
    }

    private void logout() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Close the database connection when fragment view is destroyed
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}

package com.example.apptelefare2.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apptelefare2.DatabaseHelper;
import com.example.apptelefare2.MyPrizesActivity;
import com.example.apptelefare2.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Random;

public class DashboardFragment extends Fragment {

    private ImageButton btnStartLottery;
    private TextView tvBalance;
    private GridLayout gridLayout;
    private FrameLayout[] frameLayouts;
    private ImageView[] imageViews;
    private int remainingLotteryTimes;
    private int[] prizeImages = {R.drawable.prize100mb, R.drawable.prize5yuan, R.drawable.prize1gb, R.drawable.prize300mb, R.drawable.prize20yuan,R.drawable.thanks, R.drawable.prize500mb, R.drawable.prize10yuan};
    private String[] prizeNames = {"100MB流量", "5元话费", "1GB流量", "300MB流量", "20元话费", "谢谢参与","500MB流量", "10元话费"};
    private Random random = new Random();

    private Handler handler;
    private Runnable runnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        btnStartLottery = view.findViewById(R.id.btnStartLottery);
        tvBalance = view.findViewById(R.id.tvBalance);
        gridLayout = view.findViewById(R.id.gridLayout);

        imageViews = new ImageView[]{
                view.findViewById(R.id.imageView1), view.findViewById(R.id.imageView2), view.findViewById(R.id.imageView3),
                view.findViewById(R.id.imageView4), view.findViewById(R.id.imageView5), view.findViewById(R.id.imageView6),
                view.findViewById(R.id.imageView7), view.findViewById(R.id.imageView8)
        };

        frameLayouts = new FrameLayout[]{
                view.findViewById(R.id.frameLayout1), view.findViewById(R.id.frameLayout2), view.findViewById(R.id.frameLayout3),
                view.findViewById(R.id.frameLayout4), view.findViewById(R.id.frameLayout5), view.findViewById(R.id.frameLayout6),
                view.findViewById(R.id.frameLayout7), view.findViewById(R.id.frameLayout8)
        };

        // Initialize remaining lottery times (this value should come from the user's total amount spent)
        remainingLotteryTimes = calculateLotteryTimes(150); // Example value
        updateLotteryTimesDisplay();

        btnStartLottery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remainingLotteryTimes > 0) {
                    startLottery();
                } else {
                    Toast.makeText(getActivity(), "没有剩余抽奖次数", Toast.LENGTH_SHORT).show();
                }
            }
        });

        view.findViewById(R.id.btnMyPrizes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMyPrizes();
            }
        });

        return view;
    }

    private int calculateLotteryTimes(int totalAmountSpent) {
        if (totalAmountSpent >= 100 && totalAmountSpent < 200) {
            return 1;
        } else if (totalAmountSpent >= 200 && totalAmountSpent < 300) {
            return 2;
        } else if (totalAmountSpent >= 300) {
            return 3;
        } else {
            return 0;
        }
    }

    private void updateLotteryTimesDisplay() {
        tvBalance.setText("剩余抽奖次数: " + remainingLotteryTimes);
    }

    private void startLottery() {
        remainingLotteryTimes--;
        updateLotteryTimesDisplay();

        final int duration = random.nextInt(3001) + 2000; // Random duration between 2s to 5s
        final long startTime = System.currentTimeMillis(); // Record the start time of the animation

        final int[] currentIndex = {0};

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;

                if (elapsedTime <= duration) {
                    currentIndex[0] = (currentIndex[0] + 1) % prizeImages.length;

                    // Highlight the current imageView
                    highlightImageView(currentIndex[0]);

                    handler.postDelayed(this, 100);
                } else {
                    handler.removeCallbacks(this);
                    showPrize(currentIndex[0]);
                }
            }
        };
        handler.post(runnable);
    }


    private void highlightImageView(int index) {
        // Reset all frameLayouts to inactive state
        for (FrameLayout frameLayout : frameLayouts) {
            frameLayout.setBackgroundResource(R.drawable.border_inactive);
        }

        // Highlight the current imageView
        frameLayouts[index].setBackgroundResource(R.drawable.border_active);
    }

    private void showPrize(int index) {
        int prizeIndex = (index + prizeImages.length) % prizeImages.length;
        String prize = prizeNames[prizeIndex];
        Toast.makeText(getActivity(), "恭喜你获得: " + prize, Toast.LENGTH_LONG).show();

        // 保存奖品到数据库
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        dbHelper.savePrize(prize);
    }

    private void showMyPrizes() {
        // Display the list of prizes the user has won
        Intent intent = new Intent(getActivity(), MyPrizesActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}

package com.example.apptelefare2;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.apptelefare2.DatabaseHelper.Prize;
import java.util.List;

public class MyPrizesActivity extends AppCompatActivity {

    private ListView listViewPrizes;
    private List<Prize> prizeList;
    private PrizeAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_prizes);

        dbHelper = new DatabaseHelper(this);
        listViewPrizes = findViewById(R.id.listViewPrizes);

        loadPrizes();
    }

    private void loadPrizes() {
        prizeList = dbHelper.getPrizes();
        adapter = new PrizeAdapter();
        listViewPrizes.setAdapter(adapter);
    }

    private class PrizeAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return prizeList.size();
        }

        @Override
        public Object getItem(int position) {
            return prizeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.prize_item, parent, false);
            }

            ImageView imageView = convertView.findViewById(R.id.imageView1);
            TextView tvPrizeName = convertView.findViewById(R.id.tvPrizeName);
            Button btnUsePrize = convertView.findViewById(R.id.btnUsePrize);

            final Prize prize = prizeList.get(position);
            imageView.setImageResource(prize.getImageResourceId());
            tvPrizeName.setText(prize.getName());

            btnUsePrize.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbHelper.usePrize(prize.getId());
                    loadPrizes();
                }
            });

            return convertView;
        }
    }
}

package com.example.apptelefare2;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mcharge.db";
    private static final int DATABASE_VERSION = 3; // Incremented version

    // Table names
    public static final String TABLE_ACCOUNT_INFO = "account_info";
    public static final String TABLE_DATA_INFO = "data_info";
    public static final String TABLE_DISCOUNT_INFO = "discount_info";
    public static final String TABLE_PRIZES = "prizes"; // New table for prizes

    // Account info table columns
    public static final String COLUMN_NUMBER = "number";
    public static final String COLUMN_CALLS_MONEY = "calls_money";
    public static final String COLUMN_DATA_TYPE = "data_type";
    public static final String COLUMN_DATA_LEFT = "data_left";
    public static final String COLUMN_DISCOUNT = "discount";

    // Data info table columns
    public static final String COLUMN_DATA_NUM = "data_num";
    public static final String COLUMN_DATA_TYPE_DATA = "data_type";
    public static final String COLUMN_DATA = "data";
    public static final String COLUMN_PRICE = "price";

    // Discount info table columns
    public static final String COLUMN_DIS_NUM = "dis_num";
    public static final String COLUMN_DIS_MONEY = "dis_money";
    public static final String COLUMN_DIS_CONDITION = "dis_condition";

    // Prizes table columns
    public static final String COLUMN_PRIZE_ID = "id";
    public static final String COLUMN_PRIZE_NAME = "prize_name";
    public static final String COLUMN_IS_USED = "is_used";

    // SQL statements to create tables
    private static final String SQL_CREATE_TABLE_ACCOUNT_INFO =
            "CREATE TABLE " + TABLE_ACCOUNT_INFO + " (" +
                    COLUMN_NUMBER + " VARCHAR(100) PRIMARY KEY, " +
                    COLUMN_CALLS_MONEY + " DECIMAL(30, 2), " +
                    COLUMN_DATA_TYPE + " VARCHAR(100), " +
                    COLUMN_DATA_LEFT + " DECIMAL(30, 2), " +
                    COLUMN_DISCOUNT + " DECIMAL(30, 2)" +
                    ")";

    private static final String SQL_CREATE_TABLE_DATA_INFO =
            "CREATE TABLE " + TABLE_DATA_INFO + " (" +
                    COLUMN_DATA_NUM + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DATA_TYPE_DATA + " VARCHAR(100), " +
                    COLUMN_DATA + " DECIMAL(30, 2), " +
                    COLUMN_PRICE + " DECIMAL(30, 2)" +
                    ")";

    private static final String SQL_CREATE_TABLE_DISCOUNT_INFO =
            "CREATE TABLE " + TABLE_DISCOUNT_INFO + " (" +
                    COLUMN_DIS_NUM + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DIS_MONEY + " DECIMAL(30, 2), " +
                    COLUMN_DIS_CONDITION + " DECIMAL(30, 2)" +
                    ")";

    private static final String SQL_CREATE_TABLE_PRIZES =
            "CREATE TABLE " + TABLE_PRIZES + " (" +
                    COLUMN_PRIZE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PRIZE_NAME + " TEXT, " +
                    COLUMN_IS_USED + " INTEGER DEFAULT 0" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_ACCOUNT_INFO);
        db.execSQL(SQL_CREATE_TABLE_DATA_INFO);
        db.execSQL(SQL_CREATE_TABLE_DISCOUNT_INFO);
        db.execSQL(SQL_CREATE_TABLE_PRIZES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(SQL_CREATE_TABLE_PRIZES);
        }
    }

    public void savePrize(String prizeName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRIZE_NAME, prizeName);
        db.insert(TABLE_PRIZES, null, values);
    }

    public List<Prize> getPrizes() {
        List<Prize> prizes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRIZES, null, COLUMN_IS_USED + " = 0", null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range")
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_PRIZE_ID));
                @SuppressLint("Range")
                String prizeName = cursor.getString(cursor.getColumnIndex(COLUMN_PRIZE_NAME));
                int imageResourceId = getPrizeImageResourceId(prizeName);
                prizes.add(new Prize(id, prizeName, imageResourceId));
            }
            cursor.close();
        }
        return prizes;
    }

    private int getPrizeImageResourceId(String prizeName) {
        switch (prizeName) {
            case "100MB流量":
                return R.drawable.prize100mb;
            case "5元话费":
                return R.drawable.prize5yuan;
            case "1GB流量":
                return R.drawable.prize1gb;
            case "10元话费":
                return R.drawable.prize10yuan;
            case "300MB流量":
                return R.drawable.prize300mb;
            case "500MB流量":
                return R.drawable.prize500mb;
            case "20元话费":
                return R.drawable.prize20yuan;
            default:
                return R.drawable.defaultimg;
        }
    }


    public void usePrize(int prizeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_USED, 1);
        db.update(TABLE_PRIZES, values, COLUMN_PRIZE_ID + " = ?", new String[]{String.valueOf(prizeId)});
    }

    public Cursor getUserProfile(String number) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_ACCOUNT_INFO + " WHERE " + COLUMN_NUMBER + " = ?";
        return db.rawQuery(query, new String[]{number});
    }

    public class Prize {
        private int id;
        private String name;
        private int imageResourceId;

        public Prize(int id, String name, int imageResourceId) {
            this.id = id;
            this.name = name;
            this.imageResourceId = imageResourceId;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getImageResourceId() {
            return imageResourceId;
        }
    }
}

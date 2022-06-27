package com.shop.kakebe.KaKebe.localDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.shop.kakebe.KaKebe.Models.FoodDBModel;

import java.util.ArrayList;
import java.util.List;

public class SenseDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "kakebe";
    private static final int DB_VERSION = 7;
    private static final String TABLE_NAME = "CART";
    private static final String TABLE_ADDRESS = "ADDRESS";
    public static final String COLUMN_id = "_id";
    public static final String COLUMN_menuId = "menuId";
    public static final String COLUMN_menuName = "menuName";
    public static final String COLUMN_price = "price";
    public static final String COLUMN_menuTypeId = "menuTypeId";
    public static final String COLUMN_menuImage = "menuImage";
    public static final String COLUMN_quantity = "quantity";
    public static final String COLUMN_order_status = "order_status";


    public static final String COLUMN_ADDRESS_id = "addressId";


    List<FoodDBModel> foodDBModelList;

    public SenseDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        //create CART table
        String sql = "CREATE TABLE CART (" +
                COLUMN_id + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_menuId + " INTEGER," +
                COLUMN_menuName + " TEXT," +
                COLUMN_price + " INTEGER," +
                COLUMN_menuTypeId + " INTEGER," +
                COLUMN_menuImage + " TEXT," +
                COLUMN_quantity + " INTEGER DEFAULT 1," +
                COLUMN_order_status + " TINYINT )";

        db.execSQL(sql);

        //create address table
        String address_sql = "CREATE TABLE ADDRESS (" + COLUMN_id + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_ADDRESS_id + " INTEGER )";
        db.execSQL(address_sql);
    }

    //upgrading the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }


    public List<FoodDBModel> listTweetsBD() {
        String sql = "select * from " + TABLE_NAME + " order by " + COLUMN_id + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        foodDBModelList = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(0));
                int menuId = Integer.parseInt(cursor.getString(1));
                String menuname = cursor.getString(2);
                int price = Integer.parseInt(cursor.getString(3));
                int menutypid = Integer.parseInt(cursor.getString(4));
                String menuimage = cursor.getString(5);
                int quantity = Integer.parseInt(cursor.getString(6));
                int order_status = Integer.parseInt(cursor.getString(7));

                foodDBModelList.add(new FoodDBModel(menuId, menuname, price, menutypid, menuimage, quantity, order_status));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return foodDBModelList;
    }

    public void addTweet(
            int menuId,
            String menuname,
            int price,
            int menutypeid,
            String menuImage,
            int quantity,
            int order_status
    ) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_menuId, menuId);
        values.put(COLUMN_menuName, menuname);
        values.put(COLUMN_price, price);
        values.put(COLUMN_menuTypeId, menutypeid);
        values.put(COLUMN_menuImage, menuImage);
        values.put(COLUMN_quantity, quantity);
        values.put(COLUMN_order_status, order_status);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
        db.close();
    }


    public void updateMenuCount(Integer qtn, Integer menuID) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_quantity, qtn);
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(TABLE_NAME, values, COLUMN_menuId + " = ?", new String[]{String.valueOf(menuID)});
        db.close();
    }


    public void deleteTweet(String id_str) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_menuId + " = ?", new String[]{id_str});
        db.close();
    }

    public void clearCart() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }


    public boolean checktweetindb(String id_str) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_menuId, COLUMN_menuName},
                COLUMN_menuId + " = ?",
                new String[]{id_str},
                null, null, null, null);
        if (cursor.moveToFirst()) {
            //recordexist
            cursor.close();
            db.close();
            return false;
        } else {
            //record not existing
            cursor.close();
            db.close();

            return true;
        }
    }

    public int countCart() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCount = db.rawQuery("select count(*) from " + TABLE_NAME, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();

        db.close();

        return count;
    }


    public int getMenuQtn(String id_str) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_quantity},
                COLUMN_menuId + " = ?",
                new String[]{id_str},
                null, null, null, null);
        if (cursor.moveToFirst()) {
            //recordexist
            int count = cursor.getInt(0);
            cursor.close();
            db.close();
            return count;
        } else {
            //record not existing
            int count = 1;
            cursor.close();
            db.close();
            return count;
        }
    }

    public int sumPriceCartItems() {
        int result = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select sum(" + COLUMN_price + " * " + COLUMN_quantity + ") from " + TABLE_NAME, null);
        if (cursor.moveToFirst()) result = cursor.getInt(0);
        cursor.close();
        db.close();
        return result;
    }


    public Cursor getUnsyncedMenuItem() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_order_status + " = 0";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    //    Get address
    public void clearAddress() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ADDRESS, null, null);
        db.close();
    }

    public void deleteAddress(String id_str) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ADDRESS, COLUMN_ADDRESS_id + " = ?", new String[]{id_str});
        db.close();
    }

    public void addADDress(
            int addressID
    ) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ADDRESS_id, addressID);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_ADDRESS, null, values);
        db.close();
    }

    public boolean checkAddressinDB(String id_str) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ADDRESS,
                new String[]{COLUMN_ADDRESS_id},
                COLUMN_ADDRESS_id + " = ?",
                new String[]{id_str},
                null, null, null, null);
        if (cursor.moveToFirst()) {
            //recordexist
            cursor.close();
            db.close();
            return false;
        } else {
            //record not existing
            cursor.close();
            db.close();

            return true;
        }
    }


}

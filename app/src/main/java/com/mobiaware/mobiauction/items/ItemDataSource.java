/*
 * Copyright (c) 2010 mobiaware.com.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.mobiaware.mobiauction.items;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class ItemDataSource {
    private SQLiteDatabase _database;
    private ItemSQLiteHelper _databaseHelper;

    private String[] allColumns = {ItemSQLiteHelper.COLUMN_ID, ItemSQLiteHelper.COLUMN_AUCTION,
            ItemSQLiteHelper.COLUMN_NUMBER, ItemSQLiteHelper.COLUMN_NAME,
            ItemSQLiteHelper.COLUMN_DESCRIPTION, ItemSQLiteHelper.COLUMN_CATEGORY,
            ItemSQLiteHelper.COLUMN_SELLER, ItemSQLiteHelper.COLUMN_VALPRICE,
            ItemSQLiteHelper.COLUMN_MINPRICE, ItemSQLiteHelper.COLUMN_INCPRICE,
            ItemSQLiteHelper.COLUMN_CURPRICE, ItemSQLiteHelper.COLUMN_WINNER,
            ItemSQLiteHelper.COLUMN_BIDCOUNT, ItemSQLiteHelper.COLUMN_WATCHCOUNT,
            ItemSQLiteHelper.COLUMN_URL, ItemSQLiteHelper.COLUMN_MULTI};

    public ItemDataSource(Context context) {
        _databaseHelper = new ItemSQLiteHelper(context);
    }

    public void open() throws SQLException {
        _database = _databaseHelper.getWritableDatabase();
    }

    public void close() {
        _databaseHelper.close();
    }

    public Item createItem(long auction, String number, String name, String description,
                           String category, String seller, double valPrice, double minPrice, double incPrice,
                           double curPrice, String winner, long bidCount, long watchCount, String url, boolean multi) {
        ContentValues values = new ContentValues();
        values.put(ItemSQLiteHelper.COLUMN_AUCTION, auction);
        values.put(ItemSQLiteHelper.COLUMN_NUMBER, number);
        values.put(ItemSQLiteHelper.COLUMN_NAME, name);
        values.put(ItemSQLiteHelper.COLUMN_DESCRIPTION, description);
        values.put(ItemSQLiteHelper.COLUMN_CATEGORY, category);
        values.put(ItemSQLiteHelper.COLUMN_SELLER, seller);
        values.put(ItemSQLiteHelper.COLUMN_VALPRICE, valPrice);
        values.put(ItemSQLiteHelper.COLUMN_MINPRICE, minPrice);
        values.put(ItemSQLiteHelper.COLUMN_INCPRICE, incPrice);
        values.put(ItemSQLiteHelper.COLUMN_CURPRICE, curPrice);
        values.put(ItemSQLiteHelper.COLUMN_WINNER, winner);
        values.put(ItemSQLiteHelper.COLUMN_BIDCOUNT, bidCount);
        values.put(ItemSQLiteHelper.COLUMN_WATCHCOUNT, watchCount);
        values.put(ItemSQLiteHelper.COLUMN_URL, url);
        values.put(ItemSQLiteHelper.COLUMN_MULTI, multi);

        long insertId = _database.insert(ItemSQLiteHelper.TABLE_ITEMS, null, values);

        return getItem(insertId);
    }

    public Item getItem(long id) {
        Cursor cursor = null;
        try {
            cursor =
                    _database.query(ItemSQLiteHelper.TABLE_ITEMS, allColumns, ItemSQLiteHelper.COLUMN_ID
                                    + " = " + id, null, ItemSQLiteHelper.COLUMN_NUMBER, null,
                            ItemSQLiteHelper.COLUMN_NUMBER);

            if (cursor == null || cursor.getCount() == 0) {
                return null; // < 1 means no item
            }

            cursor.moveToFirst();
            return cursorToItem(cursor);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public ArrayList<Item> getItems() {
        ArrayList<Item> items = new ArrayList<Item>();

        Cursor cursor = null;
        try {
            cursor =
                    _database.query(ItemSQLiteHelper.TABLE_ITEMS, allColumns, null, null,
                            ItemSQLiteHelper.COLUMN_NUMBER, null, ItemSQLiteHelper.COLUMN_NUMBER);

            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    items.add(cursorToItem(cursor));
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return items;
    }

    private Item cursorToItem(Cursor cursor) {
        return new Item(cursor.getLong(0), cursor.getLong(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getDouble(7),
                cursor.getDouble(8), cursor.getDouble(9), cursor.getDouble(10), cursor.getString(11),
                cursor.getLong(12), cursor.getLong(13), cursor.getString(14), cursor.getLong(15) != 0);
    }
}

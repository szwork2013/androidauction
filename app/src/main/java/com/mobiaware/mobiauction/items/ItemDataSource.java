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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;

public class ItemDataSource {
    private static String[] ALL_COLUMNS = {ItemSQLiteHelper.COLUMN_UID + " AS " + BaseColumns._ID,
            ItemSQLiteHelper.COLUMN_NUMBER, ItemSQLiteHelper.COLUMN_NAME,
            ItemSQLiteHelper.COLUMN_DESCRIPTION, ItemSQLiteHelper.COLUMN_CATEGORY,
            ItemSQLiteHelper.COLUMN_SELLER, ItemSQLiteHelper.COLUMN_VALPRICE,
            ItemSQLiteHelper.COLUMN_MINPRICE, ItemSQLiteHelper.COLUMN_INCPRICE,
            ItemSQLiteHelper.COLUMN_CURPRICE, ItemSQLiteHelper.COLUMN_WINNER,
            ItemSQLiteHelper.COLUMN_BIDCOUNT, ItemSQLiteHelper.COLUMN_WATCHCOUNT,
            ItemSQLiteHelper.COLUMN_URL, ItemSQLiteHelper.COLUMN_MULTI,
            ItemSQLiteHelper.COLUMN_ISBIDDING, ItemSQLiteHelper.COLUMN_ISWATCHING};

    private ContentResolver _contentResolver;

    public ItemDataSource(Context context) {
        _contentResolver = context.getContentResolver();
    }

    public void createItem(long uid, String number, String name, String description, String category,
                           String seller, double valPrice, double minPrice, double incPrice, double curPrice,
                           String winner, long bidCount, long watchCount, String url, boolean multi) {
        ContentValues values = new ContentValues();
        values.put(ItemSQLiteHelper.COLUMN_UID, uid);
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

        Uri uri = _contentResolver.insert(ItemContentProvider.CONTENT_URI, values);
    }

    public long setIsBidding(long uid) {
        ContentValues values = new ContentValues();
        values.put(ItemSQLiteHelper.COLUMN_ISBIDDING, 1);

        return _contentResolver.update(ItemContentProvider.CONTENT_URI, values,
                ItemSQLiteHelper.COLUMN_UID + "=?", new String[] {Long.toString(uid)});
    }

    public long setIsWatching(long uid) {
        ContentValues values = new ContentValues();
        values.put(ItemSQLiteHelper.COLUMN_ISWATCHING, 1);

        return _contentResolver.update(ItemContentProvider.CONTENT_URI, values,
                ItemSQLiteHelper.COLUMN_UID + "=?", new String[] {Long.toString(uid)});
    }


    public ArrayList<Item> getItems() {
        ArrayList<Item> items = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor =
                    _contentResolver.query(ItemContentProvider.CONTENT_URI, ALL_COLUMNS, null, null,
                            ItemSQLiteHelper.COLUMN_NUMBER);


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

    public ArrayList<Item> getMyItems() {
        ArrayList<Item> items = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor =
                    _contentResolver.query(ItemContentProvider.CONTENT_URI, ALL_COLUMNS,
                            ItemSQLiteHelper.COLUMN_ISBIDDING + "=1 OR " + ItemSQLiteHelper.COLUMN_ISWATCHING
                                    + "=1", null, ItemSQLiteHelper.COLUMN_NUMBER);

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

    public ArrayList<Item> getLowBidItems() {
        ArrayList<Item> items = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor =
                    _contentResolver.query(ItemContentProvider.CONTENT_URI, ALL_COLUMNS,
                            ItemSQLiteHelper.COLUMN_BIDCOUNT + "<=2", null, ItemSQLiteHelper.COLUMN_BIDCOUNT);

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

    public ArrayList<Item> getSearchItems(String s) {
        ArrayList<Item> items = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor =
                    _contentResolver.query(ItemContentProvider.CONTENT_URI, ALL_COLUMNS,
                            ItemSQLiteHelper.COLUMN_NUMBER + " LIKE '%" + s + "%' OR "
                                    + ItemSQLiteHelper.COLUMN_NAME + " LIKE '%" + s + "%' OR "
                                    + ItemSQLiteHelper.COLUMN_DESCRIPTION + " LIKE '%" + s + "%'", null,
                            ItemSQLiteHelper.COLUMN_NUMBER);

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
        return new Item(cursor.getLong(0), cursor.getString(1), cursor.getString(2),
                cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getDouble(6),
                cursor.getDouble(7), cursor.getDouble(8), cursor.getDouble(9), cursor.getString(10),
                cursor.getLong(11), cursor.getLong(12), cursor.getString(13), cursor.getLong(14) != 0,
                cursor.getLong(15) != 0, cursor.getLong(16) != 0);
    }
}

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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ItemSQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_ITEMS = "items";

    public static final String COLUMN_UID = "uid";
    public static final String COLUMN_NUMBER = "number";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_SELLER = "seller";
    public static final String COLUMN_VALPRICE = "valprice";
    public static final String COLUMN_MINPRICE = "minprice";
    public static final String COLUMN_INCPRICE = "incprice";
    public static final String COLUMN_CURPRICE = "curprice";
    public static final String COLUMN_WINNER = "winner";
    public static final String COLUMN_BIDCOUNT = "bidcount";
    public static final String COLUMN_WATCHCOUNT = "watchcount";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_MULTI = "multi";
    public static final String COLUMN_ISBIDDING = "isbidding";
    public static final String COLUMN_ISWATCHING = "iswatching";

    private static final String DATABASE_NAME = "items.db";
    private static final int DATABASE_VERSION = 1;

    // NOTE: uid comes from the liveauction service and is used as the primary key
    private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_ITEMS + "(" + COLUMN_UID
            + " INTEGER PRIMARY KEY," + COLUMN_NUMBER + " TEXT UNIQUE NOT NULL, " + COLUMN_NAME
            + " TEXT, " + COLUMN_DESCRIPTION + " TEXT, " + COLUMN_CATEGORY + " TEXT, " + COLUMN_SELLER
            + " TEXT, " + COLUMN_VALPRICE + " REAL, " + COLUMN_MINPRICE + " REAL, " + COLUMN_INCPRICE
            + " REAL, " + COLUMN_CURPRICE + " REAL, " + COLUMN_WINNER + " TEXT, " + COLUMN_BIDCOUNT
            + " INTEGER, " + COLUMN_WATCHCOUNT + " INTEGER, " + COLUMN_URL + " TEXT, " + COLUMN_MULTI
            + " INTEGER," + COLUMN_ISBIDDING + " INTEGER," + COLUMN_ISWATCHING + " INTEGER" + ");";

    public ItemSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ItemSQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion
                + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }
}

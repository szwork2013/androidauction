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

package com.mobiaware.mobiauction.funds;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class FundsSQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_FUNDS = "funds";

    public static final String COLUMN_AUCTION = "auction";
    public static final String COLUMN_VALUE = "value";
    public static final String COLUMN_NAME = "name";

    private static final String DATABASE_NAME = "funds.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_FUNDS + "("
            + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_AUCTION + " INTEGER,"
            + COLUMN_VALUE + " REAL, " + COLUMN_NAME + " TEXT NOT NULL" + ");";

    public FundsSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(FundsSQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion
                + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FUNDS);
        onCreate(db);
    }
}

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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.mobiaware.mobiauction.utils.CloseUtils;

public class FundDataSource {
    private static final String[] ALL_COLUMNS = {FundsSQLiteHelper.COLUMN_AUCTION,
            FundsSQLiteHelper.COLUMN_VALUE, FundsSQLiteHelper.COLUMN_NAME};

    private SQLiteDatabase _database;
    private final FundsSQLiteHelper _databaseHelper;

    public FundDataSource(Context context) {
        _databaseHelper = new FundsSQLiteHelper(context);
    }

    public void open() throws SQLException {
        _database = _databaseHelper.getWritableDatabase();
    }

    public void close() {
        _databaseHelper.close();
    }

    public Fund setFund(Fund fund) {
        ContentValues values = new ContentValues();
        values.put(FundsSQLiteHelper.COLUMN_AUCTION, fund.getAuction());
        values.put(FundsSQLiteHelper.COLUMN_VALUE, fund.getValue());
        values.put(FundsSQLiteHelper.COLUMN_NAME, fund.getName());

        long insertId = _database.replace(FundsSQLiteHelper.TABLE_FUNDS, null, values);

        if (insertId < 0) {
            return new Fund(1, 0.0, ""); // no fund
        }

        return fund;
    }

    public Fund getFund() {
        Cursor cursor = null;
        try {
            cursor =
                    _database.query(FundsSQLiteHelper.TABLE_FUNDS, ALL_COLUMNS, null, null, null, null, null);

            if (cursor == null || cursor.getCount() == 0) {
                return new Fund(1, 0.0, ""); // no fund
            }

            cursor.moveToLast();
            return cursorToFund(cursor);
        } finally {
            CloseUtils.closeQuietly(cursor);
        }
    }

    private Fund cursorToFund(Cursor cursor) {
        return new Fund(cursor.getLong(0), cursor.getDouble(1), cursor.getString(2));
    }
}

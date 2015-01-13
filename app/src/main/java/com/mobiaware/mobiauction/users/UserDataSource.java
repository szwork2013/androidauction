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

package com.mobiaware.mobiauction.users;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class UserDataSource {
    private SQLiteDatabase _database;
    private UsersSQLiteHelper _databaseHelper;

    private String[] allColumns = {BaseColumns._ID, UsersSQLiteHelper.COLUMN_AUCTION,
            UsersSQLiteHelper.COLUMN_BIDDER, UsersSQLiteHelper.COLUMN_PASSWORD,
            UsersSQLiteHelper.COLUMN_FIRSTNAME, UsersSQLiteHelper.COLUMN_LASTNAME};


    public UserDataSource(Context context) {
        _databaseHelper = new UsersSQLiteHelper(context);
    }

    public void open() throws SQLException {
        _database = _databaseHelper.getWritableDatabase();
    }

    public void close() {
        _databaseHelper.close();
    }

    public User createUser(long auction, String bidder, String password, String firstName,
                           String lastName) {
        ContentValues values = new ContentValues();
        values.put(UsersSQLiteHelper.COLUMN_AUCTION, auction);
        values.put(UsersSQLiteHelper.COLUMN_BIDDER, bidder);
        values.put(UsersSQLiteHelper.COLUMN_PASSWORD, password);
        values.put(UsersSQLiteHelper.COLUMN_FIRSTNAME, firstName);
        values.put(UsersSQLiteHelper.COLUMN_LASTNAME, lastName);

        long insertId = _database.insert(UsersSQLiteHelper.TABLE_USERS, null, values);

        return getUser(insertId);
    }

    public User getUser(long id) {
        Cursor cursor = null;
        try {
            cursor =
                    _database.query(UsersSQLiteHelper.TABLE_USERS, allColumns, BaseColumns._ID + "=" + id,
                            null, null, null, null);

            if (cursor == null || cursor.getCount() == 0) {
                return null; // < 1 means no login
            }

            cursor.moveToLast(); // use last entry for login
            return cursorToUser(cursor);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private User cursorToUser(Cursor cursor) {
        return new User(cursor.getLong(0), cursor.getLong(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.getString(5));
    }
}

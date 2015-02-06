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

import com.mobiaware.mobiauction.utils.CloseUtils;

public class UserDataSource {
    private static final String[] ALL_COLUMNS = {UsersSQLiteHelper.COLUMN_UID + " AS " + BaseColumns._ID,
            UsersSQLiteHelper.COLUMN_AUCTION, UsersSQLiteHelper.COLUMN_BIDDER,
            UsersSQLiteHelper.COLUMN_PASSWORD, UsersSQLiteHelper.COLUMN_FIRSTNAME,
            UsersSQLiteHelper.COLUMN_LASTNAME};

    private SQLiteDatabase _database;
    private final UsersSQLiteHelper _databaseHelper;

    public UserDataSource(Context context) {
        _databaseHelper = new UsersSQLiteHelper(context);
    }

    public void open() throws SQLException {
        _database = _databaseHelper.getWritableDatabase();
    }

    public void close() {
        _databaseHelper.close();
    }

    public User setUser(User user) {
        ContentValues values = new ContentValues();
        values.put(UsersSQLiteHelper.COLUMN_UID, user.getUid());
        values.put(UsersSQLiteHelper.COLUMN_AUCTION, user.getAuction());
        values.put(UsersSQLiteHelper.COLUMN_BIDDER, user.getBidder());
        values.put(UsersSQLiteHelper.COLUMN_PASSWORD, user.getPassword());
        values.put(UsersSQLiteHelper.COLUMN_FIRSTNAME, user.getFirstName());
        values.put(UsersSQLiteHelper.COLUMN_LASTNAME, user.getLastName());

        long insertId = _database.replace(UsersSQLiteHelper.TABLE_USERS, null, values);

        if (insertId < 0) {
            return null;
        }

        return user;
    }

    public User getUser() {
        Cursor cursor = null;
        try {
            cursor =
                    _database.query(UsersSQLiteHelper.TABLE_USERS, ALL_COLUMNS, null, null, null, null, null);

            if (cursor == null || cursor.getCount() == 0) {
                return null; // < 1 means no login
            }

            cursor.moveToLast(); // use last entry for login
            return cursorToUser(cursor);
        } finally {
            CloseUtils.closeQuietly(cursor);
        }
    }

    private User cursorToUser(Cursor cursor) {
        return new User(cursor.getLong(0), cursor.getLong(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.getString(5));
    }
}

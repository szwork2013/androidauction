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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class ItemContentProvider extends ContentProvider {
    static final String AUTHORITY = "com.mobiaware.mobiauction.items.ItemContentProvider";
    static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
            + ItemSQLiteHelper.TABLE_ITEMS);

    public static final int ITEMS = 1;
    public static final int ITEMS_ID = 2;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, ItemSQLiteHelper.TABLE_ITEMS, ITEMS);
        URI_MATCHER.addURI(AUTHORITY, ItemSQLiteHelper.TABLE_ITEMS + "/#", ITEMS_ID);
    }

    private SQLiteDatabase _database;
    private ItemSQLiteHelper _databaseHelper;

    public ItemContentProvider() {

    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        _databaseHelper = new ItemSQLiteHelper(context);

        _database = _databaseHelper.getWritableDatabase();
        return (_database != null);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(ItemSQLiteHelper.TABLE_ITEMS);

        int uriType = URI_MATCHER.match(uri);
        switch (uriType) {
            case ITEMS_ID:
                queryBuilder.appendWhere(ItemSQLiteHelper.COLUMN_UID + "=" + uri.getLastPathSegment());
                break;
            case ITEMS:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor cursor =
                queryBuilder.query(_databaseHelper.getReadableDatabase(), projection, selection,
                        selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowId = 0;

        int uriType = URI_MATCHER.match(uri);
        switch (uriType) {
            case ITEMS:
                rowId = _database.insert(ItemSQLiteHelper.TABLE_ITEMS, "", values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (rowId > 0) {
            Uri tmp = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(tmp, null);
            return tmp;
        }

        throw new SQLException("Failed to add a record:" + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted = 0;

        int uriType = URI_MATCHER.match(uri);
        switch (uriType) {
            case ITEMS:
                rowsDeleted = _database.delete(ItemSQLiteHelper.TABLE_ITEMS, selection, selectionArgs);
                break;

            case ITEMS_ID:
                String id = uri.getLastPathSegment();
                _database.delete(ItemSQLiteHelper.TABLE_ITEMS, ItemSQLiteHelper.COLUMN_UID + " = " + id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated = 0;

        int uriType = URI_MATCHER.match(uri);
        switch (uriType) {
            case ITEMS:
                rowsUpdated =
                        _database.update(ItemSQLiteHelper.TABLE_ITEMS, values, selection, selectionArgs);
                break;
            case ITEMS_ID:
                String id = uri.getLastPathSegment();
                _database
                        .update(ItemSQLiteHelper.TABLE_ITEMS, values, ItemSQLiteHelper.COLUMN_UID + " = " + id
                                + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }
}

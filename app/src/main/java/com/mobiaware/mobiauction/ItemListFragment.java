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

package com.mobiaware.mobiauction;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.mobiaware.mobiauction.items.ItemContentProvider;
import com.mobiaware.mobiauction.items.ItemDataSource;
import com.mobiaware.mobiauction.items.ItemSQLiteHelper;

public class ItemListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARG_TYPE = "type";

    private AbsListView _listView;

    private ItemListItemsAdapter _adapter;

    private LoaderManager _loaderManager;
    private CursorLoader _cursorLoader;

    public static ItemListFragment newInstance(int type) {
        ItemListFragment fragment = new ItemListFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int type = 0;
        if (getArguments() != null) {
            type = getArguments().getInt(ARG_TYPE);
        }

        _adapter = new ItemListItemsAdapter(getActivity(), null);

        _loaderManager = getLoaderManager();
        _loaderManager.initLoader(type, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        _listView = (AbsListView) view.findViewById(android.R.id.list);
        _listView.setEmptyView(view.findViewById(android.R.id.empty));
        _listView.setAdapter(_adapter);

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case 1:
                _cursorLoader =
                        new CursorLoader(getActivity(), ItemContentProvider.CONTENT_URI,
                                ItemDataSource.ALL_COLUMNS, ItemSQLiteHelper.COLUMN_ISBIDDING + "=1 OR "
                                + ItemSQLiteHelper.COLUMN_ISWATCHING + "=1", null,
                                ItemSQLiteHelper.COLUMN_NUMBER);
                break;
            case 2:
                _cursorLoader =
                        new CursorLoader(getActivity(), ItemContentProvider.CONTENT_URI,
                                ItemDataSource.ALL_COLUMNS, ItemSQLiteHelper.COLUMN_BIDCOUNT + "<=2", null,
                                ItemSQLiteHelper.COLUMN_BIDCOUNT);
                break;

            default:
                _cursorLoader =
                        new CursorLoader(getActivity(), ItemContentProvider.CONTENT_URI,
                                ItemDataSource.ALL_COLUMNS, null, null, null);
        }
        return _cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (_adapter != null && cursor != null) {
            _adapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (_adapter != null) {
            _adapter.swapCursor(null);
        }
    }
}

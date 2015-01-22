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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Toast;

import com.mobiaware.mobiauction.api.RESTClient;
import com.mobiaware.mobiauction.items.ItemContentProvider;
import com.mobiaware.mobiauction.items.ItemDataSource;
import com.mobiaware.mobiauction.items.ItemSQLiteHelper;
import com.mobiaware.mobiauction.users.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ItemListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ItemListFragment.class.getName();

    private static final String ARG_TYPE = "type";
    private static final String ARG_FILTER = "filter";

    private AbsListView _listView;
    private SwipeRefreshLayout _swipeContainer;

    private ItemListItemsAdapter _adapter;

    private LoaderManager _loaderManager;
    private CursorLoader _cursorLoader;

    private GetItemsTask _getItemsTask;

    private ItemDataSource _datasource;

    private int _type;
    private String _filter;

    public static ItemListFragment newInstance(int type, String filter) {
        ItemListFragment fragment = new ItemListFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        args.putString(ARG_FILTER, filter);

        fragment.setArguments(args);

        return fragment;
    }

    public ItemListFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            _type = getArguments().getInt(ARG_TYPE, 0);
            _filter = getArguments().getString(ARG_FILTER, null);
        }

        _adapter = new ItemListItemsAdapter(getActivity(), null);

        _loaderManager = getLoaderManager();
        _loaderManager.initLoader(_type, null, this);

        _datasource = new ItemDataSource(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        _listView = (AbsListView) view.findViewById(android.R.id.list);
        _listView.setEmptyView(view.findViewById(android.R.id.empty));
        _listView.setAdapter(_adapter);

        _swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipecontainer);
        _swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });

        _listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                boolean enable = false;
                if (_listView != null && _listView.getChildCount() > 0) {
                    boolean firstItemVisible = _listView.getFirstVisiblePosition() == 0;
                    boolean topOfFirstItemVisible = _listView.getChildAt(0).getTop() == 0;
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                _swipeContainer.setEnabled(enable);
            }
        });

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case 1:
                User user = ((AuctionApplication) getActivity().getApplicationContext()).getActiveUser();

                _cursorLoader =
                        new CursorLoader(getActivity(), ItemContentProvider.CONTENT_URI,
                                ItemDataSource.ALL_COLUMNS, ItemSQLiteHelper.COLUMN_ISBIDDING + "=1 or "
                                + ItemSQLiteHelper.COLUMN_ISWATCHING + "=1", null, "CASE WHEN "
                                + ItemSQLiteHelper.COLUMN_WINNER + "!=" + user.getBidder()
                                + " THEN 0 ELSE 1 END");
                break;
            case 2:
                _cursorLoader =
                        new CursorLoader(getActivity(), ItemContentProvider.CONTENT_URI,
                                ItemDataSource.ALL_COLUMNS, ItemSQLiteHelper.COLUMN_BIDCOUNT + "<=2", null,
                                ItemSQLiteHelper.COLUMN_BIDCOUNT);
                break;
            case 11:
                _cursorLoader =
                        new CursorLoader(getActivity(), ItemContentProvider.CONTENT_URI,
                                ItemDataSource.ALL_COLUMNS, ItemSQLiteHelper.COLUMN_NUMBER + " like '%" + _filter
                                + "%' or " + ItemSQLiteHelper.COLUMN_NAME + " like '%" + _filter + "%' or "
                                + ItemSQLiteHelper.COLUMN_DESCRIPTION + " like '%" + _filter + "%'", null,
                                ItemSQLiteHelper.COLUMN_NUMBER);
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


    private void refreshItems() {
        if (_getItemsTask != null) {
            return;
        }

        User user = ((AuctionApplication) getActivity().getApplicationContext()).getActiveUser();

        _getItemsTask = new GetItemsTask(user);
        _getItemsTask.execute();
    }

    public class GetItemsTask extends AsyncTask<String, Void, Boolean> {
        private final User _user;

        public GetItemsTask(User user) {
            _user = user;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                fetchItems();
                fetchBids(_user.getBidder(), _user.getPassword());
                fetchWatches(_user.getBidder(), _user.getPassword());
            } catch (IOException e) {
                Log.e(TAG, "Error fetching auction items.", e);
                return false;
            } catch (JSONException e) {
                Log.e(TAG, "Error fetching auction items.", e);
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            _getItemsTask = null;
            _swipeContainer.setRefreshing(false);

            if (success) {
                Toast.makeText(getActivity(), getString(R.string.refresh_success), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.refresh_failed), Toast.LENGTH_SHORT).show();
            }
        }

        private void fetchItems() throws IOException, JSONException {
            String response = RESTClient.get("/event/auctions/1/items", null);

            JSONArray array = new JSONArray(response);

            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);

                _datasource.createItem(object.getLong("uid"), object.getString("itemNumber"),
                        object.getString("name"), object.getString("description"),
                        object.getString("category"), object.getString("seller"), object.getDouble("valPrice"),
                        object.getDouble("minPrice"), object.getDouble("incPrice"),
                        object.getDouble("curPrice"), object.optString("winner", ""),
                        object.optLong("bidCount", 0), object.optLong("watchCount", 0),
                        object.optString("url", ""), object.getBoolean("multi"));
            }
        }

        private void fetchBids(String bidder, String password) throws IOException, JSONException {
            String response = RESTClient.get("/live/bids", bidder, password);

            JSONArray array = new JSONArray(response);

            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                _datasource.setIsBidding(object.getLong("uid"));
            }
        }

        private void fetchWatches(String bidder, String password) throws IOException, JSONException {
            String response = RESTClient.get("/live/watches", bidder, password);

            JSONArray array = new JSONArray(response);

            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                _datasource.setIsWatching(object.getLong("uid"));
            }
        }
    }
}

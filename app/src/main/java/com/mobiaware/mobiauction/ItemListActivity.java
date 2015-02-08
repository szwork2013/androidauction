package com.mobiaware.mobiauction;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.SearchView;
import android.widget.Toast;

import com.mobiaware.mobiauction.api.RESTClient;
import com.mobiaware.mobiauction.api.WSClient;
import com.mobiaware.mobiauction.items.ItemContentProvider;
import com.mobiaware.mobiauction.items.ItemDataSource;
import com.mobiaware.mobiauction.items.ItemSQLiteHelper;
import com.mobiaware.mobiauction.users.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ItemListActivity extends Activity implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor>,
        WSClient.OnMessageListener {
    private static final String TAG = ItemListActivity.class.getName();

    private static final String ARG_TYPE = "type";
    private static final String ARG_FILTER = "filter";

    public static final int TYPE_ALL = 100;
    public static final int TYPE_MYITEMS = 200;
    public static final int TYPE_LOWBIDS = 300;
    public static final int TYPE_SEARCH = 400;


    private SwipeRefreshLayout _swipeContainer;

    private int _type;
    private String _filter;

    private ItemDataSource _datasource;
    private ItemListItemsAdapter _adapter;

    private LoaderManager _loaderManager;
    private CursorLoader _cursorLoader;

    private GetItemsTask _getItemsTask;

    private WSClient _webSocket;

    public static Intent newInstance(Context context, int type, String filter) {
        Intent intent = new Intent(context, ItemListActivity.class);
        intent.putExtra(ARG_TYPE, type);
        intent.putExtra(ARG_FILTER, filter);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _type = getIntent().getIntExtra(ARG_TYPE, 0);
        _filter = getIntent().getStringExtra(ARG_FILTER);

        _datasource = new ItemDataSource(this);
        _adapter = new ItemListItemsAdapter(this, null);

        _loaderManager = getLoaderManager();
        _loaderManager.initLoader(_type, null, this);

        _webSocket = new WSClient(this, this);

        setContentView(R.layout.activity_item_list);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        _swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipecontainer);
        _swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });

        final AbsListView listView = (AbsListView) findViewById(android.R.id.list);
        listView.setEmptyView(findViewById(android.R.id.empty));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = _adapter.getCursor();
                cursor.moveToPosition(position);
                startActivity(BidActivity.newInstance(getApplicationContext(),
                        ItemDataSource.cursorToItem(cursor)));
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                boolean enable = false;
                if (listView != null && listView.getChildCount() > 0) {
                    boolean firstItemVisible = listView.getFirstVisiblePosition() == 0;
                    boolean topOfFirstItemVisible = listView.getChildAt(0).getTop() == 0;
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                _swipeContainer.setEnabled(enable);
            }
        });

        listView.setAdapter(_adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _webSocket.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        _webSocket.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _webSocket.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_list, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(true);
        searchView.setIconified(true);
        searchView.setFocusable(false);
        searchView.setFocusableInTouchMode(true);
        searchView.clearFocus();
        searchView.setQueryHint(getString(R.string.search_hint));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshItems();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemMessageReceived() {
        if (_adapter != null) {
            _adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFundMessageReceived() {
        // ignore
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case TYPE_MYITEMS:
                User user = ((AuctionApplication) getApplicationContext()).getUser();
                _cursorLoader =
                        new CursorLoader(this, ItemContentProvider.CONTENT_URI, ItemDataSource.ALL_COLUMNS,
                                ItemSQLiteHelper.COLUMN_ISBIDDING + "=1 or " + ItemSQLiteHelper.COLUMN_ISWATCHING
                                        + "=1", null, "CASE WHEN " + ItemSQLiteHelper.COLUMN_WINNER + "!="
                                + user.getBidder() + " THEN 0 ELSE 1 END");
                setTitle("My Items");
                break;
            case TYPE_LOWBIDS:
                _cursorLoader =
                        new CursorLoader(this, ItemContentProvider.CONTENT_URI, ItemDataSource.ALL_COLUMNS,
                                ItemSQLiteHelper.COLUMN_BIDCOUNT + "<=2", null, ItemSQLiteHelper.COLUMN_BIDCOUNT);
                setTitle("Low Bids");
                break;
            case TYPE_SEARCH:
                _cursorLoader =
                        new CursorLoader(this, ItemContentProvider.CONTENT_URI, ItemDataSource.ALL_COLUMNS,
                                ItemSQLiteHelper.COLUMN_NUMBER + " like '%" + _filter + "%' or "
                                        + ItemSQLiteHelper.COLUMN_NAME + " like '%" + _filter + "%' or "
                                        + ItemSQLiteHelper.COLUMN_DESCRIPTION + " like '%" + _filter + "%'", null,
                                ItemSQLiteHelper.COLUMN_NUMBER);
                setTitle("Search Results");
                break;
            default:
                _cursorLoader =
                        new CursorLoader(this, ItemContentProvider.CONTENT_URI, ItemDataSource.ALL_COLUMNS,
                                null, null, null);
                setTitle("Items");
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

        User user = ((AuctionApplication) getApplicationContext()).getUser();
        _getItemsTask = new GetItemsTask(user);
        _getItemsTask.execute();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (TextUtils.isEmpty(query)) {
            _filter = null;
            _loaderManager.restartLoader(TYPE_ALL, null, this);
        } else {
            _filter = query;
            _loaderManager.restartLoader(TYPE_SEARCH, null, this);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
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
                Toast.makeText(ItemListActivity.this, getString(R.string.refresh_success),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ItemListActivity.this, getString(R.string.refresh_failed),
                        Toast.LENGTH_SHORT).show();
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

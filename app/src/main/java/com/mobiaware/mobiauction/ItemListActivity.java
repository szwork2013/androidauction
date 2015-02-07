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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
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

public class ItemListActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>,
        WSClient.OnMessageListener {
    private static final String TAG = ItemListActivity.class.getName();

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

        _listView = (AbsListView) findViewById(android.R.id.list);
        _listView.setEmptyView(findViewById(android.R.id.empty));
        _listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = _adapter.getCursor();
                cursor.moveToPosition(position);
                startActivity(BidActivity.newInstance(getApplicationContext(),
                        ItemDataSource.cursorToItem(cursor)));
            }
        });

        _listView.setAdapter(_adapter);

        _swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipecontainer);
        _swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });

        _listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

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
            case 1:
                User user = ((AuctionApplication) getApplicationContext()).getUser();

                _cursorLoader =
                        new CursorLoader(this, ItemContentProvider.CONTENT_URI, ItemDataSource.ALL_COLUMNS,
                                ItemSQLiteHelper.COLUMN_ISBIDDING + "=1 or " + ItemSQLiteHelper.COLUMN_ISWATCHING
                                        + "=1", null, "CASE WHEN " + ItemSQLiteHelper.COLUMN_WINNER + "!="
                                + user.getBidder() + " THEN 0 ELSE 1 END");

                setTitle("My Items");
                break;
            case 2:
                _cursorLoader =
                        new CursorLoader(this, ItemContentProvider.CONTENT_URI, ItemDataSource.ALL_COLUMNS,
                                ItemSQLiteHelper.COLUMN_BIDCOUNT + "<=2", null, ItemSQLiteHelper.COLUMN_BIDCOUNT);
                setTitle("Low Bids");
                break;
            case 11:
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

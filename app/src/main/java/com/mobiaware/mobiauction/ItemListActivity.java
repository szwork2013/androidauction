package com.mobiaware.mobiauction;

import android.app.ActionBar;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import com.mobiaware.mobiauction.items.Item;
import com.mobiaware.mobiauction.items.ItemContentProvider;
import com.mobiaware.mobiauction.items.ItemDataSource;
import com.mobiaware.mobiauction.items.ItemSQLiteHelper;
import com.mobiaware.mobiauction.tasks.GetItemsTask;
import com.mobiaware.mobiauction.users.User;

public class ItemListActivity extends WebSocketActivity implements SearchView.OnQueryTextListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ItemListActivity.class.getName();

    public static final int TYPE_ALL = 100;
    public static final int TYPE_MYITEMS = 200;
    public static final int TYPE_LOWBIDS = 300;
    public static final int TYPE_SEARCH = 400;

    private static final String ARG_TYPE = "type";
    private static final String ARG_FILTER = "filter";

    private SwipeRefreshLayout _swipeContainer;

    private int _type;
    private String _filter;

    private ItemListItemsAdapter _adapter;

    private LoaderManager _loaderManager;
    private CursorLoader _cursorLoader;

    private RefreshItemsTask _refreshTask;

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

        _adapter = new ItemListItemsAdapter(this, null);

        _loaderManager = getLoaderManager();
        _loaderManager.initLoader(_type, null, this);

        setContentView(R.layout.activity_item_list);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);
                if (cursor != null) {
                    Item item = ItemDataSource.cursorToItem(cursor);
                    startActivity(BidActivity.newInstance(getApplicationContext(), item.getUid()));
                }
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                boolean enable = false;
                if (listView.getChildCount() > 0) {
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

        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) searchView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.LTGRAY);

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
                User user = ((AuctionApplication) getApplication()).getUser();
                _cursorLoader =
                        new CursorLoader(this, ItemContentProvider.CONTENT_URI, ItemDataSource.ALL_COLUMNS,
                                ItemSQLiteHelper.COLUMN_ISBIDDING + "=1 or " + ItemSQLiteHelper.COLUMN_ISWATCHING
                                        + "=1", null, "CASE WHEN " + ItemSQLiteHelper.COLUMN_WINNER + "!="
                                + user.getBidder() + " THEN 0 ELSE 1 END");
                setTitle(getString(R.string.myitems));
                break;
            case TYPE_LOWBIDS:
                _cursorLoader =
                        new CursorLoader(this, ItemContentProvider.CONTENT_URI, ItemDataSource.ALL_COLUMNS,
                                ItemSQLiteHelper.COLUMN_BIDCOUNT + "<=2", null, ItemSQLiteHelper.COLUMN_BIDCOUNT);
                setTitle(getString(R.string.lowbids));
                break;
            case TYPE_SEARCH:
                _cursorLoader =
                        new CursorLoader(this, ItemContentProvider.CONTENT_URI, ItemDataSource.ALL_COLUMNS,
                                ItemSQLiteHelper.COLUMN_NUMBER + " like '%" + _filter + "%' or "
                                        + ItemSQLiteHelper.COLUMN_NAME + " like '%" + _filter + "%' or "
                                        + ItemSQLiteHelper.COLUMN_DESCRIPTION + " like '%" + _filter + "%'", null,
                                ItemSQLiteHelper.COLUMN_NUMBER);
                setTitle(getString(R.string.searchresults));
                break;
            default:
                _cursorLoader =
                        new CursorLoader(this, ItemContentProvider.CONTENT_URI, ItemDataSource.ALL_COLUMNS,
                                null, null, null);
                setTitle(getString(R.string.items));
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

    private void refreshItems() {
        if (_refreshTask != null) {
            return;
        }

        User user = ((AuctionApplication) getApplication()).getUser();
        _refreshTask = new RefreshItemsTask(user);
        _refreshTask.execute();
    }

    public class RefreshItemsTask extends GetItemsTask {
        public RefreshItemsTask(User user) {
            super(getApplicationContext(), user);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            _refreshTask = null;
            _swipeContainer.setRefreshing(false);

            if (success) {
                Toast.makeText(ItemListActivity.this, getString(R.string.refresh_success),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ItemListActivity.this, getString(R.string.refresh_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}

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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.mobiaware.mobiauction.api.RESTClient;
import com.mobiaware.mobiauction.api.WSClient;
import com.mobiaware.mobiauction.background.FetchItemsTask;
import com.mobiaware.mobiauction.items.Item;
import com.mobiaware.mobiauction.items.ItemDataSource;
import com.mobiaware.mobiauction.users.User;
import com.mobiaware.mobiauction.utils.Preconditions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class AuctionActivity extends ActionBarActivity implements
        NavDrawerFragment.NavigationDrawerCallbacks, WSClient.WebsocketCallbacks {
    private static final String TAG = AuctionActivity.class.getName();

    private ItemDataSource _datasource;
    private WSClient _ws;

    private ProgressDialog _progressDlg;

    private GetItemsTask _getItemsTask;

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, AuctionActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _ws = new WSClient(this);

        _datasource = new ItemDataSource(this);

        setContentView(R.layout.activity_auction);

        NavDrawerFragment navDrawerFragment =
                (NavDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        navDrawerFragment
                .setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onResume() {

        _ws.start();

        refreshItems();

        super.onResume();
    }

    @Override
    protected void onPause() {
        _ws.stop();

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        _ws.stop();

        super.onDestroy();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        ArrayList items = new ArrayList();;

//        switch (position) {
//            case 1:
//                items = _datasource.getMyItems();
//
//                class MyItemsComparator implements Comparator<Item> {
//                    @Override
//                    public int compare(Item o1, Item o2) {
//                        Boolean c1 = Boolean.valueOf(o1.getWinner().equals(_user.getBidder()));
//                        Boolean c2 = Boolean.valueOf(o2.getWinner().equals(_user.getBidder()));
//                        int i = c1.compareTo(c2);
//                        if (i != 0) {
//                            return i;
//                        }
//
//                        c1 = Boolean.valueOf(o1.isBidding());
//                        c2 = Boolean.valueOf(o2.isBidding());
//                        i = c1.compareTo(c2);
//                        if (i != 0) {
//                            return i;
//                        }
//
//                        c1 = Boolean.valueOf(o1.isWatching());
//                        c2 = Boolean.valueOf(o2.isWatching());
//                        i = c1.compareTo(c2);
//                        if (i != 0) {
//                            return i;
//                        }
//
//                        return o1.getNumber().compareTo(o2.getNumber());
//                    }
//                }
//
//                Collections.sort(items, new MyItemsComparator());
//                break;
//            case 2:
//                items = _datasource.getLowBidItems();
//                break;
//            default:
//                items = _datasource.getItems();
//                break;
//        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, ItemListFragment.newInstance(position)).commit();
    }

    @Override
    public void onNavigationDrawerSearch(String search) {
        ArrayList items = new ArrayList();

//        if (search != null) {
//            items = _datasource.getSearchItems(search);
//        } else {
//            items = _datasource.getItems();
//        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, ItemListFragment.newInstance(0)).commit();
    }

    @Override
    public void onItemMessage(String payload) {
        try {
            JSONObject object = new JSONObject(payload);
            _datasource.createItem(object.getLong("uid"), object.getString("itemNumber"),
                    object.getString("name"), object.getString("description"), object.getString("category"),
                    object.getString("seller"), object.getDouble("valPrice"), object.getDouble("minPrice"),
                    object.getDouble("incPrice"), object.getDouble("curPrice"),
                    object.optString("winner", ""), object.optLong("bidCount", 0),
                    object.optLong("watchCount", 0), object.optString("url", ""), object.getBoolean("multi"));
        } catch (JSONException e) {
            Log.e(TAG, "Error fetching auction items.", e);
        }
    }

    private void refreshItems() {
        if (_getItemsTask != null) {
            return;
        }
        User user = ((AuctionApplication) getApplicationContext()).getActiveUser();

        showProgress();
        _getItemsTask =
                new GetItemsTask();
        _getItemsTask.execute(new String[] { user.getBidder(), user.getPassword() });
    }

    private void showProgress() {
        hideProgress();

        _progressDlg = new ProgressDialog(this);
        _progressDlg.setMessage(getString(R.string.progress_refreshing));
        _progressDlg.setIndeterminate(true);
        _progressDlg.show();
    }

    private void hideProgress() {
        if (_progressDlg != null && _progressDlg.isShowing()) {
            _progressDlg.dismiss();
            _progressDlg = null;
        }
    }

    public class GetItemsTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            Preconditions
                    .checkArgument(params.length == 2, "Requires bidder and password as parameters.");

            try {
                fetchItems();
                fetchBids(params[0], params[1]);
                fetchWatches(params[0], params[1]);
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
            hideProgress();
        }

        @Override
        protected void onCancelled() {
            _getItemsTask = null;
            hideProgress();
        }

        private void fetchItems() throws IOException, JSONException {
            String response = RESTClient.get("/event/auctions/1/items", null);

            JSONArray array = new JSONArray(response);

            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);

                _datasource.createItem(object.getLong("uid"), object.getString("itemNumber"),
                        object.getString("name"), object.getString("description"), object.getString("category"),
                        object.getString("seller"), object.getDouble("valPrice"), object.getDouble("minPrice"),
                        object.getDouble("incPrice"), object.getDouble("curPrice"),
                        object.optString("winner", ""), object.optLong("bidCount", 0),
                        object.optLong("watchCount", 0), object.optString("url", ""), object.getBoolean("multi"));
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

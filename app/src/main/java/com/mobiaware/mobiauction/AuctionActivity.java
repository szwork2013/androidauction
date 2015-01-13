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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;

import com.mobiaware.mobiauction.api.RESTClient;
import com.mobiaware.mobiauction.items.ItemDataSource;
import com.mobiaware.mobiauction.users.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class AuctionActivity extends ActionBarActivity implements
        NavigationFragment.NavigationDrawerCallbacks, ItemListFragment.OnFragmentInteractionListener {
    public static final String ARG_USER = "user";

    private NavigationFragment _navigationFragment;

    private CharSequence _title;

    private ItemDataSource _itemDatasource;

    private GetItemsTask _getItemsTask = null;

    private User _user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _itemDatasource = new ItemDataSource(this);
        _itemDatasource.open();

        setContentView(R.layout.activity_items);

        _navigationFragment =
                (NavigationFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        _title = getTitle();

        _navigationFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        _user = getIntent().getExtras().getParcelable(ARG_USER);
    }

    @Override
    protected void onResume() {
        _itemDatasource.open();

        refreshItems();
        super.onResume();
    }

    @Override
    protected void onPause() {
        _itemDatasource.close();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        _itemDatasource.close();
        super.onDestroy();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // TODO: Remove this refresh
        refreshItems();

        ArrayList items = _itemDatasource.getItems();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, ItemListFragment.newInstance(items))
                .commit();
    }

    @Override
    public void onFragmentInteraction(String id) {
        // TODO: Update argument type and name
    }

    public void refreshItems() {
        _getItemsTask = new GetItemsTask(this);
        _getItemsTask.execute((Void) null);
    }

    public class GetItemsTask extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog _progressDlg;

        GetItemsTask(AuctionActivity activity) {
            _progressDlg = new ProgressDialog(activity);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String response = RESTClient.get("/event/auctions/1/items", null, null);

                JSONArray array = new JSONArray(response);

                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);

                    _itemDatasource.createItem(object.getString("itemNumber"),
                            object.getString("name"), object.getString("description"),
                            object.getString("category"), object.getString("seller"),
                            object.getDouble("valPrice"), object.getDouble("minPrice"),
                            object.getDouble("incPrice"), object.getDouble("curPrice"),
                            object.getString("winner"), object.getLong("bidCount"), object.getLong("watchCount"),
                            object.getString("url"), object.getBoolean("multi"));
                }
            } catch (IOException e) {
                return false;
            } catch (JSONException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPreExecute() {
            _progressDlg.setMessage(getString(R.string.list_refreshing));
            _progressDlg.show();
            _progressDlg.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            _getItemsTask = null;

            if (_progressDlg.isShowing()) {
                _progressDlg.dismiss();
            }

            if (success) {
                // TODO: ?
            } else {
                // TODO: show alert message
            }
        }

        @Override
        protected void onCancelled() {
            _getItemsTask = null;

            if (_progressDlg.isShowing()) {
                _progressDlg.dismiss();
            }
        }
    }
}

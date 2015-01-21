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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;

import com.mobiaware.mobiauction.api.WSClient;

public class AuctionActivity extends ActionBarActivity implements
        NavDrawerFragment.NavigationDrawerCallbacks, WSClient.WebsocketCallbacks {
    private WSClient _ws;

    public static Intent newInstance(Context context) {
        return new Intent(context, AuctionActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auction);

        FragmentManager fragmentManager = getSupportFragmentManager();
        NavDrawerFragment navDrawerFragment =
                (NavDrawerFragment) fragmentManager.findFragmentById(R.id.navigation_drawer);
        navDrawerFragment
                .setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        _ws = new WSClient(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        _ws.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        _ws.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        _ws.stop();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        ItemListFragment fragment = ItemListFragment.newInstance(position, null);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment).commit();
    }

    @Override
    public void onNavigationDrawerSearch(String search) {
        ItemListFragment fragment;
        if (TextUtils.isEmpty(search)) {
            fragment = ItemListFragment.newInstance(0, null);
        } else {
            fragment = ItemListFragment.newInstance(11, search);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment).commit();
    }

    @Override
    public void onItemMessage(String payload) {
        // try {
        // JSONObject object = new JSONObject(payload);
        // _datasource.createItem(object.getLong("uid"), object.getString("itemNumber"),
        // object.getString("name"), object.getString("description"), object.getString("category"),
        // object.getString("seller"), object.getDouble("valPrice"), object.getDouble("minPrice"),
        // object.getDouble("incPrice"), object.getDouble("curPrice"),
        // object.optString("winner", ""), object.optLong("bidCount", 0),
        // object.optLong("watchCount", 0), object.optString("url", ""), object.getBoolean("multi"));
        // } catch (JSONException e) {
        // Log.e(TAG, "Error fetching auction items.", e);
        // }
    }
}

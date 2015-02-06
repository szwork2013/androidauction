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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;

import com.mobiaware.mobiauction.api.WSClient;
import com.mobiaware.mobiauction.items.Item;

public class AuctionActivity extends Activity implements
        NavDrawerFragment.NavigationDrawerCallbacks, ItemListFragment.ListItemCallbacks, WSClient.OnMessageListener {
    private static final String FRAGMENT_TAG = "ITEMLISTFRAGMENT";

    private WSClient _webSocket;

    public static Intent newInstance(Context context) {
        return new Intent(context, AuctionActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auction);

        FragmentManager fragmentManager = getFragmentManager();
        NavDrawerFragment navDrawerFragment =
                (NavDrawerFragment) fragmentManager.findFragmentById(R.id.navigation_drawer);
        navDrawerFragment
                .setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        _webSocket = new WSClient(this, this);
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
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment;

       if (position == 3) {
           startActivity(FundActivity.newInstance(getApplicationContext()));
       } else {

           switch (position) {
               case 0:
                   setTitle(getString(R.string.title_listitems));
                   fragment = ItemListFragment.newInstance(0, null);
                   break;
               case 1:
                   setTitle(getString(R.string.title_listmyitems));
                   fragment = ItemListFragment.newInstance(1, null);
                   break;
               case 2:
                   setTitle(getString(R.string.title_listlowbids));
                   fragment = ItemListFragment.newInstance(2, null);
                   break;
               default:
                   setTitle(getString(R.string.title_listitems));
                   fragment = ItemListFragment.newInstance(0, null);
           }

           FragmentManager fragmentManager = getFragmentManager();
           fragmentManager.beginTransaction()
                   .replace(R.id.container, fragment, FRAGMENT_TAG).commit();
       }
    }

    @Override
    public void onNavigationDrawerSearch(String search) {
        ItemListFragment fragment;
        if (TextUtils.isEmpty(search)) {
            fragment = ItemListFragment.newInstance(0, null);
        } else {
            fragment = ItemListFragment.newInstance(11, search);
        }

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, FRAGMENT_TAG).commit();
    }

    @Override
    public void onItemMessageReceived() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment myFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (myFragment.isVisible()) {
            ((ItemListFragment)myFragment).refreshABC();
        }
    }

    @Override
    public void onFundMessageReceived() {
        // ignore
    }

    @Override
    public void onListItemSelected(Item position) {
        startActivity(BidActivity.newInstance(getApplicationContext(), position));
    }
}

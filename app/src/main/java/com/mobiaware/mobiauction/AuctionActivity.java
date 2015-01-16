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

import com.mobiaware.mobiauction.background.FetchItemsTask;
import com.mobiaware.mobiauction.items.Item;
import com.mobiaware.mobiauction.items.ItemDataSource;
import com.mobiaware.mobiauction.users.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class AuctionActivity extends ActionBarActivity implements
        NavDrawerFragment.NavigationDrawerCallbacks {
    private static final String ARG_USER = "user";

    private ItemDataSource _datasource;
    private User _user;

    public static Intent newInstance(Context context, User user) {
        Intent intent = new Intent(context, AuctionActivity.class);
        intent.putExtra(ARG_USER, user);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _datasource = new ItemDataSource(this);
        _datasource.open();

        _user = getIntent().getExtras().getParcelable(AuctionApplication.ARG_USER);

        setContentView(R.layout.activity_auction);

        NavDrawerFragment navDrawerFragment =
                (NavDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        navDrawerFragment
                .setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onResume() {
        _datasource.open();

        FetchItemsTask task =
                new FetchItemsTask(this, _datasource, _user, getString(R.string.progress_refreshing));
        task.execute((Void) null);

        super.onResume();
    }

    @Override
    protected void onPause() {
        _datasource.close();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        _datasource.close();
        super.onDestroy();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        ArrayList items;

        switch (position) {
            case 1:
                items = _datasource.getMyItems();

                class MyItemsComparator implements Comparator<Item> {
                    @Override
                    public int compare(Item o1, Item o2) {
                        Boolean c1 = Boolean.valueOf(o1.getWinner().equals(_user.getBidder()));
                        Boolean c2 = Boolean.valueOf(o2.getWinner().equals(_user.getBidder()));
                        int i = c1.compareTo(c2);
                        if (i != 0) {
                            return i;
                        }

                        c1 = Boolean.valueOf(o1.isBidding());
                        c2 = Boolean.valueOf(o2.isBidding());
                        i = c1.compareTo(c2);
                        if (i != 0) {
                            return i;
                        }

                        c1 = Boolean.valueOf(o1.isWatching());
                        c2 = Boolean.valueOf(o2.isWatching());
                        i = c1.compareTo(c2);
                        if (i != 0) {
                            return i;
                        }

                        return o1.getNumber().compareTo(o2.getNumber());
                    }
                }

                Collections.sort(items, new MyItemsComparator());
                break;
            case 2:
                items = _datasource.getLowBidItems();
                break;
            default:
                items = _datasource.getItems();
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, ItemListFragment.newInstance(_user, items)).commit();
    }

    @Override
    public void onNavigationDrawerSearch(String search) {
        ArrayList items;

        if (search != null) {
            items = _datasource.getSearchItems(search);
        } else {
            items = _datasource.getItems();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, ItemListFragment.newInstance(_user, items)).commit();
    }
}

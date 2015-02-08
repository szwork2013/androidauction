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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.mobiaware.mobiauction.api.WSClient;
import com.mobiaware.mobiauction.items.ItemDataSource;
import com.mobiaware.mobiauction.users.User;

public class AuctionActivity extends Activity implements SearchView.OnQueryTextListener,
        WSClient.OnMessageListener {
    static class ViewHolder {
        TextView winningCount;
        TextView losingCount;
        TextView welcomeMsg;
        TextView bidderNumber;
    }

    private ItemDataSource _datasource;

    private WSClient _webSocket;

    private ViewHolder _viewHolder;

    public static Intent newInstance(Context context) {
        return new Intent(context, AuctionActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auction);

        _datasource = new ItemDataSource(this);

        _webSocket = new WSClient(this, this);

        _viewHolder = new ViewHolder();
        _viewHolder.winningCount = (TextView) findViewById(R.id.textWinningCount);
        _viewHolder.losingCount = (TextView) findViewById(R.id.textLosingCount);
        _viewHolder.welcomeMsg = (TextView) findViewById(R.id.textWelcome);
        _viewHolder.bidderNumber = (TextView) findViewById(R.id.textBidderNumber);

        findViewById(R.id.btnItems).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ItemListActivity.newInstance(getApplicationContext(),
                        ItemListActivity.TYPE_ALL, null));
            }
        });

        findViewById(R.id.btnMyItems).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ItemListActivity.newInstance(getApplicationContext(),
                        ItemListActivity.TYPE_MYITEMS, null));
            }
        });

        findViewById(R.id.btnLowBids).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ItemListActivity.newInstance(getApplicationContext(),
                        ItemListActivity.TYPE_LOWBIDS, null));
            }
        });

        findViewById(R.id.btnFund).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(FundActivity.newInstance(getApplicationContext()));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        _webSocket.start();
        updateView();
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
        getMenuInflater().inflate(R.menu.menu_auction, menu);

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
    public boolean onQueryTextSubmit(String query) {
        if (TextUtils.isEmpty(query)) {
            startActivity(ItemListActivity.newInstance(getApplicationContext(),
                    ItemListActivity.TYPE_SEARCH, query));
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onItemMessageReceived() {
        updateView();
    }

    @Override
    public void onFundMessageReceived() {
        // ignore
    }

    private void updateView() {
        User user = ((AuctionApplication) getApplicationContext()).getUser();
        _viewHolder.welcomeMsg.setText(String.format(getString(R.string.label_welcome),
                user.getFirstName()));
        _viewHolder.bidderNumber.setText(String.format(getString(R.string.label_bidder),
                user.getBidder()));
        _viewHolder.winningCount.setText(String.format(getString(R.string.label_winning2),
                Integer.toString(_datasource.getWinningCount(user))));
        _viewHolder.losingCount.setText(String.format(getString(R.string.label_losing2),
                Integer.toString(_datasource.getLosingCount(user))));
    }
}

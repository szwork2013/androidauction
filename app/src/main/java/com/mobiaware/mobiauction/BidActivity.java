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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiaware.mobiauction.api.RESTClient;
import com.mobiaware.mobiauction.api.WSClient;
import com.mobiaware.mobiauction.controls.ValueStepper;
import com.mobiaware.mobiauction.items.Item;
import com.mobiaware.mobiauction.items.ItemDataSource;
import com.mobiaware.mobiauction.users.User;
import com.mobiaware.mobiauction.utils.FormatUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class BidActivity extends Activity implements SearchView.OnQueryTextListener,
        WSClient.OnMessageListener {
    private static final String TAG = BidActivity.class.getName();

    private static final String ARG_ITEM = "item";

    static class ViewHolder {
        ValueStepper valueStepper;
        TextView itemName;
        TextView itemNumber;
        TextView itemBids;
        TextView itemPrice;
        ImageView itemWinning;
        ImageView itemLosing;
        ImageView itemFavorite;
        TextView itemMinimumInc;
        TextView itemWinner;
        TextView itemValue;
        TextView itemDescription;
        TextView itemDonatedBy;

        String[] bidLabels;
    }

    private Item _item;

    private ItemDataSource _datasource;

    private WSClient _webSocket;

    private ViewHolder _viewHolder;

    private BidTask _bidTask;

    public static Intent newInstance(Context context, Item item) {
        Intent intent = new Intent(context, BidActivity.class);
        intent.putExtra(ARG_ITEM, item);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bid);

        _item = getIntent().getExtras().getParcelable(ARG_ITEM);

        _datasource = new ItemDataSource(this);

        _webSocket = new WSClient(this, this);

        _viewHolder = new ViewHolder();
        _viewHolder.valueStepper = (ValueStepper) findViewById(R.id.fundValueStepper);
        _viewHolder.itemName = (TextView) findViewById(R.id.itemName);
        _viewHolder.itemNumber = (TextView) findViewById(R.id.itemNumber);
        _viewHolder.itemBids = (TextView) findViewById(R.id.itemBids);
        _viewHolder.itemPrice = (TextView) findViewById(R.id.itemPrice);
        _viewHolder.itemWinning = (ImageView) findViewById(R.id.itemWinning);
        _viewHolder.itemLosing = (ImageView) findViewById(R.id.itemLosing);
        _viewHolder.itemFavorite = (ImageView) findViewById(R.id.itemFavorite);
        _viewHolder.itemMinimumInc = (TextView) findViewById(R.id.itemMinimumInc);
        _viewHolder.itemWinner = (TextView) findViewById(R.id.itemWinner);
        _viewHolder.itemValue = (TextView) findViewById(R.id.itemValue);
        _viewHolder.itemDescription = (TextView) findViewById(R.id.itemDescription);
        _viewHolder.itemDonatedBy = (TextView) findViewById(R.id.itemDonatedBy);
        _viewHolder.bidLabels = getResources().getStringArray(R.array.label_bid_count);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.itemBid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBid();
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
    public void onItemMessageReceived() {
        _item = _datasource.getItem(_item.getUid());
        updateView();
    }

    @Override
    public void onFundMessageReceived() {
        // nothing
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateView() {
        _viewHolder.itemName.setText(_item.getName());
        _viewHolder.itemNumber.setText(_item.getNumber());

        long bids = _item.getBidCount();
        if (bids == 0) {
            _viewHolder.itemBids.setText(_viewHolder.bidLabels[0]);
        } else if (bids == 1) {
            _viewHolder.itemBids.setText(_viewHolder.bidLabels[1]);
        } else {
            _viewHolder.itemBids.setText(String.format(_viewHolder.bidLabels[2],
                    Long.toString(_item.getBidCount())));
        }

        _viewHolder.itemPrice.setText(FormatUtils.valueToString(_item.getCurPrice()));
        _viewHolder.itemPrice.setTextColor(Color.rgb(0, 102, 0));

        if (_item.isBidding()) {
            User user = ((AuctionApplication) getApplicationContext()).getUser();
            if (_item.getWinner().equals(user.getBidder())) {
                _viewHolder.itemWinning.setVisibility(ImageView.VISIBLE);
                _viewHolder.itemLosing.setVisibility(ImageView.INVISIBLE);
                _viewHolder.itemFavorite.setVisibility(ImageView.INVISIBLE);
            } else {
                _viewHolder.itemWinning.setVisibility(ImageView.INVISIBLE);
                _viewHolder.itemLosing.setVisibility(ImageView.VISIBLE);
                _viewHolder.itemFavorite.setVisibility(ImageView.INVISIBLE);
            }
        } else if (_item.isWatching()) {
            _viewHolder.itemWinning.setVisibility(ImageView.INVISIBLE);
            _viewHolder.itemLosing.setVisibility(ImageView.INVISIBLE);
            _viewHolder.itemFavorite.setVisibility(ImageView.VISIBLE);
        } else {
            _viewHolder.itemWinning.setVisibility(ImageView.INVISIBLE);
            _viewHolder.itemLosing.setVisibility(ImageView.INVISIBLE);
            _viewHolder.itemFavorite.setVisibility(ImageView.INVISIBLE);
        }

        _viewHolder.itemMinimumInc.setText(FormatUtils.valueToString(_item.getIncPrice()));
        _viewHolder.itemWinner.setText(_item.getWinner());
        _viewHolder.itemValue.setText(FormatUtils.valueToString(_item.getValPrice()));
        _viewHolder.itemDescription.setText(_item.getDescription());
        _viewHolder.itemDonatedBy.setText(_item.getSeller());

        double bidValue = Math.max(_item.getMinPrice(), _item.getCurPrice() + _item.getIncPrice());
        _viewHolder.valueStepper.setMinimum(bidValue);
        _viewHolder.valueStepper.setStep(_item.getIncPrice());
        _viewHolder.valueStepper.setMaximum(_item.getCurPrice() + 500);
        _viewHolder.valueStepper.setValue(bidValue);
    }

    private void sendBid() {
        if (_bidTask != null) {
            return;
        }

        final double bid = _viewHolder.valueStepper.getValue();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        String message = String.format(getString(R.string.bid_prompt), FormatUtils.valueToString(bid));
        alertDialogBuilder.setMessage(message).setCancelable(false)
                .setPositiveButton(R.string.prompt_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        _bidTask = new BidTask(bid);
                        _bidTask.execute();
                    }
                }).setNegativeButton(R.string.prompt_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private class BidTask extends AsyncTask<String, Void, Boolean> {
        private final double _bidPrice;

        public BidTask(double bidPrice) {
            _bidPrice = bidPrice;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                User user = ((AuctionApplication) getApplicationContext()).getUser();

                JSONObject input = new JSONObject();
                input.put("itemUid", _item.getUid());
                input.put("bidPrice", _bidPrice);

                RESTClient.post("/live/bids", user, input.toString());

                _datasource.setIsBidding(_item.getUid());
                _datasource.setIsWatching(_item.getUid());
                _item = _datasource.getItem(_item.getUid());
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error bidding.", e);
                return false;
            } catch (JSONException e) {
                Log.e(TAG, "Error bidding.", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            _bidTask = null;

            if (success) {
                Toast.makeText(BidActivity.this, getString(R.string.bid_success), Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(BidActivity.this, getString(R.string.bid_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }
}

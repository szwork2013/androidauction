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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiaware.mobiauction.api.RESTClient;
import com.mobiaware.mobiauction.controls.ValueStepper;
import com.mobiaware.mobiauction.items.Item;
import com.mobiaware.mobiauction.users.User;
import com.mobiaware.mobiauction.utils.FormatUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class BidActivity extends ActionBarActivity {
    private static final String TAG = BidActivity.class.getName();

    private static final String ARG_ITEM = "item";

    private ValueStepper _bidValueStepper;

    private Item _item;
    private User _user;

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
        _user = ((AuctionApplication) getApplicationContext()).getActiveUser();

        ((TextView) findViewById(R.id.itemName)).setText(_item.getName());
        ((TextView) findViewById(R.id.itemNumber)).setText(_item.getNumber());

        String[] labels = getResources().getStringArray(R.array.label_bid_count);

        long bids = _item.getBidCount();
        if (bids == 0) {
            ((TextView) findViewById(R.id.itemBids)).setText(labels[0]);
        } else if (bids == 1) {
            ((TextView) findViewById(R.id.itemBids)).setText(labels[1]);
        } else {
            ((TextView) findViewById(R.id.itemBids)).setText(String.format(labels[2],
                    Long.toString(_item.getBidCount())));
        }

        ((TextView) findViewById(R.id.itemPrice))
                .setText(FormatUtils.valueToString(_item.getCurPrice()));
        ((TextView) findViewById(R.id.itemPrice)).setTextColor(Color.rgb(0, 102, 0));

        if (_item.isBidding()) {
            if (_item.getWinner().equals(_user.getBidder())) {
                findViewById(R.id.itemWinning).setVisibility(ImageView.VISIBLE);
                findViewById(R.id.itemLosing).setVisibility(ImageView.INVISIBLE);
                findViewById(R.id.itemFavorite).setVisibility(ImageView.INVISIBLE);
            } else {
                findViewById(R.id.itemWinning).setVisibility(ImageView.INVISIBLE);
                findViewById(R.id.itemLosing).setVisibility(ImageView.VISIBLE);
                findViewById(R.id.itemFavorite).setVisibility(ImageView.INVISIBLE);
            }
        } else if (_item.isWatching()) {
            findViewById(R.id.itemWinning).setVisibility(ImageView.INVISIBLE);
            findViewById(R.id.itemLosing).setVisibility(ImageView.INVISIBLE);
            findViewById(R.id.itemFavorite).setVisibility(ImageView.VISIBLE);
        } else {
            findViewById(R.id.itemWinning).setVisibility(ImageView.INVISIBLE);
            findViewById(R.id.itemLosing).setVisibility(ImageView.INVISIBLE);
            findViewById(R.id.itemFavorite).setVisibility(ImageView.INVISIBLE);
        }

        ((TextView) findViewById(R.id.itemMinimumInc)).setText(FormatUtils.valueToString(_item
                .getIncPrice()));
        ((TextView) findViewById(R.id.itemWinner)).setText(_item.getWinner());
        ((TextView) findViewById(R.id.itemValue))
                .setText(FormatUtils.valueToString(_item.getValPrice()));
        ((TextView) findViewById(R.id.itemDescription)).setText(_item.getDescription());
        ((TextView) findViewById(R.id.itemDonatedBy)).setText(_item.getSeller());

        _bidValueStepper = (ValueStepper) findViewById(R.id.fundValueStepper);
        _bidValueStepper.setMinimum(_item.getCurPrice() + _item.getIncPrice());
        _bidValueStepper.setStep(_item.getIncPrice());
        _bidValueStepper.setMaximum(_item.getCurPrice() + 500);
        _bidValueStepper.setValue(_item.getCurPrice() + _item.getIncPrice());

        findViewById(R.id.itemBid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBid();
            }
        });
    }

    private void sendBid() {
        if (_bidTask != null) {
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        String message =
                String.format(getString(R.string.bid_prompt),
                        FormatUtils.valueToString(_bidValueStepper.getValue()));
        alertDialogBuilder.setMessage(message).setCancelable(false)
                .setPositiveButton(R.string.prompt_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        _bidTask = new BidTask(_bidValueStepper.getValue());
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

    private class BidTask extends AsyncTask<String, Void, Item> {
        private final double _bidPrice;

        public BidTask(double bidPrice) {
            _bidPrice = bidPrice;
        }

        @Override
        protected Item doInBackground(String... params) {
            try {
                JSONObject input = new JSONObject();
                input.put("itemUid", _item.getUid());
                input.put("bidPrice", _bidPrice);

                String response = RESTClient.post("/live/bids", _user, input.toString());

                JSONObject object = new JSONObject(response);

                return new Item(object.getLong("uid"), null, null, null, null, null, 0, 0, 0,
                        object.getDouble("curPrice"), object.getString("winner"),
                        object.optLong("bidCount", 0), object.optLong("watchCount", 0), null, false, true, true);
            } catch (IOException e) {
                Log.e(TAG, "Error adding fund.", e);
                return null;
            } catch (JSONException e) {
                Log.e(TAG, "Error adding fund.", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Item value) {
            _bidTask = null;

            if (value != null) {
                Toast.makeText(BidActivity.this, getString(R.string.bid_success), Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(BidActivity.this, getString(R.string.bid_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }
}

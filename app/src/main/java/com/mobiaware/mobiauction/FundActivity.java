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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiaware.mobiauction.api.RESTClient;
import com.mobiaware.mobiauction.api.WSClient;
import com.mobiaware.mobiauction.controls.ValueStepper;
import com.mobiaware.mobiauction.funds.Fund;
import com.mobiaware.mobiauction.users.User;
import com.mobiaware.mobiauction.utils.FormatUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class FundActivity extends Activity implements SearchView.OnQueryTextListener,
        WSClient.OnMessageListener {
    private static final String TAG = FundActivity.class.getName();

    private static final double FUND_VALUE_ONE = 25.0;
    private static final double FUND_VALUE_TWO = 50.0;
    private static final double FUND_VALUE_THREE = 100.0;

    static class ViewHolder {
        ValueStepper fundValueStepper;
        TextView fundValueTextView;
    }

    private WSClient _webSocket;

    private ViewHolder _viewHolder;

    private FundTask _fundTask;

    public static Intent newInstance(Context context) {
        return new Intent(context, FundActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fund);

        _webSocket = new WSClient(this, this);

        _viewHolder = new ViewHolder();
        _viewHolder.fundValueStepper = (ValueStepper) findViewById(R.id.fundValueStepper);
        _viewHolder.fundValueTextView = (TextView) findViewById(R.id.fundValue);

        _viewHolder.fundValueStepper.setMinimum(5);
        _viewHolder.fundValueStepper.setStep(5);
        _viewHolder.fundValueStepper.setMaximum(1000);
        _viewHolder.fundValueStepper.setValue(5);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.fundValueOne).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFunds(FUND_VALUE_ONE);
            }
        });

        findViewById(R.id.fundValueTwo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFunds(FUND_VALUE_TWO);
            }
        });

        findViewById(R.id.fundValueThree).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFunds(FUND_VALUE_THREE);
            }
        });

        findViewById(R.id.fundValueOther).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFunds(_viewHolder.fundValueStepper.getValue());
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
        _webSocket.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        _webSocket.stop();
        super.onDestroy();
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
        // ignore
    }

    @Override
    public void onFundMessageReceived() {
        updateView();
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
        Fund fund = ((AuctionApplication) getApplicationContext()).getFund();
        _viewHolder.fundValueTextView.setText(FormatUtils.valueToString(fund.getValue()));
        _viewHolder.fundValueTextView.setTextColor(Color.rgb(0, 102, 0));
    }

    private void sendFunds(final double fundPrice) {
        if (_fundTask != null) {
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        String message =
                String.format(getString(R.string.fund_prompt), FormatUtils.valueToString(fundPrice));
        alertDialogBuilder.setMessage(message).setCancelable(false)
                .setPositiveButton(R.string.prompt_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        _fundTask = new FundTask(fundPrice);
                        _fundTask.execute();
                    }
                }).setNegativeButton(R.string.prompt_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private class FundTask extends AsyncTask<String, Void, Double> {
        private final double _fundPrice;

        public FundTask(double fundPrice) {
            _fundPrice = fundPrice;
        }

        @Override
        protected Double doInBackground(String... params) {
            try {
                User user = ((AuctionApplication) getApplicationContext()).getUser();

                JSONObject input = new JSONObject();
                input.put("auctionUid", 1);
                input.put("bidPrice", _fundPrice);

                String response = RESTClient.post("/live/funds", user, input.toString());

                return Double.parseDouble(response);
            } catch (IOException e) {
                Log.e(TAG, "Error adding fund.", e);
                return -1.0;
            } catch (JSONException e) {
                Log.e(TAG, "Error adding fund.", e);
                return -1.0;
            }
        }

        @Override
        protected void onPostExecute(Double value) {
            _fundTask = null;

            if (value > 0.0) {
                Toast.makeText(FundActivity.this, getString(R.string.fund_success), Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(FundActivity.this, getString(R.string.fund_failed), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}

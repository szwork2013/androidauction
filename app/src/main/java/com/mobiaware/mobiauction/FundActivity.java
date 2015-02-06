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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class FundActivity extends Activity implements WSClient.OnMessageListener {
    private static final String TAG = FundActivity.class.getName();

    private static final double FUND_VALUE_ONE = 25.0;
    private static final double FUND_VALUE_TWO = 50.0;
    private static final double FUND_VALUE_THREE = 100.0;

    private WSClient _webSocket;

    private TextView _fundValueTextView;
    private ValueStepper _fundValueStepper;

    private User _user;

    private FundTask _fundTask;

    public static Intent newInstance(Context context) {
        return new Intent(context, FundActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fund);

        _user = ((AuctionApplication) getApplicationContext()).getUser();

        _fundValueStepper = (ValueStepper) findViewById(R.id.fundValueStepper);
        _fundValueStepper.setMinimum(5);
        _fundValueStepper.setStep(5);
        _fundValueStepper.setMaximum(1000);
        _fundValueStepper.setValue(5);

        _fundValueTextView = ((TextView) findViewById(R.id.fundValue));
        _fundValueTextView.setTextColor(Color.rgb(0, 102, 0));

        Fund fund = ((AuctionApplication) getApplicationContext()).getFund();
        _fundValueTextView.setText(FormatUtils.valueToString(fund.getValue()));

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
                sendFunds(_fundValueStepper.getValue());
            }
        });

        _webSocket = new WSClient(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _webSocket.start();
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

    @Override
    public void onItemMessageReceived() {
        // ignore
    }

    @Override
    public void onFundMessageReceived() {
        Fund fund = ((AuctionApplication) getApplicationContext()).getFund();
        _fundValueTextView.setText(FormatUtils.valueToString(fund.getValue()));
    }

    private class FundTask extends AsyncTask<String, Void, Double> {
        private final double _fundPrice;

        public FundTask(double fundPrice) {
            _fundPrice = fundPrice;
        }

        @Override
        protected Double doInBackground(String... params) {
            try {
                JSONObject input = new JSONObject();
                input.put("auctionUid", 1);
                input.put("bidPrice", _fundPrice);

                String response = RESTClient.post("/live/funds", _user, input.toString());

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

                _fundValueTextView.setText(FormatUtils.valueToString(value));
            } else {
                Toast.makeText(FundActivity.this, getString(R.string.fund_failed), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}

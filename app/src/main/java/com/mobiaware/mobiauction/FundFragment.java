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

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiaware.mobiauction.api.RESTClient;
import com.mobiaware.mobiauction.controls.ValueStepper;
import com.mobiaware.mobiauction.users.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

public class FundFragment extends Fragment {
    private static final String TAG = FundFragment.class.getName();

    private TextView _fundValue;
    private ValueStepper _customValue;

    private FundTask _fundTask;

    public static FundFragment newInstance() {
        FundFragment fragment = new FundFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public FundFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fund, container, false);

        _customValue = (ValueStepper) view.findViewById(R.id.valueStepper);
        _customValue.setMinimum(5);
        _customValue.setStep(5);
        _customValue.setMaximum(1000);
        _customValue.setValue(5);

        _fundValue = ((TextView) view.findViewById(R.id.fundValue));
        _fundValue.setTextColor(Color.rgb(0, 102, 0));

        double value = ((AuctionApplication) getActivity().getApplicationContext()).getFundValue();

        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
        nf.setMinimumFractionDigits(0);

        _fundValue.setText(nf.format(value));

        view.findViewById(R.id.twentyFiveFund).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFunds(25.0);
            }
        });

        view.findViewById(R.id.fiftyFund).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFunds(50.0);
            }
        });

        view.findViewById(R.id.oneHundredFund).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFunds(100.0);
            }
        });

        view.findViewById(R.id.customFund).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFunds(_customValue.getValue());
            }
        });

        return view;
    }

    private void sendFunds(double fundPrice) {
        if (_fundTask != null) {
            return;
        }

        User user = ((AuctionApplication) getActivity().getApplicationContext()).getActiveUser();

        _fundTask = new FundTask(user, fundPrice);
        _fundTask.execute();
    }

    public class FundTask extends AsyncTask<String, Void, Double> {
        private final User _user;
        private final double _fundPrice;

        public FundTask(User user, double fundPrice) {
            _user = user;
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
                Toast.makeText(getActivity(), getString(R.string.fund_success), Toast.LENGTH_SHORT).show();

                NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
                nf.setMinimumFractionDigits(0);

                _fundValue.setText(nf.format(value));

                ((AuctionApplication) getActivity().getApplicationContext()).setFundValue(value);
            } else {
                Toast.makeText(getActivity(), getString(R.string.fund_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }
}

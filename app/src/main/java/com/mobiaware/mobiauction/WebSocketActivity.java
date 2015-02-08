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
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.mobiaware.mobiauction.funds.Fund;
import com.mobiaware.mobiauction.items.Item;
import com.mobiaware.mobiauction.items.ItemDataSource;
import com.mobiaware.mobiauction.users.User;

import org.json.JSONException;
import org.json.JSONObject;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public abstract class WebSocketActivity extends Activity {
    private static final String API_WS = "ws://mobiaware.com/liveauction/notify";

    private static enum MessageType {
        INVALID, ITEM, FUND, OUTBID
    }

    private WebSocketConnection _connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _connection = new WebSocketConnection();
    }

    @Override
    protected void onResume() {
        super.onResume();

        start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stop();
    }

    public void onItemMessageReceived() {
        // nothing
    }

    public void onFundMessageReceived() {
        // nothing
    }

    public void onOutbidMessageReceived() {
        Toast.makeText(this, "You have been outbid!", Toast.LENGTH_SHORT).show();
        onItemMessageReceived();
    }

    private void start() {
        try {
            _connection.connect(API_WS, new WebSocketHandler() {
                @Override
                public void onOpen() {
                    // TODO
                }

                @Override
                public void onTextMessage(String payload) {
                    TextMessageTask task = new TextMessageTask();
                    task.execute(payload);
                }

                @Override
                public void onClose(int code, String reason) {
                    // TODO
                }
            });
        } catch (WebSocketException e) {
            // TODO
        }
    }

    private void stop() {
        if (_connection.isConnected()) {
            _connection.disconnect();
        }
    }

    private class TextMessageTask extends AsyncTask<String, Void, MessageType> {
        @Override
        protected MessageType doInBackground(String... params) {
            try {
                JSONObject object = new JSONObject(params[0]);
                if (object.has("liveauction-item")) {
                    JSONObject json = object.getJSONObject("liveauction-item");

                    MessageType type = MessageType.ITEM;
                    ItemDataSource datasource = new ItemDataSource(getApplicationContext());

                    Item tmp = datasource.getItem(json.getLong("uid"));
                    if (tmp.isBidding()) {
                        User user = ((AuctionApplication) getApplication()).getUser();
                        boolean isWinningNow = user.getBidder().equals(tmp.getWinner());
                        boolean isWinningAfter = user.getBidder().equals(json.getString("winner"));
                        if (isWinningNow && !isWinningAfter) {

                            type = MessageType.OUTBID;
                        }
                    }

                    datasource.updateItem(json.getLong("uid"), json.getDouble("curPrice"),
                            json.getString("winner"), json.optLong("bidCount", 0), json.optLong("watchCount", 0));
                    return type;
                } else if (object.has("liveauction-fund")) {
                    JSONObject json = object.getJSONObject("liveauction-fund");
                    Fund fund = new Fund(1, json.getDouble("sum"), json.getString("name"));
                    getApplication();
                    ((AuctionApplication) getApplicationContext()).setFund(fund);
                    return MessageType.FUND;
                }
            } catch (JSONException e) {
                // TODO
            }
            return MessageType.INVALID;
        }

        @Override
        protected void onPostExecute(MessageType value) {
            switch (value) {
                case ITEM:
                    onItemMessageReceived();
                    break;
                case FUND:
                    onFundMessageReceived();
                    break;
                case OUTBID:
                    onOutbidMessageReceived();
                    break;
            }
        }
    }
}

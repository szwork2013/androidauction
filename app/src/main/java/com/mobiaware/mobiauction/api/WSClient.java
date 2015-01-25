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

package com.mobiaware.mobiauction.api;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.mobiaware.mobiauction.items.ItemDataSource;

import org.json.JSONException;
import org.json.JSONObject;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class WSClient {
    private static final String TAG = WSClient.class.getName();
    private static final String API_WS = "ws://mobiaware.com/liveauction/notify";

    private enum MessageType {
        INVALID, ITEM_MESSAGE, FUND_MESSAGE
    };

    private final WebSocketConnection _ws;
    private final ItemDataSource _datasource;
    private final OnMessageListener _listener;

    public static interface OnMessageListener {
        void onItemMessageReceived();
        void onFundMessageReceived();
    }

    public WSClient(Activity activity) {
        _ws = new WebSocketConnection();
        _datasource = new ItemDataSource(activity);

        try {
            _listener = (OnMessageListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement OnMessageListener.");
        }
    }

    public void start() {
        try {
            _ws.connect(API_WS, new WebSocketHandler() {
                @Override
                public void onOpen() {
                    Log.d(TAG, "Status: Connection opened." + API_WS);
                }

                @Override
                public void onTextMessage(String payload) {
                    MessageTask m = new MessageTask();
                    m.execute(payload);
                }

                @Override
                public void onClose(int code, String reason) {
                    Log.d(TAG, "Status: Connection closed.");
                }
            });
        } catch (WebSocketException e) {
            Log.e(TAG, "Error with websocket:", e);
        }
    }

    public void stop() {
        if (_ws.isConnected()) {
            _ws.disconnect();
        }
    }

    private class MessageTask extends AsyncTask<String, Void, MessageType> {
        @Override
        protected MessageType doInBackground(String... params) {
            try {
                JSONObject object = new JSONObject(params[0]);
                if (object.has("liveauction-item")) {
                    JSONObject item = object.getJSONObject("liveauction-item");
                    _datasource.updateItem(item.getLong("uid"), item.getDouble("curPrice"),
                            item.getString("winner"), item.optLong("bidCount", 0), item.optLong("watchCount", 0));
                    return MessageType.ITEM_MESSAGE;
                } else if (object.has("liveauction-fund")) {
                    JSONObject fund = object.getJSONObject("liveauction-fund");
                    // ((AuctionApplication) getApplicationContext()).setFundValue(value);
                    return MessageType.FUND_MESSAGE;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error with websocket:", e);
            }
            return MessageType.INVALID;
        }

        @Override
        protected void onPostExecute(MessageType value) {
            if (_listener != null) {
                switch (value) {
                    case INVALID:
                        break;
                    case ITEM_MESSAGE:
                        _listener.onItemMessageReceived();
                        break;
                    case FUND_MESSAGE:
                        _listener.onFundMessageReceived();
                        break;
                    default:
                        break;
                }
            }
        }
    }
}

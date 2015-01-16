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
import android.util.Log;

import com.mobiaware.mobiauction.items.Item;

import org.json.JSONArray;
import org.json.JSONObject;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class WSClient {
    private static final String TAG = WSClient.class.getName();
    private static final String API_WS = "ws://mobiaware.com/liveauction/notify";

    private final WebSocketConnection _ws;

    private WebsocketCallbacks _callbacks;

    public WSClient(Activity activity) {
        _ws = new WebSocketConnection();

        try {
            _callbacks = (WebsocketCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement WebsocketCallbacks.");
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
                    if (_callbacks != null) {
                        _callbacks.onItemMessage(payload);
                    }
                }

                @Override
                public void onClose(int code, String reason) {
                    Log.d(TAG, "Status: Connection closed.");
                }
            });
        } catch (WebSocketException e) {
            // ignore
        }
    }

    public void stop() {
        if (_ws.isConnected()) {
            _ws.disconnect();
        }
    }

    public static interface WebsocketCallbacks {
        void onItemMessage(String payload);
    }

}

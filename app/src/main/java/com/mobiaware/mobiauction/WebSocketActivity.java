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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.mobiaware.mobiauction.funds.Fund;
import com.mobiaware.mobiauction.items.Item;
import com.mobiaware.mobiauction.items.ItemDataSource;
import com.mobiaware.mobiauction.users.User;

import org.json.JSONException;
import org.json.JSONObject;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketOptions;

public abstract class WebSocketActivity extends Activity {
    private static final String TAG = WebSocketActivity.class.getName();

    private static final String API_WS = "ws://mobiaware.com/liveauction/notify";

    private static final int NOTIFICATION_ID = 1111;

    private static enum MessageType {
        INVALID, ITEM, FUND, OUTBID
    }

    private WebSocketConnection _connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebSocketOptions options = new WebSocketOptions();
        options.setReconnectInterval(60000);

        _connection = new WebSocketConnection();
        try {
            _connection.connect(API_WS, new WebSocketConnectionHandler() {
                @Override
                public void onTextMessage(String payload) {
                    TextMessageTask task = new TextMessageTask();
                    task.execute(payload);
                }
            }/*, options*/);
        } catch (WebSocketException e) {
            Log.e(TAG, "Unable to start websocket connection.", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if ((_connection != null) && (_connection.isConnected())) {
            _connection.disconnect();
            _connection = null;
        }
    }

    public void onItemMessageReceived() {
        // no default handler
    }

    public void onFundMessageReceived() {
        // no default handler
    }

    public void onOutbidMessageReceived() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(getString(R.string.message_outbid_title))
                        .setContentText(getString(R.string.message_outbid_content))
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        Intent intent =
                ItemListActivity.newInstance(getApplicationContext(), ItemListActivity.TYPE_MYITEMS, null);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ItemListActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

        Toast toast =
                Toast.makeText(getApplicationContext(), getString(R.string.message_outbid_generic),
                        Toast.LENGTH_LONG);
        //toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();

        onItemMessageReceived();
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
                Log.e(TAG, "Unable to parse websocket message.", e);
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

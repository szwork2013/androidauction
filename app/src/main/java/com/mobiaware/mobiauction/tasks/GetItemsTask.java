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

package com.mobiaware.mobiauction.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.mobiaware.mobiauction.api.RESTClient;
import com.mobiaware.mobiauction.items.ItemDataSource;
import com.mobiaware.mobiauction.users.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class GetItemsTask extends AsyncTask<String, Void, Boolean> {
    private final Context _context;
    private final User _user;

    public GetItemsTask(Context context, User user) {
        _context = context;
        _user = user;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try {
            fetchItems();
            fetchBids(_user.getBidder(), _user.getPassword());
            fetchWatches(_user.getBidder(), _user.getPassword());
        } catch (IOException e) {
            //Log.e(TAG, "Error fetching auction items.", e);
            return false;
        } catch (JSONException e) {
            //Log.e(TAG, "Error fetching auction items.", e);
            return false;
        }

        return true;
    }

    private void fetchItems() throws IOException, JSONException {
        ItemDataSource datasource = new ItemDataSource(_context);

        String response = RESTClient.get("/event/auctions/1/items", null);

        JSONArray array = new JSONArray(response);

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);

            datasource.createItem(object.getLong("uid"), object.getString("itemNumber"),
                    object.getString("name"), object.getString("description"),
                    object.getString("category"), object.getString("seller"), object.getDouble("valPrice"),
                    object.getDouble("minPrice"), object.getDouble("incPrice"),
                    object.getDouble("curPrice"), object.optString("winner", ""),
                    object.optLong("bidCount", 0), object.optLong("watchCount", 0),
                    object.optString("url", ""), object.getBoolean("multi"));
        }
    }

    private void fetchBids(String bidder, String password) throws IOException, JSONException {
        ItemDataSource datasource = new ItemDataSource(_context);

        String response = RESTClient.get("/live/bids", bidder, password);

        JSONArray array = new JSONArray(response);

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            datasource.setIsBidding(object.getLong("uid"));
        }
    }

    private void fetchWatches(String bidder, String password) throws IOException, JSONException {
        ItemDataSource datasource = new ItemDataSource(_context);

        String response = RESTClient.get("/live/watches", bidder, password);

        JSONArray array = new JSONArray(response);

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            datasource.setIsWatching(object.getLong("uid"));
        }
    }
}
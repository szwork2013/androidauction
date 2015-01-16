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
package com.mobiaware.mobiauction.background;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.mobiaware.mobiauction.api.RESTClient;
import com.mobiaware.mobiauction.items.ItemDataSource;
import com.mobiaware.mobiauction.users.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class FetchItemsTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = FetchItemsTask.class.getName();

    private final ProgressDialog _progressDlg;

    private ItemDataSource _datasource;
    private final User _user;

    public FetchItemsTask(Context context, ItemDataSource datasource, User user, String message) {
        _progressDlg = new ProgressDialog(context);
        _progressDlg.setMessage(message);

        _datasource = datasource;
        _user = user;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            fetchItems();
            fetchBids();
            fetchWatches();
        } catch (IOException e) {
            Log.e(TAG, "Error fetching auction items.", e);
            return false;
        } catch (JSONException e) {
            Log.e(TAG, "Error fetching auction items.", e);
            return false;
        }

        return true;
    }

    @Override
    protected void onPreExecute() {
        _progressDlg.show();
        _progressDlg.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (_progressDlg.isShowing()) {
            _progressDlg.dismiss();
        }
    }

    @Override
    protected void onCancelled() {
        if (_progressDlg.isShowing()) {
            _progressDlg.dismiss();
        }
    }

    private void fetchItems() throws IOException, JSONException {
        String response = RESTClient.get("/event/auctions/1/items", null);

        JSONArray array = new JSONArray(response);

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);

            _datasource.createItem(object.getLong("uid"), object.getString("itemNumber"),
                    object.getString("name"), object.getString("description"), object.getString("category"),
                    object.getString("seller"), object.getDouble("valPrice"), object.getDouble("minPrice"),
                    object.getDouble("incPrice"), object.getDouble("curPrice"),
                    object.optString("winner", ""), object.optLong("bidCount", 0),
                    object.optLong("watchCount", 0), object.optString("url", ""), object.getBoolean("multi"));
        }
    }

    private void fetchBids() throws IOException, JSONException {
        String response = RESTClient.get("/live/bids", _user);

        JSONArray array = new JSONArray(response);

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            _datasource.setIsBidding(object.getLong("uid"));
        }
    }

    private void fetchWatches() throws IOException, JSONException {
        String response = RESTClient.get("/live/watches", _user);

        JSONArray array = new JSONArray(response);

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            _datasource.setIsWatching(object.getLong("uid"));
        }
    }
}

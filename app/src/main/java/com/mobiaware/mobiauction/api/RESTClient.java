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

import android.text.TextUtils;
import android.util.Base64;

import com.mobiaware.mobiauction.users.User;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class RESTClient {
    private static final String API_HOST = "http://mobiaware.com/liveauction";

    private RESTClient() {
        // static only
    }

    public static String get(String path, User user) throws IOException {
        String bidder = null;
        String password = null;

        if (user != null) {
            bidder = user.getBidder();
            password = user.getPassword();
        }

        return get(path, bidder, password);
    }

    public static String get(String path, String bidder, String password) throws IOException {
        HttpGet httpRequest = new HttpGet(API_HOST + path);
        return execute(httpRequest, bidder, password);
    }

    public static String post(String path, User user, String json) throws IOException {
        String bidder = null;
        String password = null;

        if (user != null) {
            bidder = user.getBidder();
            password = user.getPassword();
        }

        return post(path, bidder, password, json);
    }

    public static String post(String path, String bidder, String password, String json)
            throws IOException {
        HttpPost httpRequest = new HttpPost(API_HOST + path);

        if (!TextUtils.isEmpty(json)) {
            StringEntity entity = new StringEntity(json);
            entity.setContentType("application/json");
            httpRequest.setEntity(entity);
        }

        return execute(httpRequest, bidder, password);
    }

    private static String execute(HttpUriRequest httpRequest, String bidder, String password)
            throws IOException {
        httpRequest.addHeader("accept", "application/json");

        if (!TextUtils.isEmpty(bidder) || !TextUtils.isEmpty(password)) {
            String credentials = bidder + ":" + password;
            httpRequest.addHeader("Authorization",
                    "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP));
        }

        HttpClient httpClient = new DefaultHttpClient();
        return httpClient.execute(httpRequest, new BasicResponseHandler());
    }
}

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
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mobiaware.mobiauction.api.RESTClient;
import com.mobiaware.mobiauction.users.User;
import com.mobiaware.mobiauction.users.UserDataSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends Activity {
    private UserLoginTask _authTask = null;

    private EditText _bidderView;
    private EditText _passwordView;

    private UserDataSource _userDatasource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _userDatasource = new UserDataSource(this);
        _userDatasource.open();

        setContentView(R.layout.activity_login);

        _bidderView = (EditText) findViewById(R.id.bidder);

        _passwordView = (EditText) findViewById(R.id.password);
        _passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button bidderSignInButton = (Button) findViewById(R.id.bidder_sign_in_button);
        bidderSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    @Override
    protected void onResume() {
        _userDatasource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        _userDatasource.close();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        _userDatasource.close();
        super.onDestroy();
    }

    public void attemptLogin() {
        if (_authTask != null) {
            return;
        }

        _bidderView.setError(null);
        _passwordView.setError(null);

        String bidder = _bidderView.getText().toString();
        String password = _passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            _passwordView.setError(getString(R.string.error_invalid_password));
            focusView = _passwordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(bidder)) {
            _bidderView.setError(getString(R.string.error_field_required));
            focusView = _bidderView;
            cancel = true;
        } else if (!isBidderValid(bidder)) {
            _bidderView.setError(getString(R.string.error_invalid_bidder));
            focusView = _bidderView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            _authTask = new UserLoginTask(this, bidder, password);
            _authTask.execute((Void) null);
        }
    }

    private boolean isBidderValid(String bidder) {
        return TextUtils.isDigitsOnly(bidder);
    }

    private boolean isPasswordValid(String password) {
        return true;
    }

    public class UserLoginTask extends AsyncTask<Void, Void, User> {
        private ProgressDialog _progressDlg;

        private final String _bidder;
        private final String _password;

        UserLoginTask(LoginActivity activity, String bidder, String password) {
            _progressDlg = new ProgressDialog(activity);

            _bidder = bidder;
            _password = password;
        }

        @Override
        protected User doInBackground(Void... params) {
            User user = null;

            try {
                String response = RESTClient.post("/live/sessions", _bidder, _password, null);

                JSONObject object = new JSONObject(response);

                user =
                        _userDatasource.createUser(object.getLong("uid"), object.getLong("auctionUid"),
                                _bidder, _password, object.getString("firstName"), object.getString("lastName"));

            } catch (IOException e) {
                user = null;
            } catch (JSONException e) {
                user = null;
            }

            return user;
        }

        @Override
        protected void onPreExecute() {
            _progressDlg.setMessage(getString(R.string.progresss_authenticating));
            _progressDlg.show();
            _progressDlg.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPostExecute(User user) {
            _authTask = null;

            if (_progressDlg.isShowing()) {
                _progressDlg.dismiss();
            }

            if (user != null) {
                Intent intent = new Intent(getApplicationContext(), AuctionActivity.class);
                intent.putExtra(AuctionApplication.ARG_USER, user);
                startActivity(intent);

                finish();
            } else {
                _passwordView.setError(getString(R.string.error_incorrect_password));
                _passwordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            _authTask = null;

            if (_progressDlg.isShowing()) {
                _progressDlg.dismiss();
            }
        }
    }
}
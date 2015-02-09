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
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mobiaware.mobiauction.api.RESTClient;
import com.mobiaware.mobiauction.tasks.GetItemsTask;
import com.mobiaware.mobiauction.users.User;
import com.mobiaware.mobiauction.utils.Preconditions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends Activity {
    private static final String TAG = LoginActivity.class.getName();

    private EditText _bidderView;
    private EditText _passwordView;

    private ProgressDialog _progressDlg;

    private LoginTask _loginTask;

    public static Intent newInstance(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    void attemptLogin() {
        if (_loginTask != null) {
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
            showProgress();
            _loginTask = new LoginTask();
            _loginTask.execute(bidder, password);
        }
    }

    private boolean isBidderValid(String bidder) {
        return true;
    }

    private boolean isPasswordValid(String password) {
        return true;
    }

    private void showProgress() {
        hideProgress();

        _progressDlg = new ProgressDialog(this);
        _progressDlg.setMessage(getString(R.string.progresss_authenticating));
        _progressDlg.setIndeterminate(true);
        _progressDlg.show();
    }

    private void hideProgress() {
        if (_progressDlg != null && _progressDlg.isShowing()) {
            _progressDlg.dismiss();
            _progressDlg = null;
        }
    }

    private class LoginTask extends AsyncTask<String, Void, User> {
        @Override
        protected User doInBackground(String... params) {
            Preconditions
                    .checkArgument(params.length == 2, "Requires bidder and password as parameters.");

            try {
                String response = RESTClient.post("/live/sessions", params[0], params[1], null);

                JSONObject object = new JSONObject(response);

                return new User(object.getLong("uid"), object.getLong("auctionUid"), params[0], params[1],
                        object.getString("firstName"), object.getString("lastName"));

            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error authenticating bidder..", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            _loginTask = null;
            hideProgress();

            if (user != null) {
                ((AuctionApplication) getApplication()).setUser(user);

                GetItemsTask getItemsTask = new GetItemsTask(getApplicationContext(), user);
                getItemsTask.execute();

                startActivity(AuctionActivity.newInstance(getApplicationContext()));
                finish();
            } else {
                _passwordView.setError(getString(R.string.error_incorrect_password));
                _passwordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            _loginTask = null;
            hideProgress();
        }
    }
}

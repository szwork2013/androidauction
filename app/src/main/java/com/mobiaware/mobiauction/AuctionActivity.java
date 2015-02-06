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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mobiaware.mobiauction.users.User;

public class AuctionActivity extends Activity {
    public static Intent newInstance(Context context) {
        return new Intent(context, AuctionActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auction);

        User user = ((AuctionApplication) getApplicationContext()).getUser();

        ((TextView) findViewById(R.id.textWelcome)).setText(String.format(
                getString(R.string.label_welcome), user.getFirstName()));
        ((TextView) findViewById(R.id.textBidderNumber)).setText(String.format(
                getString(R.string.label_bidder), user.getBidder()));

        findViewById(R.id.btnItems).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ItemListActivity.newInstance(getApplicationContext(), 0, null));
            }
        });

        findViewById(R.id.btnMyItems).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ItemListActivity.newInstance(getApplicationContext(), 1, null));
            }
        });

        findViewById(R.id.btnLowBids).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ItemListActivity.newInstance(getApplicationContext(), 2, null));
            }
        });

        findViewById(R.id.btnFund).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(FundActivity.newInstance(getApplicationContext()));
            }
        });
    }
}

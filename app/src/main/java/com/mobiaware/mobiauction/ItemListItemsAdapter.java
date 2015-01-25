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

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobiaware.mobiauction.items.Item;
import com.mobiaware.mobiauction.items.ItemDataSource;
import com.mobiaware.mobiauction.users.User;
import com.mobiaware.mobiauction.utils.FormatUtils;

import java.text.NumberFormat;
import java.util.Locale;

public class ItemListItemsAdapter extends CursorAdapter {
    private final User _user;
    private final String[] _labels;

    public ItemListItemsAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);

        _user = ((AuctionApplication) context.getApplicationContext()).getActiveUser();
        _labels = context.getResources().getStringArray(R.array.label_bid_count);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.fragment_item_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Item item = ItemDataSource.cursorToItem(cursor);

        ((TextView) view.findViewById(R.id.itemName)).setText(item.getName());
        ((TextView) view.findViewById(R.id.itemNumber)).setText(item.getNumber());

        long bids = item.getBidCount();
        if (bids == 0) {
            ((TextView) view.findViewById(R.id.itemBids)).setText(_labels[0]);
        } else if (bids == 1) {
            ((TextView) view.findViewById(R.id.itemBids)).setText(_labels[1]);
        } else {
            ((TextView) view.findViewById(R.id.itemBids)).setText(String.format(_labels[2],
                    Long.toString(item.getBidCount())));
        }

        ((TextView) view.findViewById(R.id.itemPrice)).setText(FormatUtils.valueToString(item.getCurPrice()));
        ((TextView) view.findViewById(R.id.itemPrice)).setTextColor(Color.rgb(0, 102, 0));

        if (item.isBidding()) {
            if (item.getWinner().equals(_user.getBidder())) {
                view.findViewById(R.id.itemWinning).setVisibility(ImageView.VISIBLE);
                view.findViewById(R.id.itemLosing).setVisibility(ImageView.INVISIBLE);
                view.findViewById(R.id.itemFavorite).setVisibility(ImageView.INVISIBLE);
            } else {
                view.findViewById(R.id.itemWinning).setVisibility(ImageView.INVISIBLE);
                view.findViewById(R.id.itemLosing).setVisibility(ImageView.VISIBLE);
                view.findViewById(R.id.itemFavorite).setVisibility(ImageView.INVISIBLE);
            }
        } else if (item.isWatching()) {
            view.findViewById(R.id.itemWinning).setVisibility(ImageView.INVISIBLE);
            view.findViewById(R.id.itemLosing).setVisibility(ImageView.INVISIBLE);
            view.findViewById(R.id.itemFavorite).setVisibility(ImageView.VISIBLE);
        } else {
            view.findViewById(R.id.itemWinning).setVisibility(ImageView.INVISIBLE);
            view.findViewById(R.id.itemLosing).setVisibility(ImageView.INVISIBLE);
            view.findViewById(R.id.itemFavorite).setVisibility(ImageView.INVISIBLE);
        }
    }
}

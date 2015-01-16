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
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobiaware.mobiauction.items.Item;
import com.mobiaware.mobiauction.users.User;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ItemListItemsAdapter extends ArrayAdapter<Item> {
    private final User _user;
    private final String[] _bidLabels;

    public ItemListItemsAdapter(Context context, ArrayList<Item> items, User user) {
        super(context, 0, items);

        _user = user;
        _bidLabels = context.getResources().getStringArray(R.array.label_bid_count);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = getItem(position);

        if (convertView == null) {
            convertView =
                    LayoutInflater.from(getContext()).inflate(R.layout.item_list_item, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.itemName)).setText(item.getName());
        ((TextView) convertView.findViewById(R.id.itemNumber)).setText(item.getNumber());

        long bids = item.getBidCount();
        if (bids == 0) {
            ((TextView) convertView.findViewById(R.id.itemBids)).setText(_bidLabels[0]);
        } else if (bids == 1) {
            ((TextView) convertView.findViewById(R.id.itemBids)).setText(_bidLabels[1]);
        } else {
            ((TextView) convertView.findViewById(R.id.itemBids)).setText(String.format(_bidLabels[2],
                    Long.toString(item.getBidCount())));
        }

        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        ((TextView) convertView.findViewById(R.id.itemPrice))
                .setText(format.format(item.getCurPrice()));
        ((TextView) convertView.findViewById(R.id.itemPrice)).setTextColor(Color.rgb(0, 102, 0));

        if (item.isBidding()) {
            if (item.getWinner().equals(_user.getBidder())) {
                convertView.findViewById(R.id.itemWinning).setVisibility(ImageView.VISIBLE);
                convertView.findViewById(R.id.itemLosing).setVisibility(ImageView.INVISIBLE);
                convertView.findViewById(R.id.itemFavorite).setVisibility(ImageView.INVISIBLE);
            } else {
                convertView.findViewById(R.id.itemWinning).setVisibility(ImageView.INVISIBLE);
                convertView.findViewById(R.id.itemLosing).setVisibility(ImageView.VISIBLE);
                convertView.findViewById(R.id.itemFavorite).setVisibility(ImageView.INVISIBLE);
            }
        } else if (item.isWatching()) {
            convertView.findViewById(R.id.itemWinning).setVisibility(ImageView.INVISIBLE);
            convertView.findViewById(R.id.itemLosing).setVisibility(ImageView.INVISIBLE);
            convertView.findViewById(R.id.itemFavorite).setVisibility(ImageView.VISIBLE);
        } else {
            convertView.findViewById(R.id.itemWinning).setVisibility(ImageView.INVISIBLE);
            convertView.findViewById(R.id.itemLosing).setVisibility(ImageView.INVISIBLE);
            convertView.findViewById(R.id.itemFavorite).setVisibility(ImageView.INVISIBLE);
        }

        return convertView;
    }
}

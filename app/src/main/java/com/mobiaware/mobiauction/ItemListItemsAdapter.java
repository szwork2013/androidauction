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
import android.text.TextUtils;
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
import com.squareup.picasso.Picasso;

public class ItemListItemsAdapter extends CursorAdapter {
    static class ViewHolder {
        TextView itemName;
        TextView itemNumber;
        TextView itemBids;
        TextView itemPrice;
        ImageView itemWinning;
        ImageView itemLosing;
        ImageView itemFavorite;
        ImageView itemImage;

        String[] bidLabels;
    }

    private ViewHolder _viewHolder;

    public ItemListItemsAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view =
                LayoutInflater.from(context).inflate(R.layout.activity_item_list_item, parent, false);

        _viewHolder = new ViewHolder();
        _viewHolder.itemName = (TextView) view.findViewById(R.id.itemName);
        _viewHolder.itemNumber = (TextView) view.findViewById(R.id.itemNumber);
        _viewHolder.itemBids = (TextView) view.findViewById(R.id.itemBids);
        _viewHolder.itemPrice = (TextView) view.findViewById(R.id.itemPrice);
        _viewHolder.itemWinning = (ImageView) view.findViewById(R.id.itemWinning);
        _viewHolder.itemLosing = (ImageView) view.findViewById(R.id.itemLosing);
        _viewHolder.itemFavorite = (ImageView) view.findViewById(R.id.itemFavorite);
        _viewHolder.itemImage = (ImageView) view.findViewById(R.id.itemImage);
        _viewHolder.bidLabels = context.getResources().getStringArray(R.array.label_bid_count);
        view.setTag(_viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        Item item = ItemDataSource.cursorToItem(cursor);

        viewHolder.itemName.setText(item.getName());
        viewHolder.itemNumber.setText(item.getNumber());

        long bids = item.getBidCount();
        if (bids == 0) {
            viewHolder.itemBids.setText(_viewHolder.bidLabels[0]);
        } else if (bids == 1) {
            viewHolder.itemBids.setText(_viewHolder.bidLabels[1]);
        } else {
            viewHolder.itemBids.setText(String.format(_viewHolder.bidLabels[2],
                    Long.toString(item.getBidCount())));
        }

        viewHolder.itemPrice.setText(FormatUtils.valueToString(item.getCurPrice()));
        viewHolder.itemPrice.setTextColor(Color.rgb(0, 102, 0));

        if (item.isBidding()) {
            User user = ((AuctionApplication) context.getApplicationContext()).getUser();
            if (item.getWinner().equals(user.getBidder())) {
                viewHolder.itemWinning.setVisibility(ImageView.VISIBLE);
                viewHolder.itemLosing.setVisibility(ImageView.INVISIBLE);
                viewHolder.itemFavorite.setVisibility(ImageView.INVISIBLE);
            } else {
                viewHolder.itemWinning.setVisibility(ImageView.INVISIBLE);
                viewHolder.itemLosing.setVisibility(ImageView.VISIBLE);
                viewHolder.itemFavorite.setVisibility(ImageView.INVISIBLE);
            }
        } else if (item.isWatching()) {
            viewHolder.itemWinning.setVisibility(ImageView.INVISIBLE);
            viewHolder.itemLosing.setVisibility(ImageView.INVISIBLE);
            viewHolder.itemFavorite.setVisibility(ImageView.VISIBLE);
        } else {
            viewHolder.itemWinning.setVisibility(ImageView.INVISIBLE);
            viewHolder.itemLosing.setVisibility(ImageView.INVISIBLE);
            viewHolder.itemFavorite.setVisibility(ImageView.INVISIBLE);
        }

        if (TextUtils.isEmpty(item.getUrl())) {
            viewHolder.itemImage.setImageResource(R.drawable.ic_nophoto);
        } else {
            viewHolder.itemImage.setImageResource(R.drawable.ic_nophoto);
            //Picasso.with(context).load(item.getUrl()).into(viewHolder.itemImage);
        }
    }
}

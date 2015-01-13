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

package com.mobiaware.mobiauction.items;

import android.os.Parcel;
import android.os.Parcelable;

final public class Item implements Parcelable {
    private final long _id;
    private final String _number;
    private final String _name;
    private final String _description;
    private final String _category;
    private final String _seller;
    private final double _valPrice;
    private final double _minPrice;
    private final double _incPrice;
    private final double _curPrice;
    private final String _winner;
    private final long _bidCount;
    private final long _watchCount;
    private final String _url;
    private final boolean _multi;

    public Item(long id, String number, String name, String description, String category,
                String seller, double valPrice, double minPrice, double incPrice, double curPrice,
                String winner, long bidCount, long watchCount, String url, boolean multi) {
        _id = id;
        _number = number;
        _name = name;
        _description = description;
        _category = category;
        _seller = seller;
        _valPrice = valPrice;
        _minPrice = minPrice;
        _incPrice = incPrice;
        _curPrice = curPrice;
        _winner = winner;
        _bidCount = bidCount;
        _watchCount = watchCount;
        _url = url;
        _multi = multi;
    }

    public Item(Parcel in) {
        this(in.readLong(), in.readString(), in.readString(), in.readString(), in.readString(), in
                .readString(), in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble(), in
                .readString(), in.readLong(), in.readLong(), in.readString(), in.readLong() == 0);
    }

    public long getId() {
        return _id;
    }

    public String getNumber() {
        return _number;
    }

    public String getName() {
        return _name;
    }

    public String getDescription() {
        return _description;
    }

    public String getCategory() {
        return _category;
    }

    public String getSeller() {
        return _seller;
    }

    public double getValPrice() {
        return _valPrice;
    }

    public double getMinPrice() {
        return _minPrice;
    }

    public double getIncPrice() {
        return _incPrice;
    }

    public double getCurPrice() {
        return _curPrice;
    }

    public String getWinner() {
        return _winner;
    }

    public long getBidCount() {
        return _bidCount;
    }

    public long getWatchCount() {
        return _watchCount;
    }

    public String getUrl() {
        return _url;
    }

    public boolean getMulti() {
        return _multi;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(_id);
        out.writeString(_number);
        out.writeString(_name);
        out.writeString(_description);
        out.writeString(_category);
        out.writeString(_seller);
        out.writeDouble(_valPrice);
        out.writeDouble(_minPrice);
        out.writeDouble(_incPrice);
        out.writeDouble(_curPrice);
        out.writeString(_winner);
        out.writeLong(_bidCount);
        out.writeLong(_watchCount);
        out.writeString(_url);
        out.writeLong(_multi ? 1 : 0);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
}

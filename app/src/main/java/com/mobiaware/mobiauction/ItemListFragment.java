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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;

import com.mobiaware.mobiauction.items.Item;
import com.mobiaware.mobiauction.users.User;

import java.util.ArrayList;

public class ItemListFragment extends Fragment {
    private AbsListView _listView;

    private ListAdapter _adapter;

    public static ItemListFragment newInstance(User user, ArrayList<Item> items) {
        ItemListFragment fragment = new ItemListFragment();
        Bundle args = new Bundle();
        args.putParcelable(AuctionApplication.ARG_USER, user);
        args.putParcelableArrayList(AuctionApplication.ARG_ITEMS, items);
        fragment.setArguments(args);
        return fragment;
    }

    public ItemListFragment() {
        // empty
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        User user;
        ArrayList<Item> items;

        if (getArguments() != null) {
            user = getArguments().getParcelable(AuctionApplication.ARG_USER);
            items = getArguments().getParcelableArrayList(AuctionApplication.ARG_ITEMS);
        } else {
            user = null;
            items = new ArrayList<>();
        }

        _adapter = new ItemListItemsAdapter(getActivity(), items, user);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        _listView = (AbsListView) view.findViewById(android.R.id.list);
        _listView.setEmptyView(view.findViewById(android.R.id.empty));
        _listView.setAdapter(_adapter);

        return view;
    }
}

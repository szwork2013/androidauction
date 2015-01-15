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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.mobiaware.mobiauction.items.Item;
import com.mobiaware.mobiauction.users.User;

import java.util.ArrayList;

public class ItemListFragment extends Fragment implements AbsListView.OnItemClickListener {
    private OnFragmentInteractionListener _listener;

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
        _listView.setAdapter(_adapter);
        _listView.setOnItemClickListener(this);

        setEmptyText(getString(R.string.list_no_results));

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            _listener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _listener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != _listener) {
            // TODO: item click
        }
    }

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = _listView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }
}

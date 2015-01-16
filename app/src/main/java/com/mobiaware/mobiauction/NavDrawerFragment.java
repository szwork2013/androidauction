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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class NavDrawerFragment extends Fragment implements SearchView.OnQueryTextListener {
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private NavigationDrawerCallbacks _callbacks;

    private ActionBarDrawerToggle _drawerToggle;

    private DrawerLayout _drawerLayout;
    private ListView _drawerListView;
    private View _fragmentContainerView;

    private int _currentSelectedPosition = 0;
    private boolean _fromSavedInstanceState;
    private boolean _userLearnedDrawer;

    private SearchView _searchView;

    private NavDrawerItemsAdapter adapter;


    public NavDrawerFragment() {
        // empty
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        _userLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            _currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            _fromSavedInstanceState = true;
        }

        selectItem(_currentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _drawerListView =
                (ListView) inflater.inflate(R.layout.fragment_nav_drawer, container, false);
        _drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<>();
        navDrawerItems.add(new NavDrawerItem(getString(R.string.title_listitems), R.drawable.ic_items));
        navDrawerItems.add(new NavDrawerItem(getString(R.string.title_listmyitems), R.drawable.ic_myitems));
        navDrawerItems.add(new NavDrawerItem(getString(R.string.title_listlowbids), R.drawable.ic_lowitems));

        adapter = new NavDrawerItemsAdapter(getActivity(),
                navDrawerItems);
        _drawerListView.setAdapter(adapter);

        _drawerListView.setItemChecked(_currentSelectedPosition, true);
        return _drawerListView;
    }

    public boolean isDrawerOpen() {
        return _drawerLayout != null && _drawerLayout.isDrawerOpen(_fragmentContainerView);
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        _fragmentContainerView = getActivity().findViewById(fragmentId);
        _drawerLayout = drawerLayout;

        _drawerLayout.setDrawerShadow(R.drawable.ic_drawer_shadow, GravityCompat.START);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        _drawerToggle =
                new ActionBarDrawerToggle(getActivity(), _drawerLayout, R.drawable.ic_drawer,
                        R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                        if (!isAdded()) {
                            return;
                        }

                        getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                        if (!isAdded()) {
                            return;
                        }

                        if (!_userLearnedDrawer) {
                            _userLearnedDrawer = true;
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                        }

                        getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                    }
                };

        if (!_userLearnedDrawer && !_fromSavedInstanceState) {
            _drawerLayout.openDrawer(_fragmentContainerView);
        }

        _drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                _drawerToggle.syncState();
            }
        });

        _drawerLayout.setDrawerListener(_drawerToggle);
    }

    private void selectItem(int position) {
        _currentSelectedPosition = position;
        if (_drawerListView != null) {
            _drawerListView.setItemChecked(position, true);
        }
        if (_drawerLayout != null) {
            _drawerLayout.closeDrawer(_fragmentContainerView);
        }
        if (_callbacks != null) {
            _callbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            _callbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _callbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, _currentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        _drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (_drawerLayout != null && isDrawerOpen()) {
            showGlobalContextActionBar();
        }

        inflater.inflate(R.menu.options_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        _searchView = (SearchView) menuItem.getActionView();
        _searchView.setOnQueryTextListener(this);
        _searchView.setIconifiedByDefault(true);
        _searchView.setIconified(true);
        _searchView.setFocusable(false);
        _searchView.setFocusableInTouchMode(true);
        _searchView.clearFocus();

        _searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (_callbacks != null) {
                    _callbacks.onNavigationDrawerSearch(null);
                }
                return false;
            }
        });



        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (_drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        if (_callbacks != null) {
            _callbacks.onNavigationDrawerSearch(s);
        }
        _searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return true;
    }

    public static interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(int position);

        void onNavigationDrawerSearch(String search);
    }
}

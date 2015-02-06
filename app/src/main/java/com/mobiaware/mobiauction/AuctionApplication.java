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

import android.app.Application;

import com.mobiaware.mobiauction.funds.Fund;
import com.mobiaware.mobiauction.funds.FundDataSource;
import com.mobiaware.mobiauction.users.User;
import com.mobiaware.mobiauction.users.UserDataSource;

public class AuctionApplication extends Application {
    private User _user;
    private Fund _fund;

    public User getUser() {
        if (_user == null) {
            readUser();
        }
        return _user;
    }

    public void setUser(User user) {
        _user = user;
        if (_user != null) {
            saveUser();
        }
    }

    private void readUser() {
        UserDataSource datasource = null;
        try {
            datasource = new UserDataSource(this);
            datasource.open();

            _user = datasource.getUser();
        } finally {
            if (datasource != null) {
                datasource.close();
            }
        }
    }

    private void saveUser() {
        UserDataSource datasource = null;
        try {
            datasource = new UserDataSource(this);
            datasource.open();

            /* ignore failure */
            datasource.setUser(_user);
        } finally {
            if (datasource != null) {
                datasource.close();
            }
        }
    }

    public Fund getFund() {
        if (_fund == null) {
            readFund();
        }
        return _fund;
    }

    public void setFund(Fund fund) {
        _fund = fund;
        if (_fund != null) {
            saveFund();
        }
    }

    private void readFund() {
        FundDataSource datasource = null;
        try {
            datasource = new FundDataSource(this);
            datasource.open();

            _fund = datasource.getFund();
        } finally {
            if (datasource != null) {
                datasource.close();
            }
        }
    }

    private void saveFund() {
        FundDataSource datasource = null;
        try {
            datasource = new FundDataSource(this);
            datasource.open();

            /* ignore failure */
            datasource.setFund(_fund);
        } finally {
            if (datasource != null) {
                datasource.close();
            }
        }
    }
}

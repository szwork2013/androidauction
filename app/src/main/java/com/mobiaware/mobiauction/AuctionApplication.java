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

import com.mobiaware.mobiauction.users.User;
import com.mobiaware.mobiauction.users.UserDataSource;

class AuctionApplication extends Application {
    private User _user;
    private double _fundValue;

    public User getActiveUser() {
        if (_user == null) {
            UserDataSource datasource = null;
            try {
                datasource = new UserDataSource(this);
                datasource.open();

                _user = datasource.getActiveUser();
            } finally {
                if (datasource != null) {
                    datasource.close();
                }
            }
        }
        return _user;
    }

    public void setActiveUser(User user) {
        _user = user;

        UserDataSource datasource = null;
        try {
            datasource = new UserDataSource(this);
            datasource.open();

      /* ignore failure */
            datasource.setActiveUser(user);
        } finally {
            if (datasource != null) {
                datasource.close();
            }
        }
    }

    public double getFundValue() {
        return _fundValue;
    }

    public void setFundValue(double fundValue) {
        _fundValue = fundValue;
    }
}

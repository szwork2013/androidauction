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

package com.mobiaware.mobiauction.users;

final public class User {
    private final long _uid;
    private final long _auction;
    private final String _bidder;
    private final String _password;
    private final String _firstName;
    private final String _lastName;

    public User(long uid, long auction, String bidder, String password, String firstName,
                String lastName) {
        _uid = uid;
        _auction = auction;
        _bidder = bidder;
        _password = password;
        _firstName = firstName;
        _lastName = lastName;
    }

    public long getUid() {
        return _uid;
    }

    public long getAuction() {
        return _auction;
    }

    public String getBidder() {
        return _bidder;
    }

    public String getPassword() {
        return _password;
    }

    public String getFirstName() {
        return _firstName;
    }

    public String getLastName() {
        return _lastName;
    }
}

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

package com.mobiaware.mobiauction.funds;

final public class Fund {
    private final long _auction;
    private final double _value;
    private final String _name;

    public Fund(long auction, double value, String name) {
        _auction = auction;
        _value = value;
        _name = name;
    }

    public long getAuction() {
        return _auction;
    }

    public double getValue() {
        return _value;
    }

    public String getName() {
        return _name;
    }
}
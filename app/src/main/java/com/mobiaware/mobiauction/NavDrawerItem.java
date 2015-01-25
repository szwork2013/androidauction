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

public class NavDrawerItem {
    private final String _title;
    private final int _icon;

    public NavDrawerItem(String title, int icon) {
        _title = title;
        _icon = icon;
    }

    public String getTitle() {
        return _title;
    }

    public int getIcon() {
        return _icon;
    }
}

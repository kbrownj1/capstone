/*
 * Licensed under Apache License, Version 2.0 or LGPL 2.1, at your option.
 * --
 *
 * Copyright 2010 Rene Treffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * --
 *
 * Copyright (C) 2010 Rene Treffer
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.example.capstone;

import android.database.DataSetObserver;
import android.widget.ListView;

/**
 * A simple observer to scroll to the bottom of the list on invalidate,
 * in case the list was at the bottem. This is especially usefull when an
 * on screen keyboard pops up.
 */
public class AutoscrollDataSetObserver extends DataSetObserver {

    /**
     * The list view that is observed.
     */
    private final ListView view;

    public AutoscrollDataSetObserver(ListView view) {
        this.view = view;
    }

    /**
     * Triggers a scroll event on change. if needed.
     */
    @Override
    public void onChanged() {
        onInvalidated();
    }

    /**
     * Triggers a scroll event if needed.
     */
    @Override
    public void onInvalidated() {
        int position = this.view.getLastVisiblePosition();
        if (position + 2 == this.view.getCount()) {
            this.view.smoothScrollToPosition(position + 1);
        }
    }

}

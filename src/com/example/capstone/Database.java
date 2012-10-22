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


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

/**
 * Database helper to ensure that there is just one sqlite database.
 */
public class Database {

    /**
     * The internal sqlite database instance.
     */
    private static SQLiteDatabase DATABASE = null;

    /**
     * Retrieve a sqlite database instance, shared between all clients.
     * @param context The context to use for opening the database.
     * @param factory A cursor factory.
     * @return A SQLiteDatabase instance of the messages database.
     */
    public static synchronized SQLiteDatabase getDatabase(
        Context context,
        CursorFactory factory
    ) {
        if (DATABASE == null) {
        	
        	Log.i("MMMC", "Database does not exist creating database messages");
        	
            DatabaseOpenHelper helper =
                    new DatabaseOpenHelper(context, "messages", factory);
            DATABASE = helper.getWritableDatabase();
        }
        return DATABASE;
    }

}

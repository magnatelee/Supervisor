/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.demo.notepad3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class NotesDbAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table notes (_id integer primary key autoincrement, "
        + "title text not null, body text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {
    	
        DatabaseHelper(Context context) {
        	super(context, DATABASE_NAME, null, DATABASE_VERSION);
        	
        	//INSTRUMENTED
        	Supervisor.logCall("DatabaseHelper", this);
            
        	//INSTRUMENTED
        	Supervisor.logReturn("DatabaseHelper", this);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	//INSTRUMENTED
        	Supervisor.logCall("onCreate", this);

            db.execSQL(DATABASE_CREATE);
            
            //INSTRUMENTED
        	Supervisor.logReturn("onCreate", this);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	//INSTRUMENTED
        	Supervisor.logCall("onUpgrade", this);

            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
            
            //INSTRUMENTED
        	Supervisor.logReturn("onUpgrade", this);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public NotesDbAdapter(Context ctx) {
    	//INSTRUMENTED
    	Supervisor.logCall("NotesDbAdapter", this);
    	
        this.mCtx = ctx;
        
    	//INSTRUMENTED
    	Supervisor.logReturn("NotesDbAdapter", this);
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public NotesDbAdapter open() throws SQLException {
    	//INSTRUMENTED
    	Supervisor.logCall("open", this);
    	
    	mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        
    	//INSTRUMENTED
    	Supervisor.logReturn("open", this);
    	
        return this;
    }

    public void close() {
    	//INSTRUMENTED
    	Supervisor.logCall("close", this);
    	
        mDbHelper.close();
        
    	//INSTRUMENTED
    	Supervisor.logReturn("close", this);
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    public long createNote(String title, String body) {
    	//INSTRUMENTED
    	Supervisor.logCall("createNote", this);
    	
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);

    	//INSTRUMENTED
    	Supervisor.logReturn("createNote", this);
        
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {
    	//INSTRUMENTED
    	Supervisor.logCall("deleteNote", this);

    	//INSTRUMENTED
    	Supervisor.logReturn("deleteNote", this);

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;

    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotes() {
    	//INSTRUMENTED
    	Supervisor.logCall("fetchAllNotes", this);
    	
    	Cursor temp = mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_BODY}, null, null, null, null, null); 
    	
    	//INSTRUMENTED
    	Supervisor.logReturn("fetchAllNotes", this);
    	
        return temp;
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNote(long rowId) throws SQLException {

    	//INSTRUMENTED
    	Supervisor.logCall("fetchNote", this);
    	
        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_TITLE, KEY_BODY}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
        	Supervisor.logTrue("fetchNote", this);
            mCursor.moveToFirst();
        }
        
    	//INSTRUMENTED
    	Supervisor.logReturn("fetchNote", this);

        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateNote(long rowId, String title, String body) {
    	//INSTRUMENTED
    	Supervisor.logCall("updateNote", this);

        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);

        boolean temp = mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
        
    	//INSTRUMENTED
    	Supervisor.logReturn("updateNote", this);
        
        return temp;
    }
}

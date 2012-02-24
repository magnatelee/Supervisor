/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.demo.notepad3;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;



public class Notepadv3 extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private NotesDbAdapter mDbHelper;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        //INSTRUMENTED
        Log.d("wtchoi","Notepadv3.onCreate");
        Supervisor.init();
        Supervisor.logActivityCreated(this);
        
        
        setContentView(R.layout.notes_list);
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
        
        //INSTRUMENTED
        Supervisor.start();
    }

    private void fillData() {
        //INSTRUMENTED
    	Supervisor.logCall("fillData",this);
    	
        Cursor notesCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(notesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{NotesDbAdapter.KEY_TITLE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = 
            new SimpleCursorAdapter(this, R.layout.notes_row, notesCursor, from, to);
        setListAdapter(notes);
        
        //INSTRUMENTED
        Supervisor.logReturn("fillData",this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	//INSTRUMENTED
    	Supervisor.logCall("onCreateOptionMenu",this);
    	
        super.onCreateOptionsMenu(menu);     
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        
        //INSTRUMENTED
        Supervisor.logReturn("onCreateOptionMenu",this);
        
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	//INSTRUMENTED
    	Supervisor.logCall("onMenuItemSelected",this);
    	
    	boolean result = false;
    	
        switch(item.getItemId()) {
            case INSERT_ID:
                createNote();
                result = true;
                break;
            default:
            	result = super.onMenuItemSelected(featureId, item);
                break;
        }

    	//INSTRUMENTED
    	Supervisor.logReturn("onMenuItemSelected",this);
        return result;
    }


    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
    	//INSTRUMENTED
    	Supervisor.logCall("onCreateContextMenu",this);

        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
        
    	//INSTRUMENTED
    	Supervisor.logReturn("onCreateContextMenu",this);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	//INSTRUMENTED
    	Supervisor.logCall("onContextItemSelection",this);
    	
        switch(item.getItemId()) {
            case DELETE_ID:
            	//INSTRUMENTED
            	Supervisor.logSwitch("DELETE_ID",this);
            	
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
                fillData();
                return true;
        }
        
        //INSTRUMENTED
    	Supervisor.logReturn("onContextItemSelection",this);
    	
        return super.onContextItemSelected(item);
    }

    private void createNote() {
    	 //INSTRUMENTED
    	Supervisor.logCall("createNote",this);
    	
        Intent i = new Intent(this, NoteEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
        
        //INSTRUMENTED
    	Supervisor.logReturn("createNote",this);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	 //INSTRUMENTED
    	Supervisor.logCall("onListItemClick",this);
    	
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, NoteEdit.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
        
        //INSTRUMENTED
    	Supervisor.logReturn("onListItemClick",this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	 //INSTRUMENTED
    	Supervisor.logCall("onActivityResult",this);
    	
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
        
        //INSTRUMENTED
    	Supervisor.logReturn("onActivityResult",this);
    }
    
    //INSTRUMTENTED
    @Override
    protected void onStart(){
    	Log.d("wtchoi","Notepadv3.onStart");
    	super.onStart();
    	Supervisor.logStart(this);
    }
    
    @Override
    protected void onStop(){
    	Log.d("wtchoi","Notepadv3.onStop");
    	Supervisor.logStop(this);
    	super.onStop();
    }
}
